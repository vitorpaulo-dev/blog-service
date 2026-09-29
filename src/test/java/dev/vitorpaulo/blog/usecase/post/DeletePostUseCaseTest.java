package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletePostUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private AudioOutput audioOutput;
    @Mock private AuthorModel author;

    @InjectMocks
    private DeletePostUseCase deletePostUseCase;

    @Test
    void execute_ownedPosts_cleansAudioArtifactsBeforeDeletingPosts() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        var ownedIds = List.of(ids.getFirst());
        when(postOutput.findOwnedIds(ids, author)).thenReturn(ownedIds);

        deletePostUseCase.execute(ids, author);

        InOrder inOrder = inOrder(audioOutput, postOutput);
        inOrder.verify(audioOutput).deleteArtifacts(ownedIds);
        inOrder.verify(postOutput).deleteByIds(ownedIds);
    }

    @Test
    void execute_noOwnedPosts_skipsAudioCleanupAndDeletion() {
        var ids = List.of(UUID.randomUUID());
        when(postOutput.findOwnedIds(ids, author)).thenReturn(List.of());

        deletePostUseCase.execute(ids, author);

        verifyNoInteractions(audioOutput);
        verify(postOutput).findOwnedIds(ids, author);
        verify(postOutput, org.mockito.Mockito.never()).deleteByIds(ids);
    }
}
