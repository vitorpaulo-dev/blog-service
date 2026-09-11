package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProjectUseCaseTest {

    @Mock private ProjectOutput projectOutput;
    @Mock private ProjectModel project;
    @Mock private AuthorModel author;
    @Mock private ProjectModel savedProject;

    @InjectMocks
    private CreateProjectUseCase createProjectUseCase;

    @Test
    void execute_returnsSavedProject() {
        when(projectOutput.save(project, author)).thenReturn(savedProject);

        var result = createProjectUseCase.execute(project, author);

        assertEquals(savedProject, result);
        verify(projectOutput).save(project, author);
    }
}
