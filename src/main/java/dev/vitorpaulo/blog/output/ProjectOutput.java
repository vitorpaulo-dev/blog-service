package dev.vitorpaulo.blog.output;

import dev.vitorpaulo.blog.model.post.Project;

import java.util.List;
import java.util.UUID;

public interface ProjectOutput {

    List<Project> findAllById(List<UUID> ids);
}
