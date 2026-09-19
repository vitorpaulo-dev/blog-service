package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.usecase.audio.RetryPostAudioUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AudioEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RetryPostAudioUseCase retryPostAudioUseCase;

    @Test
    void retry_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/post/" + UUID.randomUUID() + "/audio/NARRATION/ENGLISH/retry"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletedStatusEndpoint_unauthenticated_returnsNotFound() throws Exception {
        mockMvc.perform(get("/v1/post/" + UUID.randomUUID() + "/audio"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletedSignedEndpoint_unauthenticated_returnsNotFound() throws Exception {
        mockMvc.perform(get("/v1/post/" + UUID.randomUUID() + "/audio/signed"))
                .andExpect(status().isNotFound());
    }

    @Test
    void retry_authenticated_returnsOk() throws Exception {
        var postId = UUID.randomUUID();
        var jwt = Jwt.withTokenValue("token").header("alg", "none").subject("user").build();
        when(jwtDecoder.decode(any())).thenReturn(jwt);
        when(retryPostAudioUseCase.execute(postId, dev.vitorpaulo.blog.model.AudioType.NARRATION, dev.vitorpaulo.blog.model.Language.ENGLISH))
            .thenReturn(new dev.vitorpaulo.blog.model.audio.AudioModel(
                dev.vitorpaulo.blog.model.AudioType.NARRATION,
                dev.vitorpaulo.blog.model.Language.ENGLISH,
                dev.vitorpaulo.blog.model.AudioStatus.QUEUED,
                "post/audio/k.wav",
                null,
                null));

        mockMvc.perform(post("/v1/post/" + postId + "/audio/NARRATION/ENGLISH/retry").header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }
}
