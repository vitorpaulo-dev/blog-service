package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.mapper.ProjectMapper;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectOutputTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectOutput projectOutput;

    @Test
    void shouldReturnEmptyListWhenIdsIsNull() {
        var result = projectOutput.findAllById(null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(projectRepository);
    }

    @Test
    void shouldReturnEmptyListWhenIdsIsEmpty() {
        var result = projectOutput.findAllById(List.of());
        assertTrue(result.isEmpty());
        verifyNoInteractions(projectRepository);
    }

    @Test
    void shouldReturnProjectsWhenFound() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var entity1 = mock(dev.vitorpaulo.blog.domain.ProjectEntity.class);
        var entity2 = mock(dev.vitorpaulo.blog.domain.ProjectEntity.class);
        var model1 = new ProjectModel(id1, "slug-1", null, null, null);
        var model2 = new ProjectModel(id2, "slug-2", null, null, null);

        when(projectRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(entity1, entity2));
        when(projectMapper.toModel(entity1)).thenReturn(model1);
        when(projectMapper.toModel(entity2)).thenReturn(model2);

        var result = projectOutput.findAllById(List.of(id1, id2));

        assertEquals(2, result.size());
        assertEquals(model1, result.get(0));
        assertEquals(model2, result.get(1));
    }
}
