package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.output.tag.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteTagUseCase {

    private final TagOutput tagOutput;

    public void execute(List<UUID> ids) {
        tagOutput.deleteAll(ids);
    }
}
