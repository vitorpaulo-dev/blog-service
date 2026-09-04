package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.output.post.PostOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetPostByIdUseCase {

    private final PostOutput postOutput;

    public PostModel execute(UUID id) {
        return postOutput.findById(id);
    }
}
