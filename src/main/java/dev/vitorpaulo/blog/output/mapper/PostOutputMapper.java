package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostContentModel;
import dev.vitorpaulo.blog.model.PostModel;
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
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
	uses = { ProjectMapper.class }
)
public interface PostOutputMapper {

	@Mapping(target = "translations", expression = "java(contentsToTranslations(entity.getContents()))")
	@Mapping(target = "projectIds", expression = "java(java.util.List.of())")
	PostModel toModel(PostEntity entity);

	default PostModel toModel(PostEntity entity, List<UUID> projectIds) {
		PostModel model = toModel(entity);
		if (model == null) return null;
		return withProjectIds(model, projectIds);
	}

	default PostModel toModel(PostEntity entity, Language language, List<UUID> projectIds) {
		PostModel model = toModel(entity, projectIds);
		if (model == null || language == null) return model;
		return filterTranslations(model, language);
	}

	private PostModel withProjectIds(PostModel model, List<UUID> projectIds) {
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
			projectIds != null ? projectIds : List.of(),
			model.viewCount(),
			model.loveCount(),
			model.celebrateCount(),
			model.geniusCount(),
			model.helpCount(),
			model.reactionCount(),
			model.translations()
		);
	}

	private PostModel filterTranslations(PostModel model, Language language) {
		var filtered = filterTranslationMap(model.translations(), language);
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
			model.projectIds(),
			model.viewCount(),
			model.loveCount(),
			model.celebrateCount(),
			model.geniusCount(),
			model.helpCount(),
			model.reactionCount(),
			filtered
		);
	}

	private <T> Map<Language, T> filterTranslationMap(Map<Language, T> translations, Language requested) {
		if (translations == null || translations.isEmpty()) return Map.of();
		if (translations.containsKey(requested)) {
			return Map.of(requested, translations.get(requested));
		}
		if (translations.containsKey(Language.ENGLISH)) {
			return Map.of(Language.ENGLISH, translations.get(Language.ENGLISH));
		}
		return translations.entrySet().stream()
			.findFirst()
			.map(e -> Map.of(e.getKey(), e.getValue()))
			.orElse(Map.of());
	}

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

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "name", ignore = true)
	@Mapping(target = "avatarUrl", ignore = true)
	@Mapping(target = "contents", ignore = true)
	dev.vitorpaulo.blog.domain.AuthorEntity toAuthorEntity(AuthorModel model);
}
