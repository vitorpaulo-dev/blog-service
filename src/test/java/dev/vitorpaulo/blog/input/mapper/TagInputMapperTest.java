package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.TagContentRequest;
import dev.vitorpaulo.blog.input.request.TagQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateTagRequest;
import dev.vitorpaulo.blog.input.response.TagResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TagInputMapperTest {

    private final TagInputMapper mapper = Mappers.getMapper(TagInputMapper.class);

    @Test
    void toResponse_nullTag_returnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponse_validTag_returnsResponse() {
        var id = UUID.randomUUID();
        var contentModel = new TagContentModel("Java");
        var tag = new TagModel(id, "java", Map.of(Language.ENGLISH, contentModel));

        var result = mapper.toResponse(tag);

        assertEquals(id, result.id());
        assertEquals("java", result.slug());
        assertEquals(1, result.translations().size());
        assertEquals("Java", result.translations().get(Language.ENGLISH).name());
    }

    @Test
    void toResponse_nullTranslations_returnsEmptyMap() {
        var tag = new TagModel(UUID.randomUUID(), "slug", null);

        var result = mapper.toResponse(tag);

        assertTrue(result.translations().isEmpty());
    }

    @Test
    void toPageableResponse_nullResult_returnsNull() {
        assertNull(mapper.toPageableResponse(null));
    }

    @Test
    void toPageableResponse_validResult_returnsResponse() {
        var contentModel = new TagContentModel("Java");
        var tag = new TagModel(UUID.randomUUID(), "java", Map.of(Language.ENGLISH, contentModel));
        var paginatedOutput = new PaginatedOutput<>(List.of(tag), 0, 10, 1, 1);

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
        var queryRequest = new TagQueryRequest("java", Language.ENGLISH);
        var request = new GenericPageableRequest<>(queryRequest, 1, 20, "slug", Sort.Direction.ASC);

        var result = mapper.toPageableInput(request);

        assertNotNull(result);
        assertEquals("java", result.query().name());
        assertEquals(Language.ENGLISH, result.query().language());
        assertEquals(1, result.page());
        assertEquals(20, result.size());
        assertEquals("slug", result.sort());
        assertEquals(Sort.Direction.ASC, result.direction());
    }

    @Test
    void toPageableInput_nullQuery_returnsNullName() {
        var request = new GenericPageableRequest<TagQueryRequest>(null, 0, 10, "slug", Sort.Direction.DESC);

        var result = mapper.toPageableInput(request);

        assertNotNull(result);
        assertNull(result.query().name());
    }

    @Test
    void toPageableInput_nullPageDefaultsToZero() {
        GenericPageableRequest<TagQueryRequest> request = new GenericPageableRequest<>(null, null, null, null, null);

        var result = mapper.toPageableInput(request);

        assertEquals(0, result.page());
        assertEquals(10, result.size());
    }

    @Test
    void toModel_updateRequest_mapsId() {
        var id = UUID.randomUUID();
        var request = new UpdateTagRequest(
            Map.of(Language.ENGLISH, new TagContentRequest("Java"))
        );

        var result = mapper.toModel(request, id);

        assertEquals(id, result.id());
        assertEquals(1, result.translations().size());
        assertEquals("Java", result.translations().get(Language.ENGLISH).name());
    }
}
