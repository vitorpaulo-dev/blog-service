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
