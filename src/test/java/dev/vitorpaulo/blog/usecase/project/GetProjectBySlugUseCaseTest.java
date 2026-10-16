package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.model.ProjectStatus;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.output.view.ViewCounterOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProjectBySlugUseCaseTest {

    @Mock private ProjectOutput projectOutput;
    @Mock private ViewCounterOutput viewCounterOutput;
    @Mock private ProjectModel expected;

    @InjectMocks
    private GetProjectBySlugUseCase getProjectBySlugUseCase;

    @Test
    void execute_published_countsViewOnceAndReturnsModel() {
        var projectId = UUID.randomUUID();
        when(projectOutput.findBySlug("my-project", Language.ENGLISH)).thenReturn(expected);
        when(expected.status()).thenReturn(ProjectStatus.PUBLISHED);
        when(expected.id()).thenReturn(projectId);

        var result = getProjectBySlugUseCase.execute("my-project", Language.ENGLISH, "203.0.113.7");

        assertEquals(expected, result);
        verify(projectOutput).findBySlug("my-project", Language.ENGLISH);
        verify(viewCounterOutput).countProjectView(projectId, "203.0.113.7");
    }

    @Test
    void execute_draft_returnsModelWithoutCountingView() {
        when(projectOutput.findBySlug("draft-project", Language.ENGLISH)).thenReturn(expected);
        when(expected.status()).thenReturn(ProjectStatus.DRAFT);

        var result = getProjectBySlugUseCase.execute("draft-project", Language.ENGLISH, "203.0.113.7");

        assertEquals(expected, result);
        verifyNoInteractions(viewCounterOutput);
    }

    @Test
    void execute_notFound_propagatesWithoutCountingView() {
        when(projectOutput.findBySlug(anyString(), any()))
                .thenThrow(new NotFoundException(ExceptionCode.PROJECT_SLUG_NOT_FOUND));

        var ex = assertThrows(NotFoundException.class,
                () -> getProjectBySlugUseCase.execute("nonexistent", Language.PORTUGUESE, "203.0.113.7"));

        assertEquals(ExceptionCode.PROJECT_SLUG_NOT_FOUND, ex.getCode());
        verifyNoInteractions(viewCounterOutput);
    }
}
