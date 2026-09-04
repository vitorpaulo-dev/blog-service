package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.domain.ProjectEntity;
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
    @Mock private ProjectEntity projectEntity1;
    @Mock private ProjectEntity projectEntity2;
    @Mock private ProjectModel projectModel1;
    @Mock private ProjectModel projectModel2;

    @InjectMocks
    private ProjectOutput projectOutput;

    @Test
    void findAllById_nullIds_returnsEmptyList() {
        var result = projectOutput.findAllById(null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(projectRepository);
    }

    @Test
    void findAllById_emptyIds_returnsEmptyList() {
        var result = projectOutput.findAllById(List.of());
        assertTrue(result.isEmpty());
        verifyNoInteractions(projectRepository);
    }

    @Test
    void findAllById_withIds_returnsMappedModels() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();

        when(projectRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(projectEntity1, projectEntity2));
        when(projectMapper.toModel(projectEntity1)).thenReturn(projectModel1);
        when(projectMapper.toModel(projectEntity2)).thenReturn(projectModel2);

        var result = projectOutput.findAllById(List.of(id1, id2));

        assertEquals(2, result.size());
        assertEquals(projectModel1, result.get(0));
        assertEquals(projectModel2, result.get(1));
    }
}
