package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.output.tag.TagOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteTagUseCaseTest {

    @Mock private TagOutput tagOutput;

    @InjectMocks
    private DeleteTagUseCase deleteTagUseCase;

    @Test
    void execute_delegatesToOutput() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        deleteTagUseCase.execute(ids);

        verify(tagOutput).deleteAll(ids);
    }
}
