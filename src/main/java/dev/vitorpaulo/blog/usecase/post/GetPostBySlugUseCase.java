package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.output.post.PostOutput;
import dev.vitorpaulo.blog.output.view.ViewCounterOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPostBySlugUseCase {

    private final PostOutput postOutput;
    private final ViewCounterOutput viewCounterOutput;

    public PostModel execute(String slug, Language language, String ip) {
        final var post = postOutput.findBySlug(slug, language);
        if (post.status() == PostStatus.PUBLISHED) {
            viewCounterOutput.countPostView(post.id(), ip);
        }
        return post;
    }
}
