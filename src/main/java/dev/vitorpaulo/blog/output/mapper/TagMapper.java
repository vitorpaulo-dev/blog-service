package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.TagContentEntity;
import dev.vitorpaulo.blog.domain.TagEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagContentModel;
import dev.vitorpaulo.blog.model.TagModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TagMapper {

    // ── Entity → Model ──

    @Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
    TagModel toModel(TagEntity entity);

    TagContentModel toContentModel(TagContentEntity entity);

    default Map<Language, TagContentModel> contentsToTranslations(List<TagContentEntity> contents) {
        if (contents == null) return Map.of();
        return contents.stream().collect(Collectors.toMap(
            TagContentEntity::getLanguage,
            this::toContentModel
        ));
    }

    // ── Model → Entity ──

    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TagEntity toEntity(TagModel model);

    List<TagEntity> toEntityList(List<TagModel> models);
}
