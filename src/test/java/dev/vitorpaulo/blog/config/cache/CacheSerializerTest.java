package dev.vitorpaulo.blog.config.cache;

import dev.vitorpaulo.blog.model.AudioStatus;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostContentModel;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.model.ProjectContentModel;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.model.ProjectStatus;
import dev.vitorpaulo.blog.model.TagContentModel;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CacheSerializerTest {

	private final RedisSerializer<Object> serializer = CacheConfig.valueSerializer();

	@Test
	void postModel_roundTrip_preservesConcreteTypesAndEquality() {
		var model = postModel();

		var restored = serializer.deserialize(serializer.serialize(model));

		assertInstanceOf(PostModel.class, restored);
		assertEquals(model, restored);
		assertInstanceOf(LinkedHashMap.class, ((PostModel) restored).audio());
		assertInstanceOf(OffsetDateTime.class, ((PostModel) restored).createdAt());
		assertInstanceOf(PostContentModel.class, ((PostModel) restored).translations().get(Language.ENGLISH));
	}

	@Test
	void paginatedOutputOfPostModel_roundTrip_preservesTypedContent() {
		var output = new PaginatedOutput<>(List.of(postModel()), 0, 10, 1, 1);

		var restored = serializer.deserialize(serializer.serialize(output));

		assertInstanceOf(PaginatedOutput.class, restored);
		assertEquals(output, restored);
		assertInstanceOf(PostModel.class, ((PaginatedOutput<?>) restored).content().getFirst());
	}

	@Test
	void projectModel_roundTrip_preservesConcreteTypesAndEquality() {
		var model = projectModel();

		var restored = serializer.deserialize(serializer.serialize(model));

		assertInstanceOf(ProjectModel.class, restored);
		assertEquals(model, restored);
		assertInstanceOf(ProjectContentModel.class, ((ProjectModel) restored).translations().get(Language.PORTUGUESE));
	}

	@Test
	void tagModel_roundTrip_preservesConcreteTypesAndEquality() {
		var model = tagModel();

		var restored = serializer.deserialize(serializer.serialize(model));

		assertInstanceOf(TagModel.class, restored);
		assertEquals(model, restored);
	}

	@Test
	void tagModelList_roundTrip_preservesTypedContent() {
		var models = new ArrayList<TagModel>();
		models.add(tagModel());
		models.add(tagModel());

		var restored = serializer.deserialize(serializer.serialize(models));

		assertInstanceOf(List.class, restored);
		assertEquals(models, restored);
		assertInstanceOf(TagModel.class, ((List<?>) restored).getFirst());
	}

	@Test
	void postModelWithImmutableCollections_roundTrip_preservesEquality() {
		var model = new PostModel(
			UUID.randomUUID(),
			"featured-post",
			"https://cdn.example.com/banner.png",
			PostStatus.PUBLISHED,
			600,
			OffsetDateTime.parse("2026-10-15T10:15:30Z"),
			OffsetDateTime.parse("2026-10-14T08:00:00Z"),
			List.of(new AuthorModel(UUID.randomUUID(), "user_featured", "Featured Author", "featured-author", null, "org:member")),
			List.of(),
			Collections.emptyList(),
			12L,
			1L,
			2L,
			3L,
			4L,
			10L,
			1,
			Map.of(Language.ENGLISH, new PostContentModel("Featured", "# Featured", "Featured summary")),
			Collections.emptyMap()
		);

		var restored = serializer.deserialize(serializer.serialize(model));

		assertInstanceOf(PostModel.class, restored);
		assertEquals(model, restored);
	}

	@Test
	void postModelFromStreamToList_roundTrip_preservesEquality() {
		var model = postModelFromStreamToList();

		var restored = serializer.deserialize(serializer.serialize(model));

		assertInstanceOf(PostModel.class, restored);
		assertEquals(model, restored);
	}

	private PostModel postModelFromStreamToList() {
		var authors = List.of("A", "B").stream()
			.map(name -> new AuthorModel(UUID.randomUUID(), "user_" + name, name, null, null, "org:admin"))
			.toList();
		return new PostModel(
			UUID.randomUUID(),
			"search-result",
			null,
			PostStatus.DRAFT,
			null,
			OffsetDateTime.parse("2026-01-01T00:00:00Z"),
			null,
			authors,
			authors.stream().map(AuthorModel::id).toList(),
			Collections.emptyList(),
			0L,
			0L,
			0L,
			0L,
			0L,
			0L,
			null,
			new HashMap<Language, PostContentModel>(),
			new LinkedHashMap<AudioType, Map<Language, AudioModel>>()
		);
	}

	private PostModel postModel() {
		var audio = new LinkedHashMap<AudioType, Map<Language, AudioModel>>();
		var narration = new LinkedHashMap<Language, AudioModel>();
		narration.put(Language.ENGLISH, new AudioModel(AudioType.NARRATION, Language.ENGLISH, AudioStatus.READY, "post/audio/1/narration.wav", null, 100));
		narration.put(Language.PORTUGUESE, new AudioModel(AudioType.NARRATION, Language.PORTUGUESE, AudioStatus.GENERATING, "post/audio/2/narration.wav", null, 42));
		audio.put(AudioType.NARRATION, narration);

		var translations = new HashMap<Language, PostContentModel>();
		translations.put(Language.ENGLISH, new PostContentModel("Hello", "# Hello world", "Summary"));
		translations.put(Language.PORTUGUESE, new PostContentModel("Ola", "# Ola mundo", "Resumo"));

		return new PostModel(
			UUID.randomUUID(),
			"my-post",
			"https://cdn.example.com/banner.png",
			PostStatus.PUBLISHED,
			420,
			OffsetDateTime.parse("2026-10-15T10:15:30Z"),
			OffsetDateTime.parse("2026-10-14T08:00:00Z"),
			List.of(new AuthorModel(UUID.randomUUID(), "user_1", "Author One", "author-one", "https://cdn.example.com/avatar.png", "org:admin")),
			List.of(UUID.randomUUID()),
			List.of(UUID.randomUUID()),
			10L,
			1L,
			2L,
			3L,
			4L,
			10L,
			1,
			translations,
			audio
		);
	}

	private ProjectModel projectModel() {
		var translations = new HashMap<Language, ProjectContentModel>();
		translations.put(Language.PORTUGUESE, new ProjectContentModel("Projeto", "Descricao", "Resumo"));

		return new ProjectModel(
			UUID.randomUUID(),
			"my-project",
			"https://cdn.example.com/logo.png",
			"https://cdn.example.com/banner.png",
			"https://github.com/vitorpaulo-dev/my-project",
			"https://my-project.example.com",
			ProjectStatus.PUBLISHED,
			OffsetDateTime.parse("2026-09-01T12:00:00Z"),
			OffsetDateTime.parse("2026-09-10T12:00:00Z"),
			List.of(new AuthorModel(UUID.randomUUID(), "user_2", "Author Two", "author-two", null, "org:member")),
			List.of(UUID.randomUUID()),
			7L,
			1L,
			1L,
			1L,
			1L,
			4L,
			translations
		);
	}

	private TagModel tagModel() {
		var translations = new HashMap<Language, TagContentModel>();
		translations.put(Language.ENGLISH, new TagContentModel("spring"));

		return new TagModel(UUID.randomUUID(), "spring", translations);
	}
}
