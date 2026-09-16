package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.input.mapper.NewsletterInputMapper;
import dev.vitorpaulo.blog.input.request.SubscribeRequest;
import dev.vitorpaulo.blog.input.request.SubscriberQueryRequest;
import dev.vitorpaulo.blog.input.response.SubscriberResponse;
import dev.vitorpaulo.blog.model.SubscribeResultModel;
import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.usecase.newsletter.AdminUnsubscribeUseCase;
import dev.vitorpaulo.blog.usecase.newsletter.ListSubscribersUseCase;
import dev.vitorpaulo.blog.usecase.newsletter.SubscribeUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsletterControllerTest {

    @Mock private SubscribeUseCase subscribeUseCase;
    @Mock private ListSubscribersUseCase listSubscribersUseCase;
    @Mock private AdminUnsubscribeUseCase adminUnsubscribeUseCase;
    @Mock private NewsletterInputMapper newsletterInputMapper;
    @Mock private SubscribeRequest subscribeRequest;
    @Mock private SubscriberResponse subscriberResponse;
    @Mock private SubscriberModel subscriberModel;
    @Mock private GenericPageableRequest<SubscriberQueryRequest> searchRequest;
    @Mock private PaginatedInput<SubscriberQueryModel> paginatedInput;
    @Mock private PaginatedOutput<SubscriberModel> paginatedOutput;
    @Mock private GenericPageableResponse<SubscriberResponse> pageableResponse;

    @InjectMocks
    private NewsletterController newsletterController;

    @Test
    void subscribe_newSubscriber_returnsCreated() {
        when(newsletterInputMapper.toModel(subscribeRequest)).thenReturn(subscriberModel);
        when(subscribeUseCase.execute(subscriberModel)).thenReturn(new SubscribeResultModel(true, subscriberModel));
        when(newsletterInputMapper.toResponse(subscriberModel)).thenReturn(subscriberResponse);

        final var result = newsletterController.subscribe(subscribeRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(subscriberResponse, result.getBody());
    }

    @Test
    void subscribe_alreadyActive_returnsOkWithExisting() {
        when(newsletterInputMapper.toModel(subscribeRequest)).thenReturn(subscriberModel);
        when(subscribeUseCase.execute(subscriberModel)).thenReturn(new SubscribeResultModel(false, subscriberModel));
        when(newsletterInputMapper.toResponse(subscriberModel)).thenReturn(subscriberResponse);

        final var result = newsletterController.subscribe(subscribeRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(subscriberResponse, result.getBody());
    }

    @Test
    void subscribe_reactivated_returnsCreated() {
        when(newsletterInputMapper.toModel(subscribeRequest)).thenReturn(subscriberModel);
        when(subscribeUseCase.execute(subscriberModel)).thenReturn(new SubscribeResultModel(true, subscriberModel));
        when(newsletterInputMapper.toResponse(subscriberModel)).thenReturn(subscriberResponse);

        final var result = newsletterController.subscribe(subscribeRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    void search_delegatesToUseCaseAndMapper() {
        when(newsletterInputMapper.toPageableInput(searchRequest)).thenReturn(paginatedInput);
        when(listSubscribersUseCase.execute(paginatedInput)).thenReturn(paginatedOutput);
        when(newsletterInputMapper.toPageableResponse(paginatedOutput)).thenReturn(pageableResponse);

        final var result = newsletterController.search(searchRequest);

        assertEquals(pageableResponse, result);
    }

    @Test
    void unsubscribe_delegatesToUseCase() {
        var id = UUID.randomUUID();
        when(adminUnsubscribeUseCase.execute(id)).thenReturn(subscriberModel);
        when(newsletterInputMapper.toResponse(subscriberModel)).thenReturn(subscriberResponse);

        final var result = newsletterController.unsubscribe(id);

        assertEquals(subscriberResponse, result);
    }

    @Test
    void unsubscribe_unknownId_propagatesNotFoundException() {
        var id = UUID.randomUUID();
        when(adminUnsubscribeUseCase.execute(id)).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> newsletterController.unsubscribe(id));
    }
}
