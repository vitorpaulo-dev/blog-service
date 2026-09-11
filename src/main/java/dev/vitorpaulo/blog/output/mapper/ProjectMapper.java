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

    default ProjectModel toModel(ProjectEntity entity, Language language) {
        ProjectModel model = toModel(entity);
        if (model == null || language == null) return model;
        return filterTranslations(model, language);
    }

    private ProjectModel filterTranslations(ProjectModel model, Language language) {
        var filtered = filterTranslationMap(model.translations(), language);
        return new ProjectModel(
            model.id(),
            model.slug(),
            model.logoUrl(),
            model.programmingLanguage(),
            model.bannerUrl(),
            model.githubUrl(),
            model.websiteUrl(),
            model.status(),
            model.createdAt(),
            model.updatedAt(),
            model.authors(),
            model.viewCount(),
            model.loveCount(),
            model.celebrateCount(),
            model.geniusCount(),
            model.helpCount(),
            model.reactionCount(),
            filtered
        );
    }

    private <T> Map<Language, T> filterTranslationMap(Map<Language, T> translations, Language requested) {
        if (translations == null || translations.isEmpty()) return Map.of();
        if (translations.containsKey(requested)) {
            return Map.of(requested, translations.get(requested));
        }
        if (translations.containsKey(Language.ENGLISH)) {
            return Map.of(Language.ENGLISH, translations.get(Language.ENGLISH));
        }
        return translations.entrySet().stream()
            .findFirst()
            .map(e -> Map.of(e.getKey(), e.getValue()))
            .orElse(Map.of());
    }

    ProjectContentModel toContentModel(ProjectContentEntity entity);

    default Map<Language, ProjectContentModel> contentsToTranslations(List<ProjectContentEntity> contents) {
        if (contents == null) return Map.of();
        return contents.stream()
			.collect(Collectors.toMap(
            ProjectContentEntity::getLanguage,
            this::toContentModel
        ));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "authors", ignore = true)
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
