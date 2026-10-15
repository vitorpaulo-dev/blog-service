package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostReadOutputTest {

    @Mock private PostRepository postRepository;
    @Mock private PostOutputMapper postOutputMapper;
    @Mock private AudioOutput audioOutput;
    @Mock private PostEntity postEntity;
    @Mock private PostModel expectedResult;
    @Mock private AudioModel audioModel;

    @InjectMocks
    private PostReadOutput postReadOutput;

    @Test
    void findBySlug_found_mapsWithProjectTagIdsAndAudioMap() {
        var postId = UUID.randomUUID();
        when(postRepository.findBySlugAndLanguage("my-post", Language.ENGLISH, true)).thenReturn(Optional.of(postEntity));
        when(postEntity.getId()).thenReturn(postId);
        when(postRepository.findProjectIds(postId)).thenReturn(List.of());
        when(postRepository.findTagIds(postId)).thenReturn(List.of());
        when(audioOutput.artifactMap(postId, Language.ENGLISH)).thenReturn(Map.of());
        when(postOutputMapper.toModel(postEntity, List.of(), List.of(), Map.of())).thenReturn(expectedResult);

        var result = postReadOutput.findBySlug("my-post", Language.ENGLISH, true);

        assertEquals(expectedResult, result);
        verify(postRepository).findBySlugAndLanguage("my-post", Language.ENGLISH, true);
    }

    @Test
    void findBySlug_notFound_throwsNotFoundException() {
        when(postRepository.findBySlugAndLanguage("nonexistent", Language.ENGLISH, false)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> postReadOutput.findBySlug("nonexistent", Language.ENGLISH, false));

        assertEquals(ExceptionCode.POST_SLUG_NOT_FOUND, ex.getCode());
        verifyNoInteractions(postOutputMapper);
        verifyNoInteractions(audioOutput);
    }

    @Test
    void findBySlug_audioEnumMap_mappedToSerializationSafeLinkedHashMaps() {
        var postId = UUID.randomUUID();
        var audio = new EnumMap<AudioType, Map<Language, AudioModel>>(AudioType.class);
        var byLanguage = new EnumMap<Language, AudioModel>(Language.class);
        byLanguage.put(Language.PORTUGUESE, audioModel);
        audio.put(AudioType.PODCAST, byLanguage);

        when(postRepository.findBySlugAndLanguage("my-post", Language.PORTUGUESE, false)).thenReturn(Optional.of(postEntity));
        when(postEntity.getId()).thenReturn(postId);
        when(postRepository.findProjectIds(postId)).thenReturn(List.of());
        when(postRepository.findTagIds(postId)).thenReturn(List.of());
        when(audioOutput.artifactMap(postId, Language.PORTUGUESE)).thenReturn(audio);
        when(postOutputMapper.toModel(eq(postEntity), anyList(), anyList(), anyMap())).thenReturn(expectedResult);

        var result = postReadOutput.findBySlug("my-post", Language.PORTUGUESE, false);

        assertEquals(expectedResult, result);
        var captor = ArgumentCaptor.forClass(Map.class);
        verify(postOutputMapper).toModel(eq(postEntity), anyList(), anyList(), captor.capture());
        var cachedAudio = captor.getValue();
        assertInstanceOf(LinkedHashMap.class, cachedAudio);
        assertEquals(audio, cachedAudio);
        assertInstanceOf(LinkedHashMap.class, cachedAudio.get(AudioType.PODCAST));
    }
}
