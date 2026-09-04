package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.ProjectContentEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectContentModel;
import dev.vitorpaulo.blog.model.ProjectModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    // ── Entity → Model ──

    @Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
    ProjectModel toModel(ProjectEntity entity);

    ProjectContentModel toContentModel(ProjectContentEntity entity);

    default Map<Language, ProjectContentModel> contentsToTranslations(List<ProjectContentEntity> contents) {
        if (contents == null) return Map.of();
        return contents.stream().collect(Collectors.toMap(
            ProjectContentEntity::getLanguage,
            this::toContentModel
        ));
    }

    // ── Model → Entity ──

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProjectEntity toEntity(ProjectModel model);

    List<ProjectEntity> toEntityList(List<ProjectModel> models);
}
