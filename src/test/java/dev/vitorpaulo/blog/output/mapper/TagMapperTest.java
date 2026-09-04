package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.TagContentEntity;
import dev.vitorpaulo.blog.domain.TagEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagContentModel;
import dev.vitorpaulo.blog.model.TagModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TagMapperTest {

    private TagMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TagMapper.class);
    }

    @Test
    void toModel_shouldMapEntityToModel() {
        // Given
        TagEntity entity = new TagEntity();
        entity.setId(UUID.randomUUID());
        entity.setSlug("test-tag");

        TagContentEntity englishContent = new TagContentEntity();
        englishContent.setLanguage(Language.ENGLISH);
        englishContent.setName("Test Tag");
        englishContent.setDescription("Test Description");

        TagContentEntity portugueseContent = new TagContentEntity();
        portugueseContent.setLanguage(Language.PORTUGUESE);
        portugueseContent.setName("Tag de Teste");
        portugueseContent.setDescription("Descrição de Teste");

        List<TagContentEntity> contents = new ArrayList<>();
        contents.add(englishContent);
        contents.add(portugueseContent);
        entity.setContents(contents);

        // When
        TagModel model = mapper.toModel(entity);

        // Then
        assertNotNull(model);
        assertEquals(entity.getId(), model.id());
        assertEquals(entity.getSlug(), model.slug());

        Map<Language, TagContentModel> translations = model.translations();
        assertNotNull(translations);
        assertEquals(2, translations.size());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertTrue(translations.containsKey(Language.PORTUGUESE));
        assertEquals("Test Tag", translations.get(Language.ENGLISH).name());
        assertEquals("Test Description", translations.get(Language.ENGLISH).description());
        assertEquals("Tag de Teste", translations.get(Language.PORTUGUESE).name());
    }

    @Test
    void toEntity_shouldMapModelToEntity() {
        // Given
        Map<Language, TagContentModel> translations = new HashMap<>();
        translations.put(Language.ENGLISH, new TagContentModel("Test Tag", "Test Description"));

        TagModel model = new TagModel(
            UUID.randomUUID(),
            "test-tag",
            translations
        );

        // When
        TagEntity entity = mapper.toEntity(model);

        // Then
        assertNotNull(entity);
        assertEquals(model.id(), entity.getId());
        assertEquals(model.slug(), entity.getSlug());
    }

    @Test
    void toEntityList_shouldMapListOfModelsToMutableListOfEntities() {
        // Given
        List<TagModel> models = new ArrayList<>();
        
        Map<Language, TagContentModel> translations1 = new HashMap<>();
        translations1.put(Language.ENGLISH, new TagContentModel("Tag 1", "Description 1"));
        models.add(new TagModel(UUID.randomUUID(), "tag-1", translations1));
        
        Map<Language, TagContentModel> translations2 = new HashMap<>();
        translations2.put(Language.ENGLISH, new TagContentModel("Tag 2", "Description 2"));
        models.add(new TagModel(UUID.randomUUID(), "tag-2", translations2));

        // When
        List<TagEntity> entities = mapper.toEntityList(models);

        // Then
        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals("tag-1", entities.get(0).getSlug());
        assertEquals("tag-2", entities.get(1).getSlug());
        
        // Verify it's mutable (ArrayList, not immutable list)
        assertDoesNotThrow(() -> entities.clear());
    }

    @Test
    void toEntityList_withEmptyList_shouldReturnEmptyMutableList() {
        // Given
        List<TagModel> models = new ArrayList<>();

        // When
        List<TagEntity> entities = mapper.toEntityList(models);

        // Then
        assertNotNull(entities);
        assertEquals(0, entities.size());
        assertDoesNotThrow(() -> entities.clear());
    }
}
