package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPostBySlugUseCase {

    private final PostOutput postOutput;

    public PostModel execute(String slug, Language language) {
        return postOutput.findBySlugAndIncrementView(slug, language);
    }
}
