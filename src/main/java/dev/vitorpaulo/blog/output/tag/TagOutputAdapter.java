package dev.vitorpaulo.blog.output.tag;

import dev.vitorpaulo.blog.mapper.PostMapper;
import dev.vitorpaulo.blog.model.post.Tag;
import dev.vitorpaulo.blog.output.TagOutput;
import dev.vitorpaulo.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TagOutputAdapter implements TagOutput {

    private final TagRepository tagRepository;
    private final PostMapper postMapper;

    @Override
    public List<Tag> findAllById(List<UUID> ids) {
        return tagRepository.findAllById(ids).stream().map(postMapper::map).toList();
    }
}
