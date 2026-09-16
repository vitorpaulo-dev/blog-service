package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.domain.ProjectContentEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.output.mapper.ProjectOutputMapper;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static dev.vitorpaulo.blog.common.util.RoleUtils.isAdmin;

@Component
@RequiredArgsConstructor
public class ProjectOutput {

	private final ProjectRepository projectRepository;
	private final ProjectOutputMapper projectOutputMapper;
	private final AuthorRepository authorRepository;
	private final TagRepository tagRepository;
	private final StringRedisTemplate stringRedisTemplate;

	private static final Duration REACTION_TTL = Duration.ofSeconds(604800);

	@Transactional(readOnly = true)
	public ProjectModel findById(UUID id) {
		return projectRepository.findByIdWithContents(id)
			.map(project -> projectOutputMapper.toModel(project, Collections.emptyList()))
			.orElseThrow(() -> new NotFoundException(ExceptionCode.PROJECT_NOT_FOUND));
	}

	@Transactional
	public ProjectModel findBySlugAndIncrementView(String slug, Language language, String ip) {
		final var entity = projectRepository.findBySlugAndLanguage(slug, language)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.PROJECT_SLUG_NOT_FOUND));

		final var viewKey = "project:" + entity.getId() + ":view:" + ip;
		if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(viewKey))) {
			entity.setViewCount(entity.getViewCount() == null ? 1L : entity.getViewCount() + 1);
			projectRepository.save(entity);
			stringRedisTemplate.opsForValue().set(viewKey, "viewed", Duration.ofHours(48));
		}

		return projectOutputMapper.toModel(entity, Collections.emptyList());
	}

	@Transactional
	public ReactionModel react(String slug, ReactionType reactionType, String ip) {
		final var entity = projectRepository.findBySlug(slug)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.PROJECT_NOT_FOUND));

		final var key = "project:" + entity.getId() + ":reaction:" + ip + ":" + reactionType;
		if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
			return projectOutputMapper.toReactionModel(entity);
		}

		increment(entity, reactionType);
		final var saved = projectRepository.save(entity);
		stringRedisTemplate.opsForValue().set(key, "reacted", REACTION_TTL);

		return projectOutputMapper.toReactionModel(saved);
	}

	private void increment(ProjectEntity entity, ReactionType reactionType) {
		switch (reactionType) {
			case LOVE -> entity.setLoveCount(next(entity.getLoveCount()));
			case CELEBRATE -> entity.setCelebrateCount(next(entity.getCelebrateCount()));
			case GENIUS -> entity.setGeniusCount(next(entity.getGeniusCount()));
			case HELP -> entity.setHelpCount(next(entity.getHelpCount()));
		}
	}

	private Long next(Long current) {
		return current == null ? 1L : current + 1;
	}

	@Transactional
	public ProjectModel save(ProjectModel project, List<UUID> tagIds, AuthorModel author) {
		final var firstContent = getFirstContent(project.translations());
		final var slug = generateUniqueSlug(firstContent.title(), null);

		final var entity = new ProjectEntity();
		projectOutputMapper.updateEntity(project, entity);
		entity.setSlug(slug);

		syncContents(entity, project.translations());
		entity.setAuthors(authorRepository.findAllById(List.of(author.id())));
		if (tagIds != null) entity.setTags(tagRepository.findAllById(tagIds));

		return projectOutputMapper.toModel(projectRepository.save(entity), tagIds);
	}

	@Transactional
	public ProjectModel update(ProjectModel project, List<UUID> tagIds, AuthorModel author) {
		final var entity = projectRepository.findByIdWithAuthor(project.id(), author.id(), isAdmin(author.role()))
			.orElseThrow(() -> new NotFoundException(ExceptionCode.PROJECT_NOT_FOUND));

		final var firstContent = getFirstContent(project.translations());
		final var previousTitle = getFirstContentTitle(entity.getContents());
		final var titleChanged = firstContent.title() != null
			&& !firstContent.title().equalsIgnoreCase(previousTitle);

		projectOutputMapper.updateEntity(project, entity);
		if (titleChanged) {
			entity.setSlug(generateUniqueSlug(firstContent.title(), entity.getId()));
		}

		syncContents(entity, project.translations());

		entity.setTags(tagRepository.findAllById(Objects.requireNonNullElse(tagIds, Collections.emptyList())));

		return projectOutputMapper.toModel(projectRepository.save(entity), tagIds);
	}

	@Transactional
	public void deleteAll(List<UUID> ids, AuthorModel author) {
		projectRepository.deleteByIdWithAuthor(ids, author.id(), isAdmin(author.role()));
	}

	@Transactional(readOnly = true)
	public PaginatedOutput<ProjectModel> search(PaginatedInput<ProjectQueryModel> pageableInput, AuthorModel author) {
		final var language = pageableInput.query().language();
		final var pageable = PageRequest.of(pageableInput.page(), pageableInput.size());
		final var page = projectRepository.search(
				pageableInput.query().query(),
				pageableInput.query().authorId(),
				pageableInput.query().tagId(),
				language != null ? language.name() : null,
				author != null,
				pageable,
				mapSortProperty(pageableInput.sort(), pageableInput.direction())
			);

		return new PaginatedOutput<>(
			page.stream()
				.map(project -> projectOutputMapper.toModel(project, Collections.emptyList()))
				.peek(project -> {
					final var contents = project.translations();
					if (contents.size() <= 1) return;

					contents.keySet().removeIf(key -> key != language);
				})
				.toList(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}

	@Transactional(readOnly = true)
	public List<ProjectModel> findAllById(List<UUID> ids, Language language) {
		return projectRepository.findAllByIdWithSingleContent(ids, language)
			.stream()
			.map(project -> projectOutputMapper.toModel(project, Collections.emptyList()))
			.peek(project -> {
				final var contents = project.translations();
				if (contents.size() <= 1) return;

				contents.keySet().removeIf(key -> key != language);
			})
			.toList();
	}

	private void syncContents(ProjectEntity entity, Map<Language, ProjectContentModel> translations) {
		if (translations == null) return;
		final var existingByLang = entity.getContents().stream()
			.collect(Collectors.toMap(ProjectContentEntity::getLanguage, c -> c));
		translations.forEach((lang, model) -> {
			final var existing = existingByLang.get(lang);
			if (existing != null) {
				existing.setTitle(model.title());
				existing.setDescription(model.description());
				existing.setSummary(model.summary());
			} else {
				final var content = projectOutputMapper.toContentEntity(model);
				content.setLanguage(lang);
				content.setProject(entity);
				entity.getContents().add(content);
			}
		});

		entity.getContents().removeIf(c -> !translations.containsKey(c.getLanguage()));
	}

	private ProjectContentModel getFirstContent(Map<Language, ProjectContentModel> translations) {
		if (translations == null || translations.isEmpty()) {
			return new ProjectContentModel("", "", "");
		}

		final var english = translations.get(Language.ENGLISH);
		return english != null ? english : translations.values().iterator().next();
	}

	private String getFirstContentTitle(List<ProjectContentEntity> contents) {
		if (contents == null || contents.isEmpty()) return null;
		return contents.stream()
			.filter(c -> c.getLanguage() == Language.ENGLISH)
			.findFirst()
			.map(ProjectContentEntity::getTitle)
			.orElseGet(() -> contents.getFirst().getTitle());
	}

	private String generateUniqueSlug(String title, UUID currentId) {
		final var base = PostUtils.slugify(title);
		final var counter = projectRepository.countBySlugAndIdNot(base, currentId);
		return counter == 0 ? base : base + "-" + (counter + 1);
	}

	private String mapSortProperty(String sort, Sort.Direction direction) {
		final var dir = direction.name();
		return switch (sort) {
			case "slug" -> "slug " + dir;
			case "viewCount" -> "view_count " + dir;
			case "reactionCount" -> "reaction_count " + dir;
			default -> "created_at " + dir + ", updated_at " + dir;
		};
	}
}
