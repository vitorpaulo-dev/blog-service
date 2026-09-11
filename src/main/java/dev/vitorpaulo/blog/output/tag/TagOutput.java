package dev.vitorpaulo.blog.output.tag;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.domain.TagContentEntity;
import dev.vitorpaulo.blog.domain.TagEntity;
import dev.vitorpaulo.blog.output.mapper.TagMapper;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagContentModel;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TagOutput {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagModel findById(UUID id) {
        return tagRepository.findById(id)
            .map(tagMapper::toModel)
            .orElseThrow(() -> new NotFoundException(ExceptionCode.TAG_NOT_FOUND));
    }

    public TagModel findBySlug(String slug, Language language) {
        return tagRepository.findBySlugAndLanguage(slug, language)
            .map(entity -> tagMapper.toModel(entity, language))
            .orElseThrow(() -> new NotFoundException(ExceptionCode.TAG_NOT_FOUND));
    }

    @Transactional
    public TagModel save(TagModel tag) {
        final var firstContent = getFirstContent(tag.translations());
        final var slug = generateUniqueSlug(firstContent.name(), null);

        final var entity = new TagEntity();
        tagMapper.updateEntity(tag, entity);
        entity.setSlug(slug);

        syncContents(entity, tag.translations());

        return tagMapper.toModel(tagRepository.save(entity));
    }

    @Transactional
    public TagModel update(TagModel tag) {
        final var entity = tagRepository.findById(tag.id())
            .orElseThrow(() -> new NotFoundException(ExceptionCode.TAG_NOT_FOUND));

        final var firstContent = getFirstContent(tag.translations());
        final var previousName = getFirstContentName(entity.getContents());
        final var nameChanged = firstContent.name() != null
            && !firstContent.name().equalsIgnoreCase(previousName);

        tagMapper.updateEntity(tag, entity);
        if (nameChanged) {
            entity.setSlug(generateUniqueSlug(firstContent.name(), entity.getId()));
        }

        syncContents(entity, tag.translations());

        return tagMapper.toModel(tagRepository.save(entity));
    }

    @Transactional
    public void deleteAll(List<UUID> ids) {
        tagRepository.deleteByIdIn(ids);
    }

    public PaginatedOutput<TagModel> search(PaginatedInput<TagQueryModel> input, Language language) {
        final var pageable = PageRequest.of(input.page(), input.size(),
            Sort.by(input.direction(), mapSortProperty(input.sort())));
        final var name = input.query() != null ? input.query().name() : null;
        final var result = tagRepository.search(name, pageable)
            .map(entity -> tagMapper.toModel(entity, language));

        return new PaginatedOutput<>(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    public List<TagModel> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return tagRepository.findAllById(ids).stream()
            .map(tagMapper::toModel)
            .toList();
    }

    public List<TagModel> findAllById(List<UUID> ids, Language language) {
        if (ids == null || ids.isEmpty()) return List.of();
        return tagRepository.findAllById(ids).stream()
            .map(entity -> tagMapper.toModel(entity, language))
            .toList();
    }

    private void syncContents(TagEntity entity, Map<Language, TagContentModel> translations) {
        if (translations == null) return;
        final var existingByLang = entity.getContents().stream()
            .collect(Collectors.toMap(TagContentEntity::getLanguage, c -> c));
        translations.forEach((lang, model) -> {
            final var existing = existingByLang.get(lang);
            if (existing != null) {
                existing.setName(model.name());
            } else {
                final var content = tagMapper.toContentEntity(model);
                content.setLanguage(lang);
                content.setTag(entity);
                entity.getContents().add(content);
            }
        });

        entity.getContents().removeIf(c -> !translations.containsKey(c.getLanguage()));
    }

    private TagContentModel getFirstContent(Map<Language, TagContentModel> translations) {
        if (translations == null || translations.isEmpty()) {
            return new TagContentModel("");
        }
        final var english = translations.get(Language.ENGLISH);
        return english != null ? english : translations.values().iterator().next();
    }

    private String getFirstContentName(List<TagContentEntity> contents) {
        if (contents == null || contents.isEmpty()) return null;
        return contents.stream()
            .filter(c -> c.getLanguage() == Language.ENGLISH)
            .findFirst()
            .map(TagContentEntity::getName)
            .orElseGet(() -> contents.getFirst().getName());
    }

    private String generateUniqueSlug(String name, UUID currentId) {
        final var base = PostUtils.slugify(name);
        final var counter = tagRepository.countBySlugAndIdNot(base, currentId);
        return counter == 0 ? base : base + "-" + (counter + 1);
    }

    private String mapSortProperty(String sort) {
        return switch (sort != null ? sort : "") {
            case "slug" -> "slug";
            case "name" -> "slug";
            default -> "created_at";
        };
    }
}
