package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.config.security.CurrentAuthor;
import dev.vitorpaulo.blog.input.mapper.TagInputMapper;
import dev.vitorpaulo.blog.input.request.CreateTagRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.input.request.TagQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateTagRequest;
import dev.vitorpaulo.blog.input.response.TagResponse;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.usecase.tag.CreateTagUseCase;
import dev.vitorpaulo.blog.usecase.tag.DeleteTagUseCase;
import dev.vitorpaulo.blog.usecase.tag.GetTagByIdUseCase;
import dev.vitorpaulo.blog.usecase.tag.SearchTagUseCase;
import dev.vitorpaulo.blog.usecase.tag.UpdateTagUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/tag")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final CreateTagUseCase createTagUseCase;
    private final UpdateTagUseCase updateTagUseCase;
    private final DeleteTagUseCase deleteTagUseCase;
    private final GetTagByIdUseCase getTagByIdUseCase;
    private final SearchTagUseCase searchTagUseCase;

    private final TagInputMapper tagInputMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse create(@Valid @RequestBody CreateTagRequest request, @CurrentAuthor AuthorModel author) {
        final var tag = createTagUseCase.execute(tagInputMapper.toModel(request));
        return tagInputMapper.toResponse(tag);
    }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateTagRequest request, @CurrentAuthor AuthorModel author) {
        final var tag = updateTagUseCase.execute(id, tagInputMapper.toModel(request, id));
        return tagInputMapper.toResponse(tag);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody MassDeleteRequest request, @CurrentAuthor AuthorModel author) {
        deleteTagUseCase.execute(request.ids());
    }

    @GetMapping("/{id}")
    public TagResponse getById(@PathVariable UUID id, @CurrentAuthor AuthorModel author) {
        return tagInputMapper.toResponse(getTagByIdUseCase.execute(id));
    }

    @PostMapping("/search")
    public GenericPageableResponse<TagResponse> search(@Valid @RequestBody GenericPageableRequest<TagQueryRequest> request) {
        final var language = request.query() != null ? request.query().language() : null;
        final var result = searchTagUseCase.execute(tagInputMapper.toPageableInput(request), language);
        return tagInputMapper.toPageableResponse(result);
    }
}
