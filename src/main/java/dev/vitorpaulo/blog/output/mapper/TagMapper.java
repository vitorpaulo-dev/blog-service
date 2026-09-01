package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.TagEntity;
import dev.vitorpaulo.blog.input.response.TagResponse;
import dev.vitorpaulo.blog.model.TagModel;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagModel toModel(TagEntity entity);

    TagEntity toEntity(TagModel model);

	List<TagEntity> toEntitySet(List<TagModel> models);
}
