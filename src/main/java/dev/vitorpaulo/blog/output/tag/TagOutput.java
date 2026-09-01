package dev.vitorpaulo.blog.output.tag;

import dev.vitorpaulo.blog.output.mapper.TagMapper;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TagOutput {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public List<TagModel> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return tagRepository.findAllById(ids).stream().map(tagMapper::toModel).toList();
    }
}
