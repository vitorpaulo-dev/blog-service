package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.config.security.AuthorRoleChecker;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import dev.vitorpaulo.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostOutput {

	private final PostRepository postRepository;
    private final PostOutputMapper postOutputMapper;
	private final ProjectRepository projectRepository;
	private final TagRepository tagRepository;
	private final AuthorRepository authorRepository;
    private final RedisRepository redisRepository;
    private final AudioOutput audioOutput;
    private final AuthorRoleChecker authorRoleChecker;

	private static final String KEY_PREFIX = "post:";
	private static final String VIEWS_KEY_SUFFIX = ":views";
	private static final String REACTIONS_KEY_SUFFIX = ":reactions";
	private static final Duration VIEW_TTL = Duration.ofHours(48);
	private static final Duration REACTION_TTL = Duration.ofSeconds(604800);

    @Transactional(readOnly = true)
    public PostModel findById(UUID id) {
        return postRepository.findByIdWithContents(id)
			.map(entity -> postOutputMapper.toModel(
				entity,
				postRepository.findProjectIds(id),
				postRepository.findTagIds(id),
				audioOutput.artifactMap(entity, null)
			))
			.orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));
    }

    @Transactional
    public ReactionModel react(String slug, ReactionType reactionType, String ip) {
        final var entity = postRepository.findBySlugAndStatus(slug, PostStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));

        final var key = KEY_PREFIX + entity.getId() + ":reaction:" + ip + ":" + reactionType;
        if (redisRepository.keyExists(key)) {
            return postOutputMapper.toReactionModel(entity);
        }

        final var now = System.currentTimeMillis();
        increment(entity, reactionType);
        final var saved = postRepository.save(entity);
        redisRepository.set(key, REACTION_TTL);
        redisRepository.addToSortedSet(
                KEY_PREFIX + entity.getId() + REACTIONS_KEY_SUFFIX,
                ip + ":" + reactionType + ":" + now,
                now,
                REACTION_TTL
        );

        return postOutputMapper.toReactionModel(saved);
    }

    private void increment(PostEntity entity, ReactionType reactionType) {
        switch (reactionType) {
            case LOVE -> entity.setLoveCount(next(entity.getLoveCount()));
            case CELEBRATE -> entity.setCelebrateCount(next(entity.getCelebrateCount()));
            case GENIUS -> entity.setGeniusCount(next(entity.getGeniusCount()));
            case HELP -> entity.setHelpCount(next(entity.getHelpCount()));
        }
    }

    private Long next(Long current) {
        return current == null ? 1L : current + 1;
    }

    @Transactional
    public PostModel findBySlugAndIncrementView(String slug, Language language, String ip) {
        final var entity = postRepository.findBySlugAndLanguage(slug, language)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_SLUG_NOT_FOUND));

        final var viewKey = KEY_PREFIX + entity.getId() + ":view:" + ip;
        if (!redisRepository.keyExists(viewKey)) {
            final var now = System.currentTimeMillis();
            entity.setViewCount(entity.getViewCount() == null ? 1L : entity.getViewCount() + 1);
            postRepository.save(entity);
            redisRepository.set(viewKey, VIEW_TTL);
            redisRepository.addToSortedSet(
                    KEY_PREFIX + entity.getId() + VIEWS_KEY_SUFFIX,
                    ip + ":" + now,
                    now,
                    VIEW_TTL
            );
        }

		return postOutputMapper.toModel(
			entity,
			postRepository.findProjectIds(entity.getId()),
			postRepository.findTagIds(entity.getId()),
			audioOutput.artifactMap(entity, language)
		);
    }

	@Transactional
    public PostModel save(PostModel post, List<UUID> tagIds, List<UUID> projectIds, AuthorModel author) {
        validateTranslations(post.translations());
        final var firstContent = getFirstContent(post.translations());
        final var slug = generateUniqueSlug(firstContent.title(), null);
        final var reading = PostUtils.computeReadingTime(firstContent.content());

        final var entity = new PostEntity();
        postOutputMapper.updateEntity(post, entity);
		entity.setEstimatedReading(reading);
		entity.setCelebrateCount(0L);
		entity.setGeniusCount(0L);
		entity.setHelpCount(0L);
        entity.setViewCount(0L);
        entity.setLoveCount(0L);
		entity.setSlug(slug);

        post.translations()
			.forEach((lang, cm) -> {
				var contentEntity = postOutputMapper.toContentEntity(cm);
				contentEntity.setLanguage(lang);
				contentEntity.setPost(entity);
				entity.getContents().add(contentEntity);
			});
        entity.setAuthors(authorRepository.findAllById(List.of(author.id())));
        if (tagIds != null) entity.setTags(tagRepository.findAllById(tagIds));
        if (projectIds != null) entity.setProjects(projectRepository.findAllById(projectIds));

        final var saved = postRepository.save(entity);
        audioOutput.dispatch(saved);
        return postOutputMapper.toModel(saved, projectIds, tagIds, audioOutput.artifactMap(saved, null));
    }

    @Transactional
    public PostModel update(PostModel post, List<UUID> tagIds, List<UUID> projectIds, AuthorModel author) {
        validateTranslations(post.translations());
        final var entity = postRepository.findByIdWithAuthor(post.id(), author.id(), authorRoleChecker.isAdmin())
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));

        final var firstContent = getFirstContent(post.translations());
        final var previousTitle = getFirstContentTitle(entity.getContents());
        final var titleChanged = firstContent.title() != null
                && !firstContent.title().equalsIgnoreCase(previousTitle);

        postOutputMapper.updateEntity(post, entity);
        entity.setEstimatedReading(PostUtils.computeReadingTime(firstContent.content()));
        if (titleChanged) {
            entity.setSlug(generateUniqueSlug(firstContent.title(), entity.getId()));
        }

        syncContents(entity, post.translations());

        entity.setTags(tagRepository.findAllById(Objects.requireNonNullElse(tagIds, Collections.emptyList())));
        entity.setProjects(projectRepository.findAllById(Objects.requireNonNullElse(projectIds, Collections.emptyList())));

        final var saved = postRepository.save(entity);
        audioOutput.dispatch(saved);
        return postOutputMapper.toModel(saved, projectIds, tagIds, audioOutput.artifactMap(saved, null));
    }

    @Transactional
    public void deleteAll(List<UUID> ids, AuthorModel author) {
        final var entities = postRepository.findAllByIdWithAuthor(ids, author.id(), authorRoleChecker.isAdmin());
        postRepository.deleteAll(entities);
    }

    @Transactional
    public void setFeaturedWeights(List<FeaturedPostModel> featured, AuthorModel author) {
        final var ids = featured.stream().map(FeaturedPostModel::postId).toList();

        if (!ids.isEmpty()) {
            final var posts = postRepository.findAllById(ids);
            if (posts.size() != ids.size()) {
                throw new NotFoundException(ExceptionCode.POST_NOT_FOUND);
            }
        }

        postRepository.clearFeaturedWeights();
        featured.forEach(featuredPost -> postRepository.updateWeight(featuredPost.postId(), featuredPost.weight()));
    }

    @Transactional(readOnly = true)
    public List<PostModel> findFeatured(Language language) {
        return postRepository.findFeatured()
            .stream()
            .map(post -> postOutputMapper.toModel(post, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap()))
			.peek(post -> {
				final var contents = post.translations();
				if (contents.size() <= 1) return;

				contents.keySet().removeIf(key -> key != language);
			})
			.toList();
    }

    @Transactional(readOnly = true)
    public PaginatedOutput<PostModel> search(PaginatedInput<PostQueryModel> pageableInput, AuthorModel author) {
        final var language = pageableInput.query().language();
        final var pageable = PageRequest.of(pageableInput.page(), pageableInput.size());
        final var page = postRepository.search(
                pageableInput.query().query(),
                pageableInput.query().authorId(),
                pageableInput.query().tagId(),
                language != null ? language.name() : null,
                showsDrafts(pageableInput.query().authorId(), author),
                pageable,
                pageableInput.sort(),
				pageableInput.direction().name()
            );

        return new PaginatedOutput<>(
			page
				.stream()
				.map(post -> postOutputMapper.toModel(post, Collections.emptyList(), postRepository.findTagIds(post.getId()), Collections.emptyMap()))
				.peek(post -> {
					final var contents = post.translations();
					if (contents.size() <= 1) return;

					contents.keySet().removeIf(key -> key != language);
				})
				.toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    private boolean showsDrafts(UUID filterAuthorId, AuthorModel author) {
        if (authorRoleChecker.isAdmin()) {
            return true;
        }
        return author != null
            && author.id() != null
            && filterAuthorId != null
            && filterAuthorId.equals(author.id());
    }

    private void syncContents(PostEntity entity, Map<Language, PostContentModel> translations) {
        if (translations == null) return;

        final var existingByLang = entity.getContents().stream()
            .collect(Collectors.toMap(PostContentEntity::getLanguage, c -> c));

        translations.forEach((lang, model) -> {
            final var existing = existingByLang.get(lang);
            if (existing != null) {
                existing.setTitle(model.title());
                existing.setContent(model.content());
                existing.setSummary(model.summary());
            } else {
                var contentEntity = postOutputMapper.toContentEntity(model);
                contentEntity.setLanguage(lang);
                contentEntity.setPost(entity);
                entity.getContents().add(contentEntity);
            }
        });

        entity.getContents().removeIf(c -> !translations.containsKey(c.getLanguage()));
    }

    private PostContentModel getFirstContent(Map<Language, PostContentModel> translations) {
        if (translations == null || translations.isEmpty()) {
            return new PostContentModel("", "", "");
        }
        final var english = translations.get(Language.ENGLISH);
        if (english != null) return english;
        return translations.values().iterator().next();
    }

    private String getFirstContentTitle(List<PostContentEntity> contents) {
        if (contents == null || contents.isEmpty()) return null;
        final var english = contents.stream()
                .filter(c -> c.getLanguage() == Language.ENGLISH)
                .findFirst();
        if (english.isPresent()) return english.get().getTitle();
        return contents.getFirst().getTitle();
    }

    private void validateTranslations(Map<Language, PostContentModel> translations) {
        if (translations == null || translations.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ExceptionCode.FIELD_VALIDATION, null);
        }
    }

    private String generateUniqueSlug(String title, UUID currentId) {
        final var base = PostUtils.slugify(title);
        final var counter = postRepository.countBySlugAndIdNot(base, currentId);
        if (counter == 0) return base;
        return base + "-" + (counter + 1);
    }
}
