package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.common.util.PostUtils;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.output.mapper.TagMapper;
import dev.vitorpaulo.blog.output.mapper.ProjectMapper;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static dev.vitorpaulo.blog.common.util.RoleUtils.isAdmin;

@Component
@RequiredArgsConstructor
public class PostOutput {

    private final PostRepository postRepository;
    private final PostOutputMapper postOutputMapper;
    private final TagMapper tagMapper;
    private final ProjectMapper projectMapper;

    public PostModel findById(UUID id) {
        return postRepository.findById(id)
                .map(postOutputMapper::toModel)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));
    }

    @Transactional
    public PostModel findBySlugAndIncrementView(String slug) {
        final var entity = postRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_SLUG_NOT_FOUND));
        entity.setViewCount(entity.getViewCount() == null ? 1L : entity.getViewCount() + 1);

        return postOutputMapper.toModel(postRepository.save(entity));
    }

    @Transactional
    public PostModel save(PostModel post, List<TagModel> tags, List<ProjectModel> projects, AuthorModel author) {
        final var slug = generateUniqueSlug(post.title(), null);
        final var reading = PostUtils.computeReadingTime(post.content());
        final var authors = List.of(author);

		final var postEntity = postOutputMapper.toEntity(post, reading, authors, tags, projects);
		postEntity.setSlug(slug);
        return postOutputMapper.toModel(postRepository.save(postEntity));
    }

    @Transactional
    public PostModel update(PostModel post, List<TagModel> tags, List<ProjectModel> projects, AuthorModel author) {
        final var entity = postRepository.findByIdWithAuthor(post.id(), author.id(), isAdmin(author.role()))
                .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));
        postOutputMapper.updateEntity(post, entity);
		entity.setEstimatedReading(PostUtils.computeReadingTime(post.content()));

        if (post.title() != null && !post.title().equalsIgnoreCase(entity.getSlug())) {
            entity.setSlug(generateUniqueSlug(post.title(), entity.getId()));
        }

        if (tags != null) {
            entity.setTags(tagMapper.toEntitySet(tags));
        }

        if (projects != null) {
            entity.setProjects(projectMapper.toEntitySet(projects));
        }

        return postOutputMapper.toModel(postRepository.save(entity));
    }

    @Transactional
    public void deleteAll(List<UUID> ids, AuthorModel author) {
        postRepository.deleteByIdWithAuthor(ids, author.id(), isAdmin(author.role()));
    }

	public PaginatedOutput<PostModel> search(PaginatedInput<PostQueryModel> pageableInput, AuthorModel author) {
		final var pageable = PageRequest.of(pageableInput.page(), pageableInput.size());
		final var result = postRepository.search(pageableInput.query().query(), pageableInput.query().authorId(), author == null, pageable, mapSortProperty(pageableInput.sort(), pageableInput.direction()))
			.map(postOutputMapper::toModel);

		return new PaginatedOutput<>(
			result.getContent(),
			result.getNumber(),
			result.getSize(),
			result.getTotalElements(),
			result.getTotalPages()
		);
	}

    private String generateUniqueSlug(String title, UUID currentId) {
        final var base = PostUtils.slugify(title);
        final var counter = postRepository.countBySlugAndIdNot(base, currentId);
        if (counter == 0) {
            return base;
        }

        return base + "-" + counter + 1;
    }

    private String mapSortProperty(String sort, Sort.Direction direction) {
        return switch (sort) {
            case "slug" -> "slug";
			case "title" -> "title";
			case "viewCount" -> "view_count";
			case "reactionCount" -> "reactionCount";
            default -> "created_at " + direction.name() + " updated_at";
        } + " " + direction.name();
    }
}
