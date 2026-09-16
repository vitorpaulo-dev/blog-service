package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class WebhookSignatureVerifierTest {

    private static final String WHSEC = "whsec_MFkvV2uo3az9cuPCn9umRMEv0PVy6qOVBkmA2eNcSxg";

    private WebhookSignatureVerifier webhookSignatureVerifier;
    private String secret;
    private String body;

    @BeforeEach
    void setUp() throws Exception {
        webhookSignatureVerifier = new WebhookSignatureVerifier();
        ReflectionTestUtils.setField(webhookSignatureVerifier, "secret", WHSEC);

        secret = WHSEC.substring("whsec_".length());
        body = "{\"type\":\"contact.updated\",\"data\":{\"email\":\"reader@example.com\"}}";
    }

    @Test
    void verify_missingHeaders_throwsBadRequest() {
        var exception = assertThrows(BusinessException.class, () -> webhookSignatureVerifier.verify(body, null, "1615905347", "v1,abc"));

        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void verify_validSignature_passes() throws Exception {
        var id = "msg_test";
        var timestamp = "1615905347";
        var signature = sign(id, timestamp);

        assertDoesNotThrow(() -> webhookSignatureVerifier.verify(body, id, timestamp, "v1," + signature));
    }

    @Test
    void verify_multipleSignatureVersions_anyMatchPasses() throws Exception {
        var id = "msg_test";
        var timestamp = "1615905347";
        var signature = sign(id, timestamp);

        assertDoesNotThrow(() -> webhookSignatureVerifier.verify(body, id, timestamp, "v0,invalid v1," + signature));
    }

    @Test
    void verify_wrongSignature_throwsForbiddenException() {
        var exception = assertThrows(BusinessException.class, () -> webhookSignatureVerifier.verify(
            body,
            "msg_test",
            "1615905347",
            "v1," + Base64.getEncoder().encodeToString("tampered".getBytes(StandardCharsets.UTF_8))
        ));

        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode.WEBHOOK_SIGNATURE_INVALID, exception.getCode());
    }

    @Test
    void verify_tamperedBody_throwsForbidden() throws Exception {
        var id = "msg_test";
        var timestamp = "1615905347";
        var signature = sign(id, timestamp);

        assertThrows(BusinessException.class, () ->
            webhookSignatureVerifier.verify("{\"type\":\"contact.deleted\"}", id, timestamp, "v1," + signature));
    }

    private String sign(String id, String timestamp) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256"));

        return Base64.getEncoder().encodeToString(
            mac.doFinal((id + "." + timestamp + "." + body).getBytes(StandardCharsets.UTF_8)));
    }
}
