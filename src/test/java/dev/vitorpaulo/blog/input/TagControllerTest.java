package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.input.mapper.TagInputMapper;
import dev.vitorpaulo.blog.input.request.CreateTagRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.input.request.TagQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateTagRequest;
import dev.vitorpaulo.blog.input.response.TagResponse;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.usecase.tag.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock private CreateTagUseCase createTagUseCase;
    @Mock private UpdateTagUseCase updateTagUseCase;
    @Mock private DeleteTagUseCase deleteTagUseCase;
    @Mock private GetTagByIdUseCase getTagByIdUseCase;
    @Mock private SearchTagUseCase searchTagUseCase;
    @Mock private TagInputMapper tagInputMapper;
    @Mock private AuthorModel author;
    @Mock private TagModel tagModel;
    @Mock private TagResponse tagResponse;
    @Mock private CreateTagRequest createRequest;
    @Mock private UpdateTagRequest updateRequest;
    @Mock private MassDeleteRequest massDeleteRequest;
    @Mock private GenericPageableRequest<TagQueryRequest> searchRequest;
    @Mock private PaginatedInput<TagQueryModel> paginatedInput;
    @Mock private PaginatedOutput<TagModel> paginatedOutput;
    @Mock private GenericPageableResponse<TagResponse> pageableResponse;

    @InjectMocks
    private TagController tagController;

    @Test
    void create_validRequest_returnsCreatedResponse() {
        when(tagInputMapper.toModel(createRequest)).thenReturn(tagModel);
        when(createTagUseCase.execute(tagModel)).thenReturn(tagModel);
        when(tagInputMapper.toResponse(tagModel)).thenReturn(tagResponse);

        var result = tagController.create(createRequest, author);

        assertEquals(tagResponse, result);
        verify(createTagUseCase).execute(tagModel);
    }

    @Test
    void update_validRequest_returnsUpdatedResponse() {
        var id = UUID.randomUUID();
        when(tagInputMapper.toModel(updateRequest, id)).thenReturn(tagModel);
        when(updateTagUseCase.execute(id, tagModel)).thenReturn(tagModel);
        when(tagInputMapper.toResponse(tagModel)).thenReturn(tagResponse);

        var result = tagController.update(id, updateRequest, author);

        assertEquals(tagResponse, result);
        verify(updateTagUseCase).execute(id, tagModel);
    }

    @Test
    void delete_validRequest_delegatesToUseCase() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(massDeleteRequest.ids()).thenReturn(ids);

        tagController.delete(massDeleteRequest, author);

        verify(deleteTagUseCase).execute(ids);
    }

    @Test
    void getById_found_returnsResponse() {
        var id = UUID.randomUUID();
        when(getTagByIdUseCase.execute(id)).thenReturn(tagModel);
        when(tagInputMapper.toResponse(tagModel)).thenReturn(tagResponse);

        var result = tagController.getById(id, author);

        assertEquals(tagResponse, result);
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(getTagByIdUseCase.execute(any())).thenThrow(new NotFoundException(ExceptionCode.TAG_NOT_FOUND));

        assertThrows(NotFoundException.class, () -> tagController.getById(UUID.randomUUID(), author));
    }

    @Test
    void search_validRequest_returnsPaginatedResponse() {
        var queryRequest = new TagQueryRequest("java", Language.ENGLISH);
        when(searchRequest.query()).thenReturn(queryRequest);
        when(tagInputMapper.toPageableInput(searchRequest)).thenReturn(paginatedInput);
        when(searchTagUseCase.execute(paginatedInput, Language.ENGLISH)).thenReturn(paginatedOutput);
        when(tagInputMapper.toPageableResponse(paginatedOutput)).thenReturn(pageableResponse);

        var result = tagController.search(searchRequest);

        assertEquals(pageableResponse, result);
    }

    @Test
    void search_nullQuery_passesNullLanguage() {
        when(searchRequest.query()).thenReturn(null);
        when(tagInputMapper.toPageableInput(searchRequest)).thenReturn(paginatedInput);
        when(searchTagUseCase.execute(paginatedInput, null)).thenReturn(paginatedOutput);
        when(tagInputMapper.toPageableResponse(paginatedOutput)).thenReturn(pageableResponse);

        var result = tagController.search(searchRequest);

        assertEquals(pageableResponse, result);
        verify(searchTagUseCase).execute(paginatedInput, null);
    }
}
