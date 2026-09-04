package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.ProjectContentEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectContentModel;
import dev.vitorpaulo.blog.model.ProjectModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {

    private ProjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ProjectMapper.class);
    }

    @Test
    void toModel_shouldMapEntityToModel() {
        // Given
        ProjectEntity entity = new ProjectEntity();
        entity.setId(UUID.randomUUID());
        entity.setSlug("test-project");
        entity.setLogoUrl("https://example.com/logo.png");
        entity.setProgrammingLanguage("Java");

        ProjectContentEntity englishContent = new ProjectContentEntity();
        englishContent.setLanguage(Language.ENGLISH);
        englishContent.setTitle("Test Project");
        englishContent.setDescription("Test Description");

        ProjectContentEntity portugueseContent = new ProjectContentEntity();
        portugueseContent.setLanguage(Language.PORTUGUESE);
        portugueseContent.setTitle("Projeto de Teste");
        portugueseContent.setDescription("Descrição de Teste");

        List<ProjectContentEntity> contents = new ArrayList<>();
        contents.add(englishContent);
        contents.add(portugueseContent);
        entity.setContents(contents);

        // When
        ProjectModel model = mapper.toModel(entity);

        // Then
        assertNotNull(model);
        assertEquals(entity.getId(), model.id());
        assertEquals(entity.getSlug(), model.slug());
        assertEquals(entity.getLogoUrl(), model.logoUrl());
        assertEquals(entity.getProgrammingLanguage(), model.programmingLanguage());

        Map<Language, ProjectContentModel> translations = model.translations();
        assertNotNull(translations);
        assertEquals(2, translations.size());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertTrue(translations.containsKey(Language.PORTUGUESE));
        assertEquals("Test Project", translations.get(Language.ENGLISH).title());
        assertEquals("Test Description", translations.get(Language.ENGLISH).description());
        assertEquals("Projeto de Teste", translations.get(Language.PORTUGUESE).title());
    }

    @Test
    void toEntity_shouldMapModelToEntity() {
        // Given
        Map<Language, ProjectContentModel> translations = new HashMap<>();
        translations.put(Language.ENGLISH, new ProjectContentModel("Test Project", "Test Description"));

        ProjectModel model = new ProjectModel(
            UUID.randomUUID(),
            "test-project",
            "https://example.com/logo.png",
            "Java",
            translations
        );

        // When
        ProjectEntity entity = mapper.toEntity(model);

        // Then
        assertNotNull(entity);
        assertEquals(model.id(), entity.getId());
        assertEquals(model.slug(), entity.getSlug());
        assertEquals(model.logoUrl(), entity.getLogoUrl());
        assertEquals(model.programmingLanguage(), entity.getProgrammingLanguage());
    }

    @Test
    void toEntityList_shouldMapListOfModelsToMutableListOfEntities() {
        // Given
        List<ProjectModel> models = new ArrayList<>();
        
        Map<Language, ProjectContentModel> translations1 = new HashMap<>();
        translations1.put(Language.ENGLISH, new ProjectContentModel("Project 1", "Description 1"));
        models.add(new ProjectModel(UUID.randomUUID(), "project-1", null, "Java", translations1));
        
        Map<Language, ProjectContentModel> translations2 = new HashMap<>();
        translations2.put(Language.ENGLISH, new ProjectContentModel("Project 2", "Description 2"));
        models.add(new ProjectModel(UUID.randomUUID(), "project-2", null, "TypeScript", translations2));

        // When
        List<ProjectEntity> entities = mapper.toEntityList(models);

        // Then
        assertNotNull(entities);
        assertEquals(2, entities.size());
        assertEquals("project-1", entities.get(0).getSlug());
        assertEquals("project-2", entities.get(1).getSlug());
        
        // Verify it's mutable (ArrayList, not immutable list)
        assertDoesNotThrow(() -> entities.clear());
    }

    @Test
    void toEntityList_withEmptyList_shouldReturnEmptyMutableList() {
        // Given
        List<ProjectModel> models = new ArrayList<>();

        // When
        List<ProjectEntity> entities = mapper.toEntityList(models);

        // Then
        assertNotNull(entities);
        assertEquals(0, entities.size());
        assertDoesNotThrow(() -> entities.clear());
    }
}
