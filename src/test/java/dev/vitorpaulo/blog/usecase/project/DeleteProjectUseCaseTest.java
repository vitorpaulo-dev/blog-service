package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProjectUseCaseTest {

    @Mock private ProjectOutput projectOutput;
    @Mock private AuthorModel author;

    @InjectMocks
    private DeleteProjectUseCase deleteProjectUseCase;

    @Test
    void execute_withIds_delegatesToOutput() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        deleteProjectUseCase.execute(ids, author);

        verify(projectOutput).deleteAll(ids, author);
    }
}
