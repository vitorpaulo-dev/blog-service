package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.input.response.AuthorResponse;
import dev.vitorpaulo.blog.model.AuthorModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorModel toModel(AuthorEntity entity, String role);
}
