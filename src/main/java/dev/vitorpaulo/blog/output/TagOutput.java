package dev.vitorpaulo.blog.output;

import dev.vitorpaulo.blog.model.post.Tag;

import java.util.List;
import java.util.UUID;

public interface TagOutput {

    List<Tag> findAllById(List<UUID> ids);
}
