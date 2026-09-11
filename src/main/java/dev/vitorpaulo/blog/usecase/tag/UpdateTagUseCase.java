package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateTagUseCase {

    private final TagOutput tagOutput;

    public TagModel execute(UUID id, TagModel tag) {
        final var model = new TagModel(id, tag.slug(), tag.translations());
        return tagOutput.update(model);
    }
}
