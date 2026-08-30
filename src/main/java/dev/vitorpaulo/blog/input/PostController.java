package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.usecase.post.CreatePostUseCase;
import dev.vitorpaulo.blog.usecase.post.DeletePostUseCase;
import dev.vitorpaulo.blog.usecase.post.GetPostByIdUseCase;
import dev.vitorpaulo.blog.usecase.post.GetPostBySlugUseCase;
import dev.vitorpaulo.blog.usecase.post.MassDeletePostUseCase;
import dev.vitorpaulo.blog.usecase.post.SearchPostUseCase;
import dev.vitorpaulo.blog.usecase.post.UpdatePostUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final MassDeletePostUseCase massDeletePostUseCase;
    private final GetPostByIdUseCase getPostByIdUseCase;
    private final GetPostBySlugUseCase getPostBySlugUseCase;
    private final SearchPostUseCase searchPostUseCase;

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody CreatePostRequest request) {
        PostResponse response = createPostUseCase.execute(
                request.title(), request.bannerUrl(), request.content(), request.language(),
                request.tagIds(), request.projectIds(), request.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdatePostRequest request) {
        PostResponse response = updatePostUseCase.execute(
                id, request.title(), request.bannerUrl(), request.content(), request.language(),
                request.tagIds(), request.projectIds(), request.status());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePostUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> massDelete(@Valid @RequestBody MassDeleteRequest request) {
        massDeletePostUseCase.execute(request.ids());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        PostResponse response = getPostByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> getBySlug(@PathVariable String slug) {
        PostResponse response = getPostBySlugUseCase.execute(slug);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<GenericPageableResponse<PostResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(defaultValue = "createdAt") String sort) {
        GenericPageableResponse<PostResponse> response = searchPostUseCase.execute(query, page, limit, sort);
        return ResponseEntity.ok(response);
    }
}
