package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetTagByBatchUseCase {

    private final TagOutput tagOutput;

    public List<TagModel> execute(List<UUID> ids, Language language) {
        return tagOutput.findAllById(ids, language);
    }
}
