package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.client.audio.AudioJobRequest;
import dev.vitorpaulo.blog.domain.AudioEntity;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.model.AudioStatus;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.model.audio.AudioProgressModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(
	componentModel = "spring",
	unmappedTargetPolicy = ReportingPolicy.ERROR,
	unmappedSourcePolicy = ReportingPolicy.ERROR
)
public interface AudioOutputMapper {

	@BeanMapping(ignoreUnmappedSourceProperties = {"id", "postId", "contentHash", "createdAt", "updatedAt"})
	@Mapping(target = "type", source = "entity.type")
	@Mapping(target = "language", source = "entity.language")
	@Mapping(target = "status", source = "entity.status")
	@Mapping(target = "key", source = "entity.r2Key")
	@Mapping(target = "error", source = "entity.errorMessage")
	@Mapping(target = "progress", ignore = true)
	AudioModel toModel(AudioEntity entity);

	@Mapping(target = "type", source = "entity.type")
	@Mapping(target = "language", source = "entity.language")
	@Mapping(target = "status", expression = "java(mergedStatus(entity.getStatus(), progress))")
	@Mapping(target = "key", source = "entity.r2Key")
	@Mapping(target = "error", expression = "java(mergedError(entity.getErrorMessage(), progress))")
	@Mapping(target = "progress", source = "progress.progress")
	@BeanMapping(ignoreUnmappedSourceProperties = {
		"id", "postId", "status", "contentHash", "errorMessage", "createdAt", "updatedAt", "error"
	})
	AudioModel toModel(AudioEntity entity, AudioProgressModel progress);

	@BeanMapping(ignoreUnmappedSourceProperties = {"id", "post", "summary", "createdAt", "updatedAt"})
	@Mapping(target = "language", source = "content.language")
	@Mapping(target = "title", source = "content.title")
	@Mapping(target = "content", source = "content.content")
	AudioJobRequest.AudioJobContent toJobContent(PostContentEntity content);

	List<AudioJobRequest.AudioJobContent> toJobContents(List<PostContentEntity> contents);

	@BeanMapping(ignoreUnmappedSourceProperties = {
		"leastSignificantBits", "mostSignificantBits", "declaringClass"
	})
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "postId", source = "postId")
	@Mapping(target = "type", source = "type")
	@Mapping(target = "language", source = "language")
	@Mapping(target = "status", constant = "QUEUED")
	@Mapping(target = "r2Key", ignore = true)
	@Mapping(target = "contentHash", ignore = true)
	@Mapping(target = "errorMessage", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	AudioEntity toEntity(UUID postId, AudioType type, Language language);

	default AudioStatus mergedStatus(AudioStatus dbStatus, AudioProgressModel progress) {
		if (progress == null || progress.status() == null) {
			return dbStatus;
		}

		try {
			return AudioStatus.valueOf(progress.status().toUpperCase());
		} catch (IllegalArgumentException e) {
			return dbStatus;
		}
	}

	default String mergedError(String dbError, AudioProgressModel progress) {
		if (progress == null || progress.error() == null || progress.error().isBlank()) {
			return dbError;
		}

		return progress.error();
	}
}
