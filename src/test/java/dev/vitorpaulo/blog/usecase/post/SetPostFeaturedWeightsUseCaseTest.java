package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.FeaturedPostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetPostFeaturedWeightsUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private AuthorModel author;

    @InjectMocks
    private SetPostFeaturedWeightsUseCase setPostFeaturedWeightsUseCase;

    @Test
    void execute_delegatesToOutput() {
        var featured = List.of(new FeaturedPostModel(UUID.randomUUID(), 1));

        setPostFeaturedWeightsUseCase.execute(featured, author);

        verify(postOutput).setFeaturedWeights(featured, author);
    }
}
