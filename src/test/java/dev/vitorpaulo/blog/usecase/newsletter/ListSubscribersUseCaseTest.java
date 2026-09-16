package dev.vitorpaulo.blog.usecase.newsletter;

import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.subscriber.SubscriberOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListSubscribersUseCaseTest {

    @Mock private SubscriberOutput subscriberOutput;
    @Mock private PaginatedInput<SubscriberQueryModel> input;
    @Mock private PaginatedOutput<SubscriberModel> result;

    @InjectMocks
    private ListSubscribersUseCase listSubscribersUseCase;

    @Test
    void execute_delegatesToOutputSearch() {
        when(subscriberOutput.search(input)).thenReturn(result);

        assertEquals(result, listSubscribersUseCase.execute(input));
        verify(subscriberOutput).search(input);
    }
}
