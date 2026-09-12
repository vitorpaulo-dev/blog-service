package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.model.AuthorModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthorOutputMapper {

    @Mapping(target = "clerkUserId", source = "entity.subjectId")
    @Mapping(target = "role", ignore = true)
    AuthorModel toModel(AuthorEntity entity);

    @Mapping(target = "clerkUserId", source = "entity.subjectId")
    AuthorModel toModel(AuthorEntity entity, String role);
}
