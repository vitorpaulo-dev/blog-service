package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.model.ProjectStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static List<UUID> ids(int count) {
        return IntStream.range(0, count).mapToObj(i -> UUID.randomUUID()).toList();
    }

    @Test
    void batchRequests_idsOverTwenty_invalid() {
        var tagViolations = validator.validate(new TagBatchRequest(ids(21), Language.ENGLISH));
        var projectViolations = validator.validate(new ProjectBatchRequest(ids(21), Language.ENGLISH));

        assertFalse(tagViolations.isEmpty());
        assertFalse(projectViolations.isEmpty());
    }

    @Test
    void batchRequests_idsAtTwenty_valid() {
        var tagViolations = validator.validate(new TagBatchRequest(ids(20), Language.ENGLISH));
        var projectViolations = validator.validate(new ProjectBatchRequest(ids(20), Language.ENGLISH));

        assertTrue(tagViolations.isEmpty());
        assertTrue(projectViolations.isEmpty());
    }

    @Test
    void contentRequests_summaryOverFiveHundred_invalid() {
        var summary = "s".repeat(501);
        var postViolations = validator.validate(new PostContentRequest("Title", "Content", summary));
        var projectViolations = validator.validate(new ProjectContentRequest("Title", "Description", summary));

        assertFalse(postViolations.isEmpty());
        assertFalse(projectViolations.isEmpty());
    }

    @Test
    void contentRequests_summaryAtFiveHundred_valid() {
        var summary = "s".repeat(500);
        var postViolations = validator.validate(new PostContentRequest("Title", "Content", summary));
        var projectViolations = validator.validate(new ProjectContentRequest("Title", "Description", summary));

        assertTrue(postViolations.isEmpty());
        assertTrue(projectViolations.isEmpty());
    }

    @Test
    void postAndProjectRequests_tagIdsOverThree_invalid() {
        var createPost = validator.validate(new CreatePostRequest(
                null, Map.of(Language.ENGLISH, new PostContentRequest("Title", "Content", "Summary")), ids(4), null, null));
        var updatePost = validator.validate(new UpdatePostRequest(
                null, Map.of(Language.ENGLISH, new PostContentRequest("Title", "Content", "Summary")), ids(4), null, PostStatus.DRAFT, null));
        var createProject = validator.validate(new CreateProjectRequest(
                null, null, null, null, ids(4), Map.of(Language.ENGLISH, new ProjectContentRequest("Title", "Description", "Summary")), null));
        var updateProject = validator.validate(new UpdateProjectRequest(
                null, null, null, null, ids(4), Map.of(Language.ENGLISH, new ProjectContentRequest("Title", "Description", "Summary")), ProjectStatus.DRAFT));

        assertFalse(createPost.isEmpty());
        assertFalse(updatePost.isEmpty());
        assertFalse(createProject.isEmpty());
        assertFalse(updateProject.isEmpty());
    }

    @Test
    void postAndProjectRequests_tagIdsAtThree_valid() {
        var createPost = validator.validate(new CreatePostRequest(
                null, Map.of(Language.ENGLISH, new PostContentRequest("Title", "Content", "Summary")), ids(3), null, null));
        var updatePost = validator.validate(new UpdatePostRequest(
                null, Map.of(Language.ENGLISH, new PostContentRequest("Title", "Content", "Summary")), ids(3), null, PostStatus.DRAFT, null));
        var createProject = validator.validate(new CreateProjectRequest(
                null, null, null, null, ids(3), Map.of(Language.ENGLISH, new ProjectContentRequest("Title", "Description", "Summary")), null));
        var updateProject = validator.validate(new UpdateProjectRequest(
                null, null, null, null, ids(3), Map.of(Language.ENGLISH, new ProjectContentRequest("Title", "Description", "Summary")), ProjectStatus.DRAFT));

        assertTrue(createPost.isEmpty());
        assertTrue(updatePost.isEmpty());
        assertTrue(createProject.isEmpty());
        assertTrue(updateProject.isEmpty());
    }
}
