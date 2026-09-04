package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
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
class DeletePostUseCaseTest {

    @Mock private PostOutput postOutput;

    @InjectMocks
    private DeletePostUseCase deletePostUseCase;

    @Test
    void shouldDelegateDeletionToOutput() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        var author = new AuthorModel(UUID.randomUUID(), "clerk-1", "Author", "author", null, "org:admin", null);

        deletePostUseCase.execute(ids, author);

        verify(postOutput).deleteAll(ids, author);
    }
}
