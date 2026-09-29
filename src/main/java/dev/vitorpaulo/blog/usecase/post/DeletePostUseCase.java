package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeletePostUseCase {

    private final PostOutput postOutput;
    private final AudioOutput audioOutput;

    public void execute(List<UUID> ids, AuthorModel author) {
        final var ownedIds = postOutput.findOwnedIds(ids, author);
        if (ownedIds.isEmpty()) {
            return;
        }

        audioOutput.deleteArtifacts(ownedIds);
        postOutput.deleteByIds(ownedIds);
    }
}
