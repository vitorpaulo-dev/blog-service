package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.config.cache.CacheConfig;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PostReadOutput {

	private final PostRepository postRepository;
	private final PostOutputMapper postOutputMapper;
	private final AudioOutput audioOutput;

	@Cacheable(cacheNames = CacheConfig.POST_SLUG, key = "@cacheKeys.slug(#slug, #language, #authenticated)")
	@Transactional(readOnly = true)
	public PostModel findBySlug(String slug, Language language, boolean authenticated) {
		final var entity = postRepository.findBySlugAndLanguage(slug, language, authenticated)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.POST_SLUG_NOT_FOUND));

		return postOutputMapper.toModel(
			entity,
			postRepository.findProjectIds(entity.getId()),
			postRepository.findTagIds(entity.getId()),
			cacheSafeAudio(audioOutput.artifactMap(entity.getId(), language))
		);
	}

	private static Map<AudioType, Map<Language, AudioModel>> cacheSafeAudio(Map<AudioType, Map<Language, AudioModel>> source) {
		final var audio = new LinkedHashMap<AudioType, Map<Language, AudioModel>>(source.size());
		source.forEach((type, byLanguage) -> audio.put(type, new LinkedHashMap<>(byLanguage)));
		return audio;
	}
}
