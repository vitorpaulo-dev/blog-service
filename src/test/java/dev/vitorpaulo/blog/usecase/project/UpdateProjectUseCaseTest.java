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
class UpdateProjectUseCaseTest {

    @Mock private ProjectOutput projectOutput;
    @Mock private ProjectModel project;
    @Mock private AuthorModel author;
    @Mock private ProjectModel updatedProject;

    @InjectMocks
    private UpdateProjectUseCase updateProjectUseCase;

    @Test
    void execute_returnsUpdatedProject() {
        when(projectOutput.update(project, null, author)).thenReturn(updatedProject);

        var result = updateProjectUseCase.execute(project, null, author);

        assertEquals(updatedProject, result);
        verify(projectOutput).update(project, null, author);
    }
}
