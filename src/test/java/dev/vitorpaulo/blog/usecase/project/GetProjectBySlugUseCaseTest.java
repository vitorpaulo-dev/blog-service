package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProjectBySlugUseCaseTest {

    @Mock private ProjectOutput projectOutput;
    @Mock private ProjectModel expected;

    @InjectMocks
    private GetProjectBySlugUseCase getProjectBySlugUseCase;

    @Test
    void execute_validSlug_returnsProject() {
        when(projectOutput.findBySlugAndIncrementView("my-project", Language.ENGLISH)).thenReturn(expected);

        var result = getProjectBySlugUseCase.execute("my-project", Language.ENGLISH);

        assertEquals(expected, result);
        verify(projectOutput).findBySlugAndIncrementView("my-project", Language.ENGLISH);
    }

    @Test
    void execute_notFound_throwsNotFoundException() {
        when(projectOutput.findBySlugAndIncrementView(anyString(), any())).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class,
                () -> getProjectBySlugUseCase.execute("nonexistent", Language.PORTUGUESE));
    }
}
