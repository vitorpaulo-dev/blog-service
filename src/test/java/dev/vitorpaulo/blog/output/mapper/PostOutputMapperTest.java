package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostContentModel;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PostOutputMapperTest {

    private PostOutputMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PostOutputMapper.class);
    }

    @Test
    void toModel_shouldMapEntityToModel() {
        // Given
        PostEntity entity = new PostEntity();
        entity.setId(UUID.randomUUID());
        entity.setSlug("test-post");
        entity.setBannerUrl("https://example.com/banner.jpg");
        entity.setStatus(PostStatus.PUBLISHED);
        entity.setEstimatedReading(5L);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setViewCount(100L);
        entity.setLoveCount(10L);
        entity.setCelebrateCount(5L);
        entity.setGeniusCount(3L);
        entity.setHelpCount(2L);

        PostContentEntity englishContent = new PostContentEntity();
        englishContent.setId(UUID.randomUUID());
        englishContent.setLanguage(Language.ENGLISH);
        englishContent.setTitle("Test Title");
        englishContent.setContent("Test Content");

        PostContentEntity portugueseContent = new PostContentEntity();
        portugueseContent.setId(UUID.randomUUID());
        portugueseContent.setLanguage(Language.PORTUGUESE);
        portugueseContent.setTitle("Título de Teste");
        portugueseContent.setContent("Conteúdo de Teste");

        List<PostContentEntity> contents = new ArrayList<>();
        contents.add(englishContent);
        contents.add(portugueseContent);
        entity.setContents(contents);

        // When
        PostModel model = mapper.toModel(entity);

        // Then
        assertNotNull(model);
        assertEquals(entity.getId(), model.id());
        assertEquals(entity.getSlug(), model.slug());
        assertEquals(entity.getBannerUrl(), model.bannerUrl());
        assertEquals(entity.getStatus(), model.status());
        assertEquals(Integer.valueOf(5), model.estimatedReading());
        assertEquals(entity.getViewCount(), model.viewCount());
        assertEquals(entity.getLoveCount(), model.loveCount());
        assertEquals(entity.getCelebrateCount(), model.celebrateCount());
        assertEquals(entity.getGeniusCount(), model.geniusCount());
        assertEquals(entity.getHelpCount(), model.helpCount());

        Map<Language, PostContentModel> translations = model.translations();
        assertNotNull(translations);
        assertEquals(2, translations.size());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertTrue(translations.containsKey(Language.PORTUGUESE));
        assertEquals("Test Title", translations.get(Language.ENGLISH).title());
        assertEquals("Test Content", translations.get(Language.ENGLISH).content());
        assertEquals("Título de Teste", translations.get(Language.PORTUGUESE).title());
        assertEquals("Conteúdo de Teste", translations.get(Language.PORTUGUESE).content());
    }

    @Test
    void toModel_withLanguage_shouldFilterByLanguage() {
        // Given
        PostEntity entity = new PostEntity();
        entity.setId(UUID.randomUUID());
        entity.setSlug("test-post");
        entity.setBannerUrl("https://example.com/banner.jpg");
        entity.setStatus(PostStatus.PUBLISHED);
        entity.setEstimatedReading(5L);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        entity.setViewCount(100L);
        entity.setLoveCount(10L);
        entity.setCelebrateCount(5L);
        entity.setGeniusCount(3L);
        entity.setHelpCount(2L);

        PostContentEntity englishContent = new PostContentEntity();
        englishContent.setId(UUID.randomUUID());
        englishContent.setLanguage(Language.ENGLISH);
        englishContent.setTitle("Test Title");
        englishContent.setContent("Test Content");

        PostContentEntity portugueseContent = new PostContentEntity();
        portugueseContent.setId(UUID.randomUUID());
        portugueseContent.setLanguage(Language.PORTUGUESE);
        portugueseContent.setTitle("Título de Teste");
        portugueseContent.setContent("Conteúdo de Teste");

        List<PostContentEntity> contents = new ArrayList<>();
        contents.add(englishContent);
        contents.add(portugueseContent);
        entity.setContents(contents);

        // When
        PostModel model = mapper.toModel(entity, Language.ENGLISH);

        // Then
        assertNotNull(model);
        assertEquals(entity.getId(), model.id());
        assertEquals(entity.getSlug(), model.slug());

        Map<Language, PostContentModel> translations = model.translations();
        assertNotNull(translations);
        assertEquals(1, translations.size());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertFalse(translations.containsKey(Language.PORTUGUESE));
        assertEquals("Test Title", translations.get(Language.ENGLISH).title());
        assertEquals("Test Content", translations.get(Language.ENGLISH).content());
    }

    @Test
    void toContentModel_shouldMapContentEntityToModel() {
        // Given
        PostContentEntity entity = new PostContentEntity();
        entity.setId(UUID.randomUUID());
        entity.setLanguage(Language.ENGLISH);
        entity.setTitle("Test Title");
        entity.setContent("Test Content");

        // When
        PostContentModel model = mapper.toContentModel(entity);

        // Then
        assertNotNull(model);
        assertEquals(entity.getTitle(), model.title());
        assertEquals(entity.getContent(), model.content());
    }

    @Test
    void contentsToTranslations_shouldConvertListToMap() {
        // Given
        PostContentEntity englishContent = new PostContentEntity();
        englishContent.setLanguage(Language.ENGLISH);
        englishContent.setTitle("English Title");
        englishContent.setContent("English Content");

        PostContentEntity portugueseContent = new PostContentEntity();
        portugueseContent.setLanguage(Language.PORTUGUESE);
        portugueseContent.setTitle("Portuguese Title");
        portugueseContent.setContent("Portuguese Content");

        List<PostContentEntity> contents = new ArrayList<>();
        contents.add(englishContent);
        contents.add(portugueseContent);

        // When
        Map<Language, PostContentModel> translations = mapper.contentsToTranslations(contents);

        // Then
        assertNotNull(translations);
        assertEquals(2, translations.size());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertTrue(translations.containsKey(Language.PORTUGUESE));
        assertEquals("English Title", translations.get(Language.ENGLISH).title());
        assertEquals("Portuguese Title", translations.get(Language.PORTUGUESE).title());
    }
}
