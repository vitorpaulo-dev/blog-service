package dev.vitorpaulo.blog.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityCounterDefaultsTest {

    @Test
    void newPostEntity_initializesCountersToZeroSoJpaIgnoresInsertsOfExplicitNulls() {
        var post = new PostEntity();

        assertEquals(0L, post.getViewCount());
        assertEquals(0L, post.getLoveCount());
        assertEquals(0L, post.getCelebrateCount());
        assertEquals(0L, post.getGeniusCount());
        assertEquals(0L, post.getHelpCount());
    }

    @Test
    void newProjectEntity_initializesCountersToZero() {
        var project = new ProjectEntity();

        assertEquals(0L, project.getViewCount());
        assertEquals(0L, project.getLoveCount());
        assertEquals(0L, project.getCelebrateCount());
        assertEquals(0L, project.getGeniusCount());
        assertEquals(0L, project.getHelpCount());
    }
}
