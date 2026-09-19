package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.input.mapper.AudioInputMapper;
import dev.vitorpaulo.blog.input.response.AudioResponse;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.usecase.audio.RetryPostAudioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/post")
@RequiredArgsConstructor
public class AudioController {

    private final RetryPostAudioUseCase retryPostAudioUseCase;
    private final AudioInputMapper audioInputMapper;

    @PostMapping("/{postId}/audio/{type}/{language}/retry")
    public AudioResponse retry(@PathVariable UUID postId, @PathVariable AudioType type, @PathVariable Language language) {
        return audioInputMapper.toResponse(retryPostAudioUseCase.execute(postId, type, language));
    }
}
