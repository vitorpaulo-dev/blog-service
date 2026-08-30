package dev.vitorpaulo.blog.mapper;

import dev.vitorpaulo.blog.input.response.AuthorResponse;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.input.response.ProjectResponse;
import dev.vitorpaulo.blog.input.response.TagResponse;
import dev.vitorpaulo.blog.model.post.Author;
import dev.vitorpaulo.blog.model.post.Post;
import dev.vitorpaulo.blog.model.post.PostStatus;
import dev.vitorpaulo.blog.model.post.Project;
import dev.vitorpaulo.blog.model.post.Tag;
import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.domain.TagEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {

    // MapStruct maps PostStatus enum directly; String helpers kept for backwards compat if needed
    default PostStatus mapStatus(String status) {
        if (status == null) return PostStatus.DRAFT;
        try {
            return PostStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return PostStatus.DRAFT;
        }
    }

    default String mapStatus(PostStatus status) {
        return status == null ? "DRAFT" : status.name();
    }

    Author map(AuthorEntity entity);
    Tag map(TagEntity entity);
    Project map(ProjectEntity entity);

    AuthorEntity toEntity(Author model);
    TagEntity toEntity(Tag model);
    ProjectEntity toEntity(Project model);

    AuthorResponse mapToResponse(Author model);
    TagResponse mapToResponse(Tag model);
    ProjectResponse mapToResponse(Project model);

    // Entity -> Model
    @Mapping(target = "reactionCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    Post toModel(PostEntity entity);

    PostEntity toEntity(Post model);

    // Model -> Response
    PostResponse toResponse(Post model);

    // Entity -> Response direct
    @Mapping(target = "reactionCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    PostResponse toResponse(PostEntity entity);
}
