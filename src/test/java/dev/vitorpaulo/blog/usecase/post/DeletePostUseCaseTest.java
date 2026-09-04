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
    @Mock private AuthorModel author;

    @InjectMocks
    private DeletePostUseCase deletePostUseCase;

    @Test
    void execute_withIds_delegatesToOutput() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        deletePostUseCase.execute(ids, author);

        verify(postOutput).deleteAll(ids, author);
    }
}
