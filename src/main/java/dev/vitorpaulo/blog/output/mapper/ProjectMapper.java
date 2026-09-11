package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.ProjectContentEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProjectMapper {

    @Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
    ProjectModel toModel(ProjectEntity entity);

    ProjectContentModel toContentModel(ProjectContentEntity entity);

    default Map<Language, ProjectContentModel> contentsToTranslations(List<ProjectContentEntity> contents) {
        if (contents == null) return Map.of();
        return contents.stream()
		.collect(Collectors.toMap(
            ProjectContentEntity::getLanguage,
            this::toContentModel
        ));
    }

    default ProjectModel toModel(ProjectEntity entity, Language language) {
        ProjectModel model = toModel(entity);
        if (model == null || language == null) return model;
        return filterModelByLanguage(model, language);
    }

    private ProjectModel filterModelByLanguage(ProjectModel model, Language language) {
        var filteredTranslations = filterTranslations(model.translations(), language);
        var filteredTags = model.tags() == null ? List.<TagModel>of() :
            model.tags().stream()
                .map(t -> filterTagModel(t, language))
                .toList();

        return new ProjectModel(
            model.id(), model.slug(), model.logoUrl(), model.bannerUrl(),
            model.githubUrl(), model.websiteUrl(), model.status(),
            model.createdAt(), model.updatedAt(), model.authors(), filteredTags,
            model.viewCount(), model.loveCount(), model.celebrateCount(),
            model.geniusCount(), model.helpCount(), model.reactionCount(), filteredTranslations
        );
    }

    static Map<Language, ProjectContentModel> filterTranslations(Map<Language, ProjectContentModel> translations, Language requested) {
        if (translations == null || translations.isEmpty()) return Map.of();
        if (translations.containsKey(requested)) return Map.of(requested, translations.get(requested));
        if (translations.containsKey(Language.ENGLISH)) return Map.of(Language.ENGLISH, translations.get(Language.ENGLISH));
        return translations.entrySet().stream().findFirst()
            .map(e -> Map.of(e.getKey(), e.getValue())).orElse(Map.of());
    }

    private TagModel filterTagModel(TagModel tag, Language language) {
        if (tag == null || tag.translations() == null || tag.translations().isEmpty()) return tag;
        var translations = tag.translations();
        if (translations.containsKey(language)) return new TagModel(tag.id(), tag.slug(), Map.of(language, translations.get(language)));
        if (translations.containsKey(Language.ENGLISH)) return new TagModel(tag.id(), tag.slug(), Map.of(Language.ENGLISH, translations.get(Language.ENGLISH)));
        return translations.entrySet().stream().findFirst()
            .map(e -> new TagModel(tag.id(), tag.slug(), Map.of(e.getKey(), e.getValue())))
            .orElse(tag);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "loveCount", ignore = true)
    @Mapping(target = "celebrateCount", ignore = true)
    @Mapping(target = "geniusCount", ignore = true)
    @Mapping(target = "helpCount", ignore = true)
    @Mapping(target = "reactionCount", ignore = true)
    void updateEntity(ProjectModel model, @MappingTarget ProjectEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProjectContentEntity toContentEntity(ProjectContentModel model);
}
