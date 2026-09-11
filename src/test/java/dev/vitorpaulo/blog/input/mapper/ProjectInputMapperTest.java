package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.ProjectContentRequest;
import dev.vitorpaulo.blog.input.request.ProjectQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateProjectRequest;
import dev.vitorpaulo.blog.input.response.ProjectResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProjectInputMapperTest {

    private final ProjectInputMapper mapper = Mappers.getMapper(ProjectInputMapper.class);

    @Test
    void toResponse_nullProject_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponse_validProject_returnsResponse() {
        var id = UUID.randomUUID();
        var authorModel = new AuthorModel(
            UUID.randomUUID(), "clerk1", "John", "john", "avatar.png",
            "org:admin", Map.of()
        );
        var contentModel = new ProjectContentModel("Title", "Description");
        var project = new ProjectModel(
            id, "my-project", "logo.png", "banner.png",
            "https://github.com", "https://website.com",
            ProjectStatus.PUBLISHED, OffsetDateTime.now(), OffsetDateTime.now(),
            List.of(authorModel), List.of(), 10L, 5L, 3L, 2L, 1L, 11L,
            Map.of(Language.ENGLISH, contentModel)
        );

        var result = mapper.toResponse(project);

        assertEquals(id, result.id());
        assertEquals("my-project", result.slug());
        assertEquals("logo.png", result.logoUrl());
        assertEquals("banner.png", result.bannerUrl());
        assertEquals("https://github.com", result.githubUrl());
        assertEquals("https://website.com", result.websiteUrl());
        assertEquals("PUBLISHED", result.status());
        assertEquals(10L, result.viewCount());
        assertEquals(5L, result.loveCount());
        assertEquals(3L, result.celebrateCount());
        assertEquals(2L, result.geniusCount());
        assertEquals(1L, result.helpCount());
        assertEquals(11L, result.reactionCount());
        assertEquals(1, result.authors().size());
        assertEquals("John", result.authors().getFirst().name());
        assertTrue(result.tags().isEmpty());
        assertEquals(1, result.translations().size());
        assertEquals("Title", result.translations().get(Language.ENGLISH).title());
    }

    @Test
    void toResponse_nullStatus_returnsNullStatus() {
        var project = new ProjectModel(
            UUID.randomUUID(), "slug", null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, Map.of()
        );

        var result = mapper.toResponse(project);

        assertNull(result.status());
    }

    @Test
    void toResponse_nullTranslations_returnsEmptyMap() {
        var project = new ProjectModel(
            UUID.randomUUID(), "slug", null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null
        );

        var result = mapper.toResponse(project);

        assertTrue(result.translations().isEmpty());
    }

    @Test
    void toResponse_nullAuthors_returnsEmptyList() {
        var project = new ProjectModel(
            UUID.randomUUID(), "slug", null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, Map.of()
        );

        var result = mapper.toResponse(project);

        assertTrue(result.authors().isEmpty());
    }

    @Test
    void toPageableResponse_nullResult_returnsNull() {
        assertNull(mapper.toPageableResponse(null));
    }

    @Test
    void toPageableResponse_validResult_returnsResponse() {
        var contentModel = new ProjectContentModel("Title", "Desc");
        var project = new ProjectModel(
            UUID.randomUUID(), "slug", null, null, null, null,
            ProjectStatus.DRAFT, null, null, List.of(), List.of(), 0L, 0L, 0L, 0L, 0L, 0L,
            Map.of(Language.ENGLISH, contentModel)
        );
        var paginatedOutput = new PaginatedOutput<>(List.of(project), 0, 10, 1, 1);

        var result = mapper.toPageableResponse(paginatedOutput);

        assertEquals(1, result.content().size());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalPages());
        assertEquals(1L, result.totalElements());
    }

    @Test
    void toPageableInput_nullRequest_returnsNull() {
        assertNull(mapper.toPageableInput(null));
    }

    @Test
    void toPageableInput_validRequest_returnsInput() {
        var tagId = UUID.randomUUID();
        var queryRequest = new ProjectQueryRequest("search", UUID.randomUUID(), Language.ENGLISH, tagId);
        var request = new GenericPageableRequest<>(queryRequest, 1, 20, "createdAt", Sort.Direction.DESC);

        var result = mapper.toPageableInput(request);

        assertNotNull(result);
        assertEquals("search", result.query().query());
        assertEquals(1, result.page());
        assertEquals(20, result.size());
        assertEquals("createdAt", result.sort());
        assertEquals(Sort.Direction.DESC, result.direction());
        assertEquals(Language.ENGLISH, result.query().language());
        assertEquals(tagId, result.query().tagId());
    }

    @Test
    void toPageableInput_nullQuery_returnsNullQueryFields() {
        var request = new GenericPageableRequest<ProjectQueryRequest>(null, 0, 10, "createdAt", Sort.Direction.ASC);

        var result = mapper.toPageableInput(request);

        assertNotNull(result);
        assertNull(result.query().query());
        assertNull(result.query().authorId());
        assertNull(result.query().language());
        assertNull(result.query().tagId());
    }

    @Test
    void toPageableInput_nullPageDefaultsToZero() {
        GenericPageableRequest<ProjectQueryRequest> request = new GenericPageableRequest<>(null, null, null, null, null);

        var result = mapper.toPageableInput(request);

        assertEquals(0, result.page());
        assertEquals(10, result.size());
    }

    @Test
    void toModel_updateRequest_mapsId() {
        var id = UUID.randomUUID();
        var tagIds = List.of(UUID.randomUUID());
        var request = new UpdateProjectRequest(
            "logo.png", "banner.png", "github", "website", tagIds,
            Map.of(Language.ENGLISH, new ProjectContentRequest("Title", "Desc")),
            ProjectStatus.PUBLISHED
        );

        var result = mapper.toModel(request, id);

        assertEquals(id, result.id());
        assertEquals("logo.png", result.logoUrl());
        assertEquals("banner.png", result.bannerUrl());
        assertEquals("github", result.githubUrl());
        assertEquals("website", result.websiteUrl());
        assertEquals(ProjectStatus.PUBLISHED, result.status());
        assertEquals(1, result.translations().size());
        assertEquals("Title", result.translations().get(Language.ENGLISH).title());
    }
}
