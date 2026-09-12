package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTagUseCase {

    private final TagOutput tagOutput;

    public TagModel execute(TagModel tag) {
        return tagOutput.save(tag);
    }
}
