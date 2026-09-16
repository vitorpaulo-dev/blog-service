package dev.vitorpaulo.blog.client.resend;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "resend", url = "${resend.api.url}")
public interface ResendFeignClient {

    @PostMapping("/contacts")
    ResendContactResponse createContact(@RequestBody ResendCreateContactRequest request);

    @PatchMapping("/contacts/{id}")
    ResendContactResponse updateContact(@PathVariable("id") String id, @RequestBody ResendUpdateContactRequest request);

    @PatchMapping("/contacts/{id}/topics")
    ResendContactResponse updateContactTopics(@PathVariable("id") String id, @RequestBody List<ResendTopicSubscriptionRequest> topics);
}
