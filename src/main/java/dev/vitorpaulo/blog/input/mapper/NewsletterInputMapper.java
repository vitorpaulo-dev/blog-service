package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.SubscribeRequest;
import dev.vitorpaulo.blog.input.request.SubscriberQueryRequest;
import dev.vitorpaulo.blog.input.response.SubscriberResponse;
import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NewsletterInputMapper {

    SubscriberModel toModel(SubscribeRequest request);

    SubscriberQueryModel toQueryModel(SubscriberQueryRequest request);

    SubscriberResponse toResponse(SubscriberModel model);

    GenericPageableResponse<SubscriberResponse> toPageableResponse(PaginatedOutput<SubscriberModel> result);

    PaginatedInput<SubscriberQueryModel> toPageableInput(GenericPageableRequest<SubscriberQueryRequest> request);
}
