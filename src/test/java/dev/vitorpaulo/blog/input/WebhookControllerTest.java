package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.input.request.ResendWebhookRequest;
import dev.vitorpaulo.blog.model.ResendWebhookEventModel;
import dev.vitorpaulo.blog.usecase.newsletter.ProcessResendWebhookUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    private static final String BODY = "{\"type\":\"contact.updated\",\"data\":{\"email\":\"reader@example.com\",\"unsubscribed\":true}}";

    @Mock private WebhookSignatureVerifier webhookSignatureVerifier;
    @Mock private ProcessResendWebhookUseCase processResendWebhookUseCase;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private WebhookController webhookController;

    @Test
    void resend_validRequest_verifiesAndProcesses() {
        var request = new ResendWebhookRequest("contact.updated",
            new dev.vitorpaulo.blog.input.request.ResendWebhookDataRequest("reader@example.com", true));
        org.mockito.Mockito.when(objectMapper.readValue(anyString(), eq(ResendWebhookRequest.class))).thenReturn(request);
        org.mockito.Mockito.doAnswer(invocation -> null).when(webhookSignatureVerifier).verify(anyString(), anyString(), anyString(), anyString());

        webhookController.resend(BODY, "msg_test", "1615905347", "v1,signature");

        verify(webhookSignatureVerifier).verify(BODY, "msg_test", "1615905347", "v1,signature");

        final var captor = ArgumentCaptor.forClass(ResendWebhookEventModel.class);
        verify(processResendWebhookUseCase).execute(captor.capture());
        assertEquals("contact.updated", captor.getValue().type());
        assertEquals("reader@example.com", captor.getValue().email());
        assertEquals(Boolean.TRUE, captor.getValue().unsubscribed());
    }

    @Test
    void resend_invalidSignature_stopsProcessing() {
        doThrow(new BusinessException(org.springframework.http.HttpStatus.FORBIDDEN,
            dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode.WEBHOOK_SIGNATURE_INVALID, null))
            .when(webhookSignatureVerifier).verify(anyString(), anyString(), anyString(), anyString());

        org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class, () ->
            webhookController.resend(BODY, "msg_test", "1615905347", "v1,invalid"));

        verify(processResendWebhookUseCase, never()).execute(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resend_missingHeaders_verifierThrowsBadRequest() {
        doThrow(new BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST))
            .when(webhookSignatureVerifier).verify(eq(BODY), isNull(), isNull(), isNull());

        org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class, () ->
            webhookController.resend(BODY, null, null, null));

        verify(processResendWebhookUseCase, never()).execute(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resend_nullData_mapsNullEventFields() {
        var request = new ResendWebhookRequest("contact.deleted", null);
        org.mockito.Mockito.when(objectMapper.readValue(anyString(), eq(ResendWebhookRequest.class))).thenReturn(request);

        webhookController.resend(BODY, "msg_test", "1615905347", "v1,signature");

        final var captor = ArgumentCaptor.forClass(ResendWebhookEventModel.class);
        verify(processResendWebhookUseCase).execute(captor.capture());
        assertEquals("contact.deleted", captor.getValue().type());
        assertNull(captor.getValue().email());
        assertNull(captor.getValue().unsubscribed());
    }
}
