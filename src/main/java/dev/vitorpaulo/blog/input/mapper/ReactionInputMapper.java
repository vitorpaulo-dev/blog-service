package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.input.response.ReactionResponse;
import dev.vitorpaulo.blog.model.ReactionModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReactionInputMapper {

	ReactionResponse toResponse(ReactionModel model);
}
