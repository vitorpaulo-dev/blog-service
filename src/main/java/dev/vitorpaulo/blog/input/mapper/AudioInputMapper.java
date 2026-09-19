package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.input.response.AudioArtifactResponse;
import dev.vitorpaulo.blog.input.response.AudioResponse;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface AudioInputMapper {

    AudioResponse toResponse(AudioModel model);

    AudioArtifactResponse toArtifactResponse(AudioModel model);

    Map<Language, AudioArtifactResponse> toArtifactResponseByLanguage(Map<Language, AudioModel> audio);

    Map<AudioType, Map<Language, AudioArtifactResponse>> toArtifactResponseMap(Map<AudioType, Map<Language, AudioModel>> audio);
}
