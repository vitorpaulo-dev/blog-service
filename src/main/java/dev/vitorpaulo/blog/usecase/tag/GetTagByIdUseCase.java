package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetTagByIdUseCase {

    private final TagOutput tagOutput;

    public TagModel execute(UUID id) {
        return tagOutput.findById(id);
    }
}
