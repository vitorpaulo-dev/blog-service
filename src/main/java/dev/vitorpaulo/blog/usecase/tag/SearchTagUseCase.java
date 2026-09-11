package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchTagUseCase {

    private final TagOutput tagOutput;

    public PaginatedOutput<TagModel> execute(PaginatedInput<TagQueryModel> input, Language language) {
        return tagOutput.search(input, language);
    }
}
