package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.input.mapper.ProjectInputMapper;
import dev.vitorpaulo.blog.input.request.CreateProjectRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.input.request.ProjectBatchRequest;
import dev.vitorpaulo.blog.input.request.ProjectQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateProjectRequest;
import dev.vitorpaulo.blog.input.response.ProjectResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.usecase.project.*;
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
class ProjectControllerTest {

    @Mock private CreateProjectUseCase createProjectUseCase;
    @Mock private UpdateProjectUseCase updateProjectUseCase;
    @Mock private DeleteProjectUseCase deleteProjectUseCase;
    @Mock private GetProjectByIdUseCase getProjectByIdUseCase;
    @Mock private GetProjectBySlugUseCase getProjectBySlugUseCase;
    @Mock private SearchProjectUseCase searchProjectUseCase;
    @Mock private ProjectInputMapper projectInputMapper;
    @Mock private ProjectOutput projectOutput;
    @Mock private AuthorModel author;
    @Mock private ProjectModel projectModel;
    @Mock private ProjectResponse projectResponse;
    @Mock private CreateProjectRequest createRequest;
    @Mock private UpdateProjectRequest updateRequest;
    @Mock private MassDeleteRequest massDeleteRequest;
    @Mock private GenericPageableRequest<ProjectQueryRequest> searchRequest;
    @Mock private PaginatedInput<ProjectQueryModel> paginatedInput;
    @Mock private PaginatedOutput<ProjectModel> paginatedOutput;
    @Mock private GenericPageableResponse<ProjectResponse> pageableResponse;
    @Mock private ProjectBatchRequest batchRequest;

    @InjectMocks
    private ProjectController projectController;

    @Test
    void create_validRequest_returnsCreatedResponse() {
        when(createRequest.tagIds()).thenReturn(null);
        when(projectInputMapper.toModel(createRequest)).thenReturn(projectModel);
        when(createProjectUseCase.execute(projectModel, null, author)).thenReturn(projectModel);
        when(projectInputMapper.toResponse(projectModel)).thenReturn(projectResponse);

        var result = projectController.create(createRequest, author);

        assertEquals(projectResponse, result);
        verify(createProjectUseCase).execute(projectModel, null, author);
    }

    @Test
    void update_validRequest_returnsUpdatedResponse() {
        var id = UUID.randomUUID();
        when(updateRequest.tagIds()).thenReturn(null);
        when(projectInputMapper.toModel(updateRequest, id)).thenReturn(projectModel);
        when(updateProjectUseCase.execute(projectModel, null, author)).thenReturn(projectModel);
        when(projectInputMapper.toResponse(projectModel)).thenReturn(projectResponse);

        var result = projectController.update(id, updateRequest, author);

        assertEquals(projectResponse, result);
        verify(updateProjectUseCase).execute(projectModel, null, author);
    }

    @Test
    void delete_validRequest_delegatesToUseCase() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(massDeleteRequest.ids()).thenReturn(ids);

        projectController.delete(massDeleteRequest, author);

        verify(deleteProjectUseCase).execute(ids, author);
    }

    @Test
    void getById_found_returnsResponse() {
        var id = UUID.randomUUID();
        when(getProjectByIdUseCase.execute(id)).thenReturn(projectModel);
        when(projectInputMapper.toResponse(projectModel)).thenReturn(projectResponse);

        var result = projectController.getById(id);

        assertEquals(projectResponse, result);
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(getProjectByIdUseCase.execute(any())).thenThrow(new NotFoundException(ExceptionCode.PROJECT_NOT_FOUND));

        assertThrows(NotFoundException.class, () -> projectController.getById(UUID.randomUUID()));
    }

    @Test
    void getBySlug_validSlug_returnsResponse() {
        when(getProjectBySlugUseCase.execute("my-project", Language.ENGLISH)).thenReturn(projectModel);
        when(projectInputMapper.toResponse(projectModel)).thenReturn(projectResponse);

        var result = projectController.getBySlug("my-project", Language.ENGLISH);

        assertEquals(projectResponse, result);
        verify(getProjectBySlugUseCase).execute("my-project", Language.ENGLISH);
    }

    @Test
    void search_validRequest_returnsPaginatedResponse() {
        when(projectInputMapper.toPageableInput(searchRequest)).thenReturn(paginatedInput);
        when(searchProjectUseCase.execute(paginatedInput, author)).thenReturn(paginatedOutput);
        when(projectInputMapper.toPageableResponse(paginatedOutput)).thenReturn(pageableResponse);

        var result = projectController.search(searchRequest, author);

        assertEquals(pageableResponse, result);
    }

    @Test
    void batch_validRequest_returnsList() {
        var ids = List.of(UUID.randomUUID());
        when(batchRequest.ids()).thenReturn(ids);
        when(batchRequest.language()).thenReturn(Language.ENGLISH);
        when(projectOutput.findAllById(ids, Language.ENGLISH)).thenReturn(List.of(projectModel));
        when(projectInputMapper.toResponse(projectModel)).thenReturn(projectResponse);

        var result = projectController.batch(batchRequest);

        assertEquals(1, result.size());
        assertEquals(projectResponse, result.getFirst());
    }
}
