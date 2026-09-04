package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.output.mapper.ProjectMapper;
import dev.vitorpaulo.blog.output.mapper.TagMapper;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.vitorpaulo.blog.common.util.RoleUtils.isAdmin;

@Component
@RequiredArgsConstructor
public class PostOutput {

    private final PostRepository postRepository;
    private final PostOutputMapper postOutputMapper;
    private final TagMapper tagMapper;
    private final ProjectMapper projectMapper;

    public PostModel findById(UUID id) {
        return postRepository.findById(id)
                .map(postOutputMapper::toModel)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));
    }

    @Transactional
    public PostModel findBySlugAndIncrementView(String slug, Language language) {
        final var entity = postRepository.findBySlugAndLanguage(slug, language)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_SLUG_NOT_FOUND));
        entity.setViewCount(entity.getViewCount() == null ? 1L : entity.getViewCount() + 1);
        final var saved = postRepository.save(entity);
        return postOutputMapper.toModel(saved, language);
    }

    @Transactional
    public PostModel save(PostModel post, List<TagModel> tags, List<ProjectModel> projects, AuthorModel author) {
        validateTranslations(post.translations());
        final var firstContent = getFirstContent(post.translations());
        final var slug = generateUniqueSlug(firstContent.title(), null);
        final var reading = PostUtils.computeReadingTime(firstContent.content());

        final var entity = new PostEntity();
        postOutputMapper.updateEntity(post, entity);
        entity.setSlug(slug);
        entity.setEstimatedReading(reading);
        entity.setViewCount(0L);
        entity.setLoveCount(0L);
        entity.setCelebrateCount(0L);
        entity.setGeniusCount(0L);
        entity.setHelpCount(0L);

        post.translations().forEach((lang, cm) -> {
            final var content = postOutputMapper.toContentEntity(cm);
            content.setLanguage(lang);
            content.setPost(entity);
            entity.getContents().add(content);
        });

        entity.setAuthors(new ArrayList<>(List.of(postOutputMapper.toAuthorEntity(author))));
        entity.setTags(tags != null ? tagMapper.toEntityList(tags) : new ArrayList<>());
        entity.setProjects(projects != null ? projectMapper.toEntityList(projects) : new ArrayList<>());

        return postOutputMapper.toModel(postRepository.save(entity));
    }

    @Transactional
    public PostModel update(PostModel post, List<TagModel> tags, List<ProjectModel> projects, AuthorModel author) {
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

        if (tags != null) entity.setTags(tagMapper.toEntityList(tags));
        if (projects != null) entity.setProjects(projectMapper.toEntityList(projects));

        return postOutputMapper.toModel(postRepository.save(entity));
    }

    @Transactional
    public void deleteAll(List<UUID> ids, AuthorModel author) {
        postRepository.deleteByIdWithAuthor(ids, author.id(), isAdmin(author.role()));
    }

    public PaginatedOutput<PostModel> search(PaginatedInput<PostQueryModel> pageableInput, AuthorModel author) {
        final var language = pageableInput.query().language();
        final var pageable = PageRequest.of(pageableInput.page(), pageableInput.size());
        final var result = postRepository.search(
                pageableInput.query().query(),
                pageableInput.query().authorId(),
                language != null ? language.name() : null,
                author == null,
                pageable,
                mapSortProperty(pageableInput.sort(), pageableInput.direction())
            )
            .map(entity -> postOutputMapper.toModel(entity, language));

        return new PaginatedOutput<>(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    // ── Orchestration helpers ──

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
                final var content = postOutputMapper.toContentEntity(model);
                content.setLanguage(lang);
                content.setPost(entity);
                entity.getContents().add(content);
            }
        });

        entity.getContents().removeIf(c -> !translations.containsKey(c.getLanguage()));
    }

    // ── Business helpers ──

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
        return contents.get(0).getTitle();
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
        return base + "-" + counter + 1;
    }

    private String mapSortProperty(String sort, Sort.Direction direction) {
        final var dir = direction.name();
        return switch (sort) {
            case "slug" -> "slug " + dir;
            case "viewCount" -> "view_count " + dir;
            case "reactionCount" -> "reactionCount " + dir;
            default -> "created_at " + dir + ", updated_at " + dir;
        };
    }
}
