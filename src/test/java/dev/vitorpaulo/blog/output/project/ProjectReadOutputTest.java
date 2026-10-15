package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.mapper.ProjectOutputMapper;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectReadOutputTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectOutputMapper projectOutputMapper;
    @Mock private ProjectEntity projectEntity;
    @Mock private ProjectModel expectedResult;

    @InjectMocks
    private ProjectReadOutput projectReadOutput;

    @Test
    void findBySlug_found_mapsWithEmptyTagIds() {
        when(projectRepository.findBySlugAndLanguage("my-project", Language.ENGLISH, true)).thenReturn(Optional.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity, List.of())).thenReturn(expectedResult);

        var result = projectReadOutput.findBySlug("my-project", Language.ENGLISH, true);

        assertEquals(expectedResult, result);
        verify(projectRepository).findBySlugAndLanguage("my-project", Language.ENGLISH, true);
    }

    @Test
    void findBySlug_notFound_throwsNotFoundException() {
        when(projectRepository.findBySlugAndLanguage("nonexistent", Language.ENGLISH, false)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> projectReadOutput.findBySlug("nonexistent", Language.ENGLISH, false));

        assertEquals(ExceptionCode.PROJECT_SLUG_NOT_FOUND, ex.getCode());
        verifyNoInteractions(projectOutputMapper);
    }
}
