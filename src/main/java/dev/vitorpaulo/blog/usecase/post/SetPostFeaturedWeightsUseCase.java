package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.FeaturedPostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SetPostFeaturedWeightsUseCase {

    private final PostOutput postOutput;

    public void execute(List<FeaturedPostModel> featured, AuthorModel author) {
        postOutput.setFeaturedWeights(featured, author);
    }
}
