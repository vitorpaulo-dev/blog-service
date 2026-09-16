package dev.vitorpaulo.blog.usecase.newsletter;

import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.subscriber.SubscriberOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListSubscribersUseCase {

    private final SubscriberOutput subscriberOutput;

    public PaginatedOutput<SubscriberModel> execute(PaginatedInput<SubscriberQueryModel> input) {
        return subscriberOutput.search(input);
    }
}
