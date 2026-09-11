package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.config.security.CurrentAuthor;
import dev.vitorpaulo.blog.input.mapper.ProjectInputMapper;
import dev.vitorpaulo.blog.input.request.CreateProjectRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.input.request.ProjectBatchRequest;
import dev.vitorpaulo.blog.input.request.ProjectQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateProjectRequest;
import dev.vitorpaulo.blog.input.response.ProjectResponse;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.usecase.project.CreateProjectUseCase;
import dev.vitorpaulo.blog.usecase.project.DeleteProjectUseCase;
import dev.vitorpaulo.blog.usecase.project.GetProjectByIdUseCase;
import dev.vitorpaulo.blog.usecase.project.GetProjectBySlugUseCase;
import dev.vitorpaulo.blog.usecase.project.SearchProjectUseCase;
import dev.vitorpaulo.blog.usecase.project.UpdateProjectUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/project")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final CreateProjectUseCase createProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final DeleteProjectUseCase deleteProjectUseCase;
    private final GetProjectByIdUseCase getProjectByIdUseCase;
    private final GetProjectBySlugUseCase getProjectBySlugUseCase;
    private final SearchProjectUseCase searchProjectUseCase;

    private final ProjectInputMapper projectInputMapper;
    private final ProjectOutput projectOutput;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request, @CurrentAuthor AuthorModel author) {
        final var project = createProjectUseCase.execute(projectInputMapper.toModel(request), author);
        return projectInputMapper.toResponse(project);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateProjectRequest request, @CurrentAuthor AuthorModel author) {
        final var project = updateProjectUseCase.execute(projectInputMapper.toModel(request, id), author);
        return projectInputMapper.toResponse(project);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody MassDeleteRequest request, @CurrentAuthor AuthorModel author) {
        deleteProjectUseCase.execute(request.ids(), author);
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable UUID id) {
        return projectInputMapper.toResponse(getProjectByIdUseCase.execute(id));
    }

    @GetMapping("/slug/{slug}/{language}")
    public ProjectResponse getBySlug(@PathVariable String slug, @PathVariable Language language) {
        return projectInputMapper.toResponse(getProjectBySlugUseCase.execute(slug, language));
    }

    @PostMapping("/search")
    public GenericPageableResponse<ProjectResponse> search(@Valid @RequestBody GenericPageableRequest<ProjectQueryRequest> request, @CurrentAuthor AuthorModel author) {
        final var result = searchProjectUseCase.execute(projectInputMapper.toPageableInput(request), author);
        return projectInputMapper.toPageableResponse(result);
    }

    @PostMapping("/batch")
    public List<ProjectResponse> batch(@RequestBody @Valid ProjectBatchRequest request) {
        return projectOutput.findAllById(request.ids(), request.language()).stream()
            .map(projectInputMapper::toResponse)
            .toList();
    }
}
