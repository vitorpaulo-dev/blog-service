package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static dev.vitorpaulo.blog.common.util.RoleUtils.isAdmin;

@Component
@RequiredArgsConstructor
public class PostOutput {

    private final PostRepository postRepository;
    private final PostOutputMapper postOutputMapper;
	private final ProjectRepository projectRepository;
	private final TagRepository tagRepository;
	private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public PostModel findById(UUID id) {
        return postRepository.findByIdWithContents(id)
			.map(entity -> postOutputMapper.toModel(
				entity,
				postRepository.findProjectIds(id),
				postRepository.findTagIds(id)
			))
			.orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));
    }

    @Transactional
    public PostModel findBySlugAndIncrementView(String slug, Language language) {
        final var entity = postRepository.findBySlugAndLanguage(slug, language)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_SLUG_NOT_FOUND));
        entity.setViewCount(entity.getViewCount() == null ? 1L : entity.getViewCount() + 1);

        return postOutputMapper.toModel(
			postRepository.save(entity),
			postRepository.findProjectIds(entity.getId()),
			postRepository.findTagIds(entity.getId())
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
        return postOutputMapper.toModel(saved, projectIds, tagIds);
    }

    @Transactional
    public PostModel update(PostModel post, List<UUID> tagIds, List<UUID> projectIds, AuthorModel author) {
        validateTranslations(post.translations());
        final var entity = postRepository.findByIdWithAuthor(post.id(), author.id(), isAdmin(author.role()))
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
        return postOutputMapper.toModel(saved, projectIds, tagIds);
    }

    @Transactional
    public void deleteAll(List<UUID> ids, AuthorModel author) {
        postRepository.deleteByIdWithAuthor(ids, author.id(), isAdmin(author.role()));
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
                author != null,
                pageable,
                pageableInput.sort(),
				pageableInput.direction().name()
            );

        return new PaginatedOutput<>(
			page
				.stream()
				.map(post -> postOutputMapper.toModel(post, Collections.emptyList(), postRepository.findTagIds(post.getId())))
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

    private void syncContents(PostEntity entity, Map<Language, PostContentModel> translations) {
        if (translations == null) return;

        final var existingByLang = entity.getContents().stream()
            .collect(Collectors.toMap(PostContentEntity::getLanguage, c -> c));

        translations.forEach((lang, model) -> {
            final var existing = existingByLang.get(lang);
            if (existing != null) {
                existing.setTitle(model.title());
                existing.setContent(model.content());
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
            return new PostContentModel("", "");
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
