package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetFeaturedPostsUseCase {

    private final PostOutput postOutput;

    public List<PostModel> execute(Language language) {
        return postOutput.findFeatured(language);
    }
}
