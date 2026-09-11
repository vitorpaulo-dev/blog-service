package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProjectByIdUseCaseTest {

    @Mock private ProjectOutput projectOutput;
    @Mock private ProjectModel expected;

    @InjectMocks
    private GetProjectByIdUseCase getProjectByIdUseCase;

    @Test
    void execute_found_returnsProject() {
        var id = UUID.randomUUID();
        when(projectOutput.findById(id)).thenReturn(expected);

        var result = getProjectByIdUseCase.execute(id);

        assertEquals(expected, result);
    }

    @Test
    void execute_notFound_throwsNotFoundException() {
        when(projectOutput.findById(any())).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> getProjectByIdUseCase.execute(UUID.randomUUID()));
    }
}
