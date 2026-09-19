package dev.vitorpaulo.blog.client.audio;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "audio-worker", url = "${audio.worker.url}")
public interface AudioWorkerFeignClient {

    @PostMapping("/generate")
    void generate(@RequestBody AudioJobRequest request);
}
