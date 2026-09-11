package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.domain.ProjectContentEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.output.mapper.ProjectMapper;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.vitorpaulo.blog.common.util.RoleUtils.isAdmin;

@Component
@RequiredArgsConstructor
public class ProjectOutput {

	private final ProjectRepository projectRepository;
	private final ProjectMapper projectMapper;
	private final AuthorRepository authorRepository;

	public ProjectModel findById(UUID id) {
		return projectRepository.findById(id)
			.map(projectMapper::toModel)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.PROJECT_NOT_FOUND));
	}

	@Transactional
	public ProjectModel findBySlugAndIncrementView(String slug, Language language) {
		final var entity = projectRepository.findBySlugAndLanguage(slug, language)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.PROJECT_SLUG_NOT_FOUND));
		entity.setViewCount(entity.getViewCount() == null ? 1L : entity.getViewCount() + 1);

		return projectMapper.toModel(projectRepository.save(entity), language);
	}

	@Transactional
	public ProjectModel save(ProjectModel project, AuthorModel author) {
		final var firstContent = getFirstContent(project.translations());
		final var slug = generateUniqueSlug(firstContent.title(), null);

		final var entity = new ProjectEntity();
		projectMapper.updateEntity(project, entity);
		resetReactionCounts(entity);
		entity.setSlug(slug);

		syncContents(entity, project.translations());
		entity.setAuthors(authorRepository.findAllById(List.of(author.id())));

		return projectMapper.toModel(projectRepository.save(entity));
	}

	@Transactional
	public ProjectModel update(ProjectModel project, AuthorModel author) {
		final var entity = projectRepository.findByIdWithAuthor(project.id(), author.id(), isAdmin(author.role()))
			.orElseThrow(() -> new NotFoundException(ExceptionCode.PROJECT_NOT_FOUND));

		final var firstContent = getFirstContent(project.translations());
		final var previousTitle = getFirstContentTitle(entity.getContents());
		final var titleChanged = firstContent.title() != null
			&& !firstContent.title().equalsIgnoreCase(previousTitle);

		projectMapper.updateEntity(project, entity);
		if (titleChanged) {
			entity.setSlug(generateUniqueSlug(firstContent.title(), entity.getId()));
		}

		syncContents(entity, project.translations());

		return projectMapper.toModel(projectRepository.save(entity));
	}

	@Transactional
	public void deleteAll(List<UUID> ids, AuthorModel author) {
		projectRepository.deleteByIdWithAuthor(ids, author.id(), isAdmin(author.role()));
	}

	public PaginatedOutput<ProjectModel> search(PaginatedInput<ProjectQueryModel> pageableInput, AuthorModel author) {
		final var language = pageableInput.query().language();
		final var pageable = PageRequest.of(pageableInput.page(), pageableInput.size());
		final var result = projectRepository.search(
				pageableInput.query().query(),
				pageableInput.query().authorId(),
				language != null ? language.name() : null,
				author != null,
				pageable,
				mapSortProperty(pageableInput.sort(), pageableInput.direction())
			)
			.map(entity -> projectMapper.toModel(entity, language));

		return new PaginatedOutput<>(
			result.getContent(),
			result.getNumber(),
			result.getSize(),
			result.getTotalElements(),
			result.getTotalPages()
		);
	}

	public List<ProjectModel> findAllById(List<UUID> ids) {
		if (ids == null || ids.isEmpty()) return List.of();
		return projectRepository.findAllById(ids).stream()
			.map(projectMapper::toModel)
			.toList();
	}

	public List<ProjectModel> findAllById(List<UUID> ids, Language language) {
		if (ids == null || ids.isEmpty()) return List.of();
		return projectRepository.findAllById(ids).stream()
			.map(entity -> projectMapper.toModel(entity, language))
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
			} else {
				final var content = projectMapper.toContentEntity(model);
				content.setLanguage(lang);
				content.setProject(entity);
				entity.getContents().add(content);
			}
		});

		entity.getContents().removeIf(c -> !translations.containsKey(c.getLanguage()));
	}

	private void resetReactionCounts(ProjectEntity entity) {
		entity.setCelebrateCount(0L);
		entity.setGeniusCount(0L);
		entity.setHelpCount(0L);
		entity.setViewCount(0L);
		entity.setLoveCount(0L);
	}

	private ProjectContentModel getFirstContent(Map<Language, ProjectContentModel> translations) {
		if (translations == null || translations.isEmpty()) {
			return new ProjectContentModel("", "");
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