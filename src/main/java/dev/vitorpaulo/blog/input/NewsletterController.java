package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.config.captcha.ValidateCaptcha;
import dev.vitorpaulo.blog.input.mapper.NewsletterInputMapper;
import dev.vitorpaulo.blog.input.request.SubscribeRequest;
import dev.vitorpaulo.blog.input.request.SubscriberQueryRequest;
import dev.vitorpaulo.blog.input.response.SubscriberResponse;
import dev.vitorpaulo.blog.model.SubscribeResultModel;
import dev.vitorpaulo.blog.usecase.newsletter.AdminUnsubscribeUseCase;
import dev.vitorpaulo.blog.usecase.newsletter.ListSubscribersUseCase;
import dev.vitorpaulo.blog.usecase.newsletter.SubscribeUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/newsletter")
@RequiredArgsConstructor
@Validated
public class NewsletterController {

    private final SubscribeUseCase subscribeUseCase;
    private final ListSubscribersUseCase listSubscribersUseCase;
    private final AdminUnsubscribeUseCase adminUnsubscribeUseCase;
    private final NewsletterInputMapper newsletterInputMapper;

    @PostMapping("/subscribe")
    @ValidateCaptcha
    public ResponseEntity<SubscriberResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        final var result = subscribeUseCase.execute(newsletterInputMapper.toModel(request));
        final var response = newsletterInputMapper.toResponse(result.subscriber());

        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    @PostMapping("/subscriber/search")
    public GenericPageableResponse<SubscriberResponse> search(@Valid @RequestBody GenericPageableRequest<SubscriberQueryRequest> request) {
        final var result = listSubscribersUseCase.execute(newsletterInputMapper.toPageableInput(request));

        return newsletterInputMapper.toPageableResponse(result);
    }

    @PostMapping("/subscriber/{id}/unsubscribe")
    public SubscriberResponse unsubscribe(@PathVariable UUID id) {
        return newsletterInputMapper.toResponse(adminUnsubscribeUseCase.execute(id));
    }
}
