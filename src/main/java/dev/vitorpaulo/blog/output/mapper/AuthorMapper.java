package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.AuthorContentEntity;
import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.model.AuthorContentModel;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    @Mapping(target = "clerkUserId", source = "entity.subjectId")
    @Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
    @Mapping(target = "role", ignore = true)
    AuthorModel toModel(AuthorEntity entity);

    @Mapping(target = "clerkUserId", source = "entity.subjectId")
    @Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
    AuthorModel toModel(AuthorEntity entity, String role);

    AuthorContentModel toContentModel(AuthorContentEntity entity);

    default Map<Language, AuthorContentModel> contentsToTranslations(List<AuthorContentEntity> contents) {
        if (contents == null) return Map.of();
        return contents.stream().collect(Collectors.toMap(
            AuthorContentEntity::getLanguage,
            this::toContentModel
        ));
    }
}
