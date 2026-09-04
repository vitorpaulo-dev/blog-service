package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
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
public interface PostOutputMapper {

    // ── Entity → Model ──

    @Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
    PostModel toModel(PostEntity entity);

    PostContentModel toContentModel(PostContentEntity entity);

    default PostModel toModel(PostEntity entity, Language language) {
        if (language == null) return toModel(entity);
        final var filtered = entity.getContents().stream()
            .filter(c -> c.getLanguage() == language)
            .toList();
        final var model = toModel(entity);
        final var filteredTranslations = contentsToTranslations(filtered);
        return new PostModel(
            model.id(),
            model.slug(),
            model.bannerUrl(),
            model.status(),
            model.estimatedReading(),
            model.createdAt(),
            model.updatedAt(),
            model.authors(),
            model.tags(),
            model.projects(),
            model.viewCount(),
            model.loveCount(),
            model.celebrateCount(),
            model.geniusCount(),
            model.helpCount(),
            model.reactionCount(),
            filteredTranslations
        );
    }

    default Map<Language, PostContentModel> contentsToTranslations(List<PostContentEntity> contents) {
        if (contents == null) return Map.of();
        return contents.stream().collect(Collectors.toMap(
            PostContentEntity::getLanguage,
            this::toContentModel
        ));
    }

    // ── Model → Entity (update existing or populate new) ──

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "estimatedReading", ignore = true)
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "loveCount", ignore = true)
    @Mapping(target = "celebrateCount", ignore = true)
    @Mapping(target = "geniusCount", ignore = true)
    @Mapping(target = "helpCount", ignore = true)
    @Mapping(target = "reactionCount", ignore = true)
    void updateEntity(PostModel model, @MappingTarget PostEntity entity);

    // ── Content entity creation ──

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PostContentEntity toContentEntity(PostContentModel model);

    // ── Sub-entity references ──

    @Mapping(target = "subjectId", ignore = true)
    @Mapping(target = "githubUrl", ignore = true)
    @Mapping(target = "linkedinUrl", ignore = true)
    @Mapping(target = "instagramUrl", ignore = true)
    @Mapping(target = "websiteUrl", ignore = true)
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AuthorEntity toAuthorEntity(AuthorModel model);
}
