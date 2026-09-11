package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PostOutputMapperTest {

    private final PostOutputMapper mapper = Mappers.getMapper(PostOutputMapper.class);

    @Test
    void toModel_withProjectIds_mapsProjectIds() {
        var content = new PostContentEntity();
        content.setLanguage(Language.ENGLISH);
        content.setTitle("Title");
        content.setContent("Content");

        var entity = PostEntity.builder()
            .id(UUID.randomUUID())
            .slug("my-post")
            .bannerUrl("banner.png")
            .status(PostStatus.PUBLISHED)
            .contents(new ArrayList<>(List.of(content)))
            .viewCount(10L)
            .loveCount(5L)
            .celebrateCount(3L)
            .geniusCount(2L)
            .helpCount(1L)
            .build();

        var projectIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        var result = mapper.toModel(entity, projectIds);

        assertEquals(projectIds, result.projectIds());
        assertEquals(2, result.projectIds().size());
    }

    @Test
    void toModel_withoutProjectIds_returnsEmptyProjectIds() {
        var content = new PostContentEntity();
        content.setLanguage(Language.ENGLISH);
        content.setTitle("Title");
        content.setContent("Content");

        var entity = PostEntity.builder()
            .id(UUID.randomUUID())
            .slug("my-post")
            .status(PostStatus.DRAFT)
            .contents(new ArrayList<>(List.of(content)))
            .build();

        var result = mapper.toModel(entity);

        assertNotNull(result.projectIds());
        assertTrue(result.projectIds().isEmpty());
    }

    @Test
    void toModel_mapsAllFields() {
        var id = UUID.randomUUID();
        var content = new PostContentEntity();
        content.setLanguage(Language.ENGLISH);
        content.setTitle("Title");
        content.setContent("Content");

        var entity = PostEntity.builder()
            .id(id)
            .slug("my-post")
            .bannerUrl("banner.png")
            .status(PostStatus.PUBLISHED)
            .contents(new ArrayList<>(List.of(content)))
            .viewCount(10L)
            .loveCount(5L)
            .celebrateCount(3L)
            .geniusCount(2L)
            .helpCount(1L)
            .build();

        var result = mapper.toModel(entity, List.of());

        assertEquals(id, result.id());
        assertEquals("my-post", result.slug());
        assertEquals("banner.png", result.bannerUrl());
        assertEquals(PostStatus.PUBLISHED, result.status());
        assertEquals(10L, result.viewCount());
        assertEquals(5L, result.loveCount());
        assertEquals(3L, result.celebrateCount());
        assertEquals(2L, result.geniusCount());
        assertEquals(1L, result.helpCount());
        assertEquals(1, result.translations().size());
        assertEquals("Title", result.translations().get(Language.ENGLISH).title());
        assertEquals("Content", result.translations().get(Language.ENGLISH).content());
    }

    @Test
    void toModel_multipleContents_returnsAllTranslations() {
        var enContent = new PostContentEntity();
        enContent.setLanguage(Language.ENGLISH);
        enContent.setTitle("English Title");
        enContent.setContent("English Content");

        var ptContent = new PostContentEntity();
        ptContent.setLanguage(Language.PORTUGUESE);
        ptContent.setTitle("Portuguese Title");
        ptContent.setContent("Portuguese Content");

        var entity = PostEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>(List.of(enContent, ptContent)))
            .status(PostStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity, List.of());

        assertEquals(2, result.translations().size());
        assertTrue(result.translations().containsKey(Language.ENGLISH));
        assertTrue(result.translations().containsKey(Language.PORTUGUESE));
        assertEquals("English Title", result.translations().get(Language.ENGLISH).title());
        assertEquals("Portuguese Title", result.translations().get(Language.PORTUGUESE).title());
    }

    @Test
    void contentsToTranslations_nullContents_returnsEmptyMap() {
        var result = mapper.contentsToTranslations(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void contentsToTranslations_emptyContents_returnsEmptyMap() {
        var result = mapper.contentsToTranslations(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void contentsToTranslations_withContents_returnsMap() {
        var content = new PostContentEntity();
        content.setLanguage(Language.ENGLISH);
        content.setTitle("Title");
        content.setContent("Content");

        var result = mapper.contentsToTranslations(List.of(content));

        assertEquals(1, result.size());
        assertEquals("Title", result.get(Language.ENGLISH).title());
        assertEquals("Content", result.get(Language.ENGLISH).content());
    }

    @Test
    void toContentEntity_fromModel_mapsFields() {
        var model = new PostContentModel("Title", "Content");

        var result = mapper.toContentEntity(model);

        assertEquals("Title", result.getTitle());
        assertEquals("Content", result.getContent());
    }

    @Test
    void toModel_withLanguage_filtersToRequestedLanguage() {
        var enContent = new PostContentEntity();
        enContent.setLanguage(Language.ENGLISH);
        enContent.setTitle("English Title");
        enContent.setContent("English Content");

        var ptContent = new PostContentEntity();
        ptContent.setLanguage(Language.PORTUGUESE);
        ptContent.setTitle("Titulo");
        ptContent.setContent("Conteudo");

        var entity = PostEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>(List.of(enContent, ptContent)))
            .status(PostStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity, Language.PORTUGUESE, List.of());

        assertEquals(1, result.translations().size());
        assertTrue(result.translations().containsKey(Language.PORTUGUESE));
        assertEquals("Titulo", result.translations().get(Language.PORTUGUESE).title());
    }

    @Test
    void toModel_withLanguage_fallsBackToEnglish() {
        var enContent = new PostContentEntity();
        enContent.setLanguage(Language.ENGLISH);
        enContent.setTitle("English Title");
        enContent.setContent("English Content");

        var entity = PostEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>(List.of(enContent)))
            .status(PostStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity, Language.PORTUGUESE, List.of());

        assertEquals(1, result.translations().size());
        assertTrue(result.translations().containsKey(Language.ENGLISH));
    }

    @Test
    void toModel_withNullLanguage_returnsAllTranslations() {
        var enContent = new PostContentEntity();
        enContent.setLanguage(Language.ENGLISH);
        enContent.setTitle("English Title");
        enContent.setContent("English Content");

        var ptContent = new PostContentEntity();
        ptContent.setLanguage(Language.PORTUGUESE);
        ptContent.setTitle("Titulo");
        ptContent.setContent("Conteudo");

        var entity = PostEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>(List.of(enContent, ptContent)))
            .status(PostStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity, null, List.of());

        assertEquals(2, result.translations().size());
    }
}
