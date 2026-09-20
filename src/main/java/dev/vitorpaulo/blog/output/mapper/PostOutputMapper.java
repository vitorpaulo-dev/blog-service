package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
	componentModel = "spring",
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PostOutputMapper {

	@Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
	PostModel toModel(PostEntity entity, List<UUID> projectIds, List<UUID> tagIds, Map<AudioType, Map<Language, AudioModel>> audio);

	ReactionModel toReactionModel(PostEntity entity);

	PostContentModel toContentModel(PostContentEntity entity);

	default Map<Language, PostContentModel> contentsToTranslations(List<PostContentEntity> contents) {
		if (contents == null) return Map.of();
		return contents.stream()
			.collect(Collectors.toMap(
				PostContentEntity::getLanguage,
				this::toContentModel
			));
	}

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

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	PostContentEntity toContentEntity(PostContentModel model, Language language, PostEntity post);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "language", ignore = true)
	@Mapping(target = "post", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	PostContentEntity toContentEntity(PostContentModel model);
}
