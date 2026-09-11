package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.TagContentEntity;
import dev.vitorpaulo.blog.domain.TagEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagContentModel;
import dev.vitorpaulo.blog.model.TagModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
    TagModel toModel(TagEntity entity);

    default TagModel toModel(TagEntity entity, Language language) {
        TagModel model = toModel(entity);
        if (model == null || language == null) return model;
        var filtered = filterTranslations(model.translations(), language);
        return new TagModel(model.id(), model.slug(), filtered);
    }

    TagContentModel toContentModel(TagContentEntity entity);

    default Map<Language, TagContentModel> contentsToTranslations(List<TagContentEntity> contents) {
        if (contents == null) return Map.of();
        return contents.stream().collect(Collectors.toMap(
            TagContentEntity::getLanguage,
            this::toContentModel
        ));
    }

    default Map<Language, TagContentModel> filterTranslations(Map<Language, TagContentModel> translations, Language requested) {
        if (translations == null || translations.isEmpty()) return Map.of();
        if (translations.containsKey(requested)) return Map.of(requested, translations.get(requested));
        if (translations.containsKey(Language.ENGLISH)) return Map.of(Language.ENGLISH, translations.get(Language.ENGLISH));
        return translations.entrySet().stream().findFirst()
            .map(e -> Map.of(e.getKey(), e.getValue())).orElse(Map.of());
    }

    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TagEntity toEntity(TagModel model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TagModel model, @MappingTarget TagEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TagContentEntity toContentEntity(TagContentModel model);
}
