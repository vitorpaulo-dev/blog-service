package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.ProjectContentEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProjectMapperTest {

    private final ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

    @Test
    void toModel_entity_mapsAllFields() {
        var id = UUID.randomUUID();
        var content = new ProjectContentEntity();
        content.setLanguage(Language.ENGLISH);
        content.setTitle("Title");
        content.setDescription("Description");

        var entity = ProjectEntity.builder()
            .id(id)
            .slug("my-project")
            .logoUrl("logo.png")
            .programmingLanguage("Java")
            .bannerUrl("banner.png")
            .githubUrl("github")
            .websiteUrl("website")
            .status(ProjectStatus.PUBLISHED)
            .contents(new ArrayList<>(List.of(content)))
            .viewCount(10L)
            .loveCount(5L)
            .celebrateCount(3L)
            .geniusCount(2L)
            .helpCount(1L)
            .build();

        var result = mapper.toModel(entity);

        assertEquals(id, result.id());
        assertEquals("my-project", result.slug());
        assertEquals("logo.png", result.logoUrl());
        assertEquals("Java", result.programmingLanguage());
        assertEquals("banner.png", result.bannerUrl());
        assertEquals("github", result.githubUrl());
        assertEquals("website", result.websiteUrl());
        assertEquals(ProjectStatus.PUBLISHED, result.status());
        assertEquals(10L, result.viewCount());
        assertEquals(5L, result.loveCount());
        assertEquals(3L, result.celebrateCount());
        assertEquals(2L, result.geniusCount());
        assertEquals(1L, result.helpCount());
        assertEquals(1, result.translations().size());
        assertEquals("Title", result.translations().get(Language.ENGLISH).title());
    }

    @Test
    void toModel_entityWithLanguage_filtersTranslations() {
        var enContent = new ProjectContentEntity();
        enContent.setLanguage(Language.ENGLISH);
        enContent.setTitle("English Title");
        enContent.setDescription("English Desc");

        var ptContent = new ProjectContentEntity();
        ptContent.setLanguage(Language.PORTUGUESE);
        ptContent.setTitle("Portuguese Title");
        ptContent.setDescription("Portuguese Desc");

        var entity = ProjectEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>(List.of(enContent, ptContent)))
            .status(ProjectStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity, Language.PORTUGUESE);

        assertEquals(1, result.translations().size());
        assertTrue(result.translations().containsKey(Language.PORTUGUESE));
        assertEquals("Portuguese Title", result.translations().get(Language.PORTUGUESE).title());
    }

    @Test
    void toModel_entityWithLanguage_fallsBackToEnglish() {
        var enContent = new ProjectContentEntity();
        enContent.setLanguage(Language.ENGLISH);
        enContent.setTitle("English Title");
        enContent.setDescription("English Desc");

        var entity = ProjectEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>(List.of(enContent)))
            .status(ProjectStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity, Language.PORTUGUESE);

        assertEquals(1, result.translations().size());
        assertTrue(result.translations().containsKey(Language.ENGLISH));
    }

    @Test
    void toModel_entityWithLanguage_fallsBackToFirstAvailable() {
        var ptContent = new ProjectContentEntity();
        ptContent.setLanguage(Language.PORTUGUESE);
        ptContent.setTitle("Portuguese Title");
        ptContent.setDescription("Portuguese Desc");

        var entity = ProjectEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>(List.of(ptContent)))
            .status(ProjectStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity, Language.ENGLISH);

        assertEquals(1, result.translations().size());
        assertTrue(result.translations().containsKey(Language.PORTUGUESE));
    }

    @Test
    void toModel_nullLanguage_returnsAllTranslations() {
        var enContent = new ProjectContentEntity();
        enContent.setLanguage(Language.ENGLISH);
        enContent.setTitle("English");

        var entity = ProjectEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>(List.of(enContent)))
            .status(ProjectStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity, null);

        assertEquals(1, result.translations().size());
    }

    @Test
    void toModel_emptyContents_returnsEmptyTranslations() {
        var entity = ProjectEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(new ArrayList<>())
            .status(ProjectStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity);

        assertTrue(result.translations().isEmpty());
    }

    @Test
    void toModel_nullContents_returnsEmptyTranslations() {
        var entity = ProjectEntity.builder()
            .id(UUID.randomUUID())
            .slug("slug")
            .contents(null)
            .status(ProjectStatus.DRAFT)
            .build();

        var result = mapper.toModel(entity);

        assertTrue(result.translations().isEmpty());
    }

    @Test
    void updateEntity_mapsFields() {
        var entity = ProjectEntity.builder()
            .id(UUID.randomUUID())
            .slug("old-slug")
            .logoUrl("old-logo.png")
            .programmingLanguage("Python")
            .bannerUrl("old-banner.png")
            .githubUrl("old-github")
            .websiteUrl("old-website")
            .status(ProjectStatus.DRAFT)
            .viewCount(100L)
            .loveCount(50L)
            .build();

        var model = new ProjectModel(
            UUID.randomUUID(), "new-slug", "new-logo.png", "Java",
            "new-banner.png", "new-github", "new-website",
            ProjectStatus.PUBLISHED, null, null, null,
            200L, 100L, 50L, 25L, 10L, 185L, Map.of()
        );

        mapper.updateEntity(model, entity);

        assertEquals("new-logo.png", entity.getLogoUrl());
        assertEquals("Java", entity.getProgrammingLanguage());
        assertEquals("new-banner.png", entity.getBannerUrl());
        assertEquals("new-github", entity.getGithubUrl());
        assertEquals("new-website", entity.getWebsiteUrl());
        assertEquals(ProjectStatus.PUBLISHED, entity.getStatus());
        assertEquals("old-slug", entity.getSlug());
        assertEquals(100L, entity.getViewCount());
        assertEquals(50L, entity.getLoveCount());
    }

    @Test
    void toContentEntity_mapsFields() {
        var model = new ProjectContentModel("Title", "Description");

        var result = mapper.toContentEntity(model);

        assertEquals("Title", result.getTitle());
        assertEquals("Description", result.getDescription());
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
        var content = new ProjectContentEntity();
        content.setLanguage(Language.ENGLISH);
        content.setTitle("Title");
        content.setDescription("Desc");

        var result = mapper.contentsToTranslations(List.of(content));

        assertEquals(1, result.size());
        assertEquals("Title", result.get(Language.ENGLISH).title());
    }
}
