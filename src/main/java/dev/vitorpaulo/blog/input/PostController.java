package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.config.captcha.ValidateCaptcha;
import dev.vitorpaulo.blog.config.security.CurrentAuthor;
import dev.vitorpaulo.blog.input.mapper.AudioInputMapper;
import dev.vitorpaulo.blog.input.mapper.PostInputMapper;
import dev.vitorpaulo.blog.input.mapper.ReactionInputMapper;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.FeaturedPostRequest;
import dev.vitorpaulo.blog.input.request.MassDeleteRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.input.request.PostQueryRequest;
import dev.vitorpaulo.blog.input.request.ReactToRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.AudioResponse;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.input.response.ReactionResponse;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.output.post.PostOutput;
import dev.vitorpaulo.blog.usecase.audio.RetryPostAudioUseCase;
import dev.vitorpaulo.blog.usecase.post.CreatePostUseCase;
import dev.vitorpaulo.blog.usecase.post.DeletePostUseCase;
import dev.vitorpaulo.blog.usecase.post.GetFeaturedPostsUseCase;
import dev.vitorpaulo.blog.usecase.post.GetPostByIdUseCase;
import dev.vitorpaulo.blog.usecase.post.GetPostBySlugUseCase;
import dev.vitorpaulo.blog.usecase.post.ReactToPostUseCase;
import dev.vitorpaulo.blog.usecase.post.SearchPostUseCase;
import dev.vitorpaulo.blog.usecase.post.SetPostFeaturedWeightsUseCase;
import dev.vitorpaulo.blog.usecase.post.UpdatePostUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/post")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final UpdatePostUseCase updatePostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final GetPostByIdUseCase getPostByIdUseCase;
    private final GetPostBySlugUseCase getPostBySlugUseCase;
    private final ReactToPostUseCase reactToPostUseCase;
    private final SearchPostUseCase searchPostUseCase;
    private final SetPostFeaturedWeightsUseCase setPostFeaturedWeightsUseCase;
    private final GetFeaturedPostsUseCase getFeaturedPostsUseCase;

    private final PostInputMapper postInputMapper;
    private final ReactionInputMapper reactionInputMapper;
    private final AudioInputMapper audioInputMapper;
    private final RetryPostAudioUseCase retryPostAudioUseCase;
    private final PostOutput postOutput;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody CreatePostRequest request, @CurrentAuthor AuthorModel author) {
        final var post = createPostUseCase.execute(postInputMapper.toModel(request), request.tagIds(), request.projectIds(), author);
        return postInputMapper.toResponse(post);
    }

    @PutMapping("/{id}")
    public PostResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePostRequest request, @CurrentAuthor AuthorModel author) {
        final var post = updatePostUseCase.execute(postInputMapper.toModel(request, id), request.tagIds(), request.projectIds(), author);
        return postInputMapper.toResponse(post);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody MassDeleteRequest request, @CurrentAuthor AuthorModel author) {
        deletePostUseCase.execute(request.ids(), author);
    }

    @GetMapping("/{id}")
    public PostResponse getById(@PathVariable UUID id) {
        return postInputMapper.toResponse(getPostByIdUseCase.execute(id));
    }

    @GetMapping("/slug/{slug}/{language}")
    public PostResponse getBySlug(@PathVariable String slug, @PathVariable Language language, HttpServletRequest servletRequest) {
        return postInputMapper.toResponse(getPostBySlugUseCase.execute(slug, language, servletRequest.getRemoteAddr()));
    }

    @PostMapping("/{slug}/react")
    @ValidateCaptcha
    @ResponseStatus(HttpStatus.OK)
    public ReactionResponse react(@PathVariable String slug, @Valid @RequestBody ReactToRequest request, HttpServletRequest servletRequest) {
        return reactionInputMapper.toResponse(reactToPostUseCase.execute(slug, request.reactionType(), servletRequest.getRemoteAddr()));
    }

    @PostMapping("/featured")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setFeatured(@RequestBody List<FeaturedPostRequest> request, @CurrentAuthor AuthorModel author) {
        setPostFeaturedWeightsUseCase.execute(postInputMapper.toFeaturedModels(request), author);
    }

    @GetMapping("/featured/{language}")
    public List<PostResponse> getFeatured(@PathVariable Language language) {
        return getFeaturedPostsUseCase.execute(language).stream()
                .map(postInputMapper::toResponse)
                .toList();
    }

    @PostMapping("/search")
    public GenericPageableResponse<PostResponse> search(@Valid @RequestBody GenericPageableRequest<PostQueryRequest> request, @CurrentAuthor AuthorModel author) {
        final var result = searchPostUseCase.execute(postInputMapper.toPageableInput(request), author);
        return postInputMapper.toPageableResponse(result);
    }

    @PostMapping("/{postId}/audio/{type}/{language}/retry")
    public AudioResponse retry(@PathVariable UUID postId, @PathVariable AudioType type, @PathVariable Language language) {
        return audioInputMapper.toResponse(retryPostAudioUseCase.execute(postId, type, language));
    }
}
