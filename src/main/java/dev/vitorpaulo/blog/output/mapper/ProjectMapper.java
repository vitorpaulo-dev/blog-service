package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.input.response.ProjectResponse;
import dev.vitorpaulo.blog.model.ProjectModel;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectModel toModel(ProjectEntity entity);

    ProjectEntity toEntity(ProjectModel model);

	List<ProjectEntity> toEntitySet(List<ProjectModel> models);
}
