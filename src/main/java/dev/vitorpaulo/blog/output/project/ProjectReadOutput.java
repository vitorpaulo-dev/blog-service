package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.config.cache.CacheConfig;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.mapper.ProjectOutputMapper;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ProjectReadOutput {

	private final ProjectRepository projectRepository;
	private final ProjectOutputMapper projectOutputMapper;

	@Cacheable(cacheNames = CacheConfig.PROJECT_SLUG, key = "@cacheKeys.slug(#slug, #language, #authenticated)")
	@Transactional(readOnly = true)
	public ProjectModel findBySlug(String slug, Language language, boolean authenticated) {
		final var entity = projectRepository.findBySlugAndLanguage(slug, language, authenticated)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.PROJECT_SLUG_NOT_FOUND));

		return projectOutputMapper.toModel(entity, Collections.emptyList());
	}
}
