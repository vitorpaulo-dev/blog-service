package dev.vitorpaulo.blog.config.cache;

import dev.vitorpaulo.blog.config.security.AuthorRoleChecker;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostQueryModel;
import dev.vitorpaulo.blog.model.ProjectQueryModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheKeysTest {

    @Mock private AuthorRoleChecker authorRoleChecker;
    @Mock private AuthorModel author;
    @Mock private AuthorModel otherAuthor;
    @Mock private PostQueryModel postQuery;
    @Mock private ProjectQueryModel projectQuery;
    @Mock private TagQueryModel tagQuery;

    @InjectMocks
    private CacheKeys cacheKeys;

    @Test
    void postSearch_sameInput_producesSameKey() {
        when(authorRoleChecker.isAdmin()).thenReturn(false);

        var first = cacheKeys.postSearch(postInput(postQuery, 0, 10, "createdAt", Sort.Direction.DESC), null);
        var second = cacheKeys.postSearch(postInput(postQuery, 0, 10, "createdAt", Sort.Direction.DESC), null);

        assertEquals(first, second);
    }

    @Test
    void postSearch_anonymousVsAuthor_producesDifferentKeys() {
        var authorId = UUID.randomUUID();
        when(authorRoleChecker.isAdmin()).thenReturn(false);
        when(author.id()).thenReturn(authorId);

        var input = postInput(postQuery, 0, 10, "createdAt", Sort.Direction.DESC);

        assertNotEquals(cacheKeys.postSearch(input, null), cacheKeys.postSearch(input, author));
    }

    @Test
    void postSearch_twoAuthors_produceDifferentKeys() {
        when(authorRoleChecker.isAdmin()).thenReturn(false);
        when(author.id()).thenReturn(UUID.randomUUID());
        when(otherAuthor.id()).thenReturn(UUID.randomUUID());

        var input = postInput(postQuery, 0, 10, "createdAt", Sort.Direction.DESC);

        assertNotEquals(cacheKeys.postSearch(input, author), cacheKeys.postSearch(input, otherAuthor));
    }

    @Test
    void postSearch_adminVsMember_produceDifferentKeys() {
        when(author.id()).thenReturn(UUID.randomUUID());

        var input = postInput(postQuery, 0, 10, "createdAt", Sort.Direction.DESC);

        when(authorRoleChecker.isAdmin()).thenReturn(false);
        var memberKey = cacheKeys.postSearch(input, author);

        when(authorRoleChecker.isAdmin()).thenReturn(true);
        var adminKey = cacheKeys.postSearch(input, author);

        assertNotEquals(memberKey, adminKey);
    }

    @Test
    void postSearch_nullQueryAndAllNullFields_produceSameKey() {
        when(authorRoleChecker.isAdmin()).thenReturn(false);

        var nullQuery = new PaginatedInput<PostQueryModel>(null, 0, 10, null, null);
        var emptyQuery = postInput(postQuery, 0, 10, null, null);

        assertEquals(cacheKeys.postSearch(nullQuery, null), cacheKeys.postSearch(emptyQuery, null));
    }

    @Test
    void postSearch_sortAndDirectionVariations_produceDifferentKeys() {
        when(authorRoleChecker.isAdmin()).thenReturn(false);

        var noSort = new PaginatedInput<PostQueryModel>(null, 0, 10, null, null);
        var sorted = postInput(postQuery, 0, 10, "createdAt", Sort.Direction.ASC);
        var reversed = postInput(postQuery, 0, 10, "createdAt", Sort.Direction.DESC);

        assertNotEquals(cacheKeys.postSearch(noSort, null), cacheKeys.postSearch(sorted, null));
        assertNotEquals(cacheKeys.postSearch(sorted, null), cacheKeys.postSearch(reversed, null));
    }

    @Test
    void postSearch_languageVariations_produceDifferentKeys() {
        when(authorRoleChecker.isAdmin()).thenReturn(false);
        when(postQuery.language()).thenReturn(Language.ENGLISH);

        var english = cacheKeys.postSearch(postInput(postQuery, 0, 10, null, null), null);

        when(postQuery.language()).thenReturn(Language.PORTUGUESE);
        var portuguese = cacheKeys.postSearch(postInput(postQuery, 0, 10, null, null), null);

        assertNotEquals(english, portuguese);
    }

    @Test
    void postSearch_isLanguageIsolatedFromProjectSearch() {
        when(authorRoleChecker.isAdmin()).thenReturn(false);

        var postKey = cacheKeys.postSearch(new PaginatedInput<>(null, 0, 10, null, null), null);
        var projectKey = cacheKeys.projectSearch(new PaginatedInput<>(null, 0, 10, null, null), null);

        assertNotEquals(postKey, projectKey);
        assertTrue(postKey.startsWith(CacheConfig.POST_SEARCH + ":"));
        assertTrue(projectKey.startsWith(CacheConfig.PROJECT_SEARCH + ":"));
    }

    @Test
    void projectSearch_anonymousVsAdmin_produceDifferentKeys() {
        var input = new PaginatedInput<>(projectQuery, 0, 10, null, null);

        when(authorRoleChecker.isAdmin()).thenReturn(false);
        var anonymousKey = cacheKeys.projectSearch(input, null);

        when(authorRoleChecker.isAdmin()).thenReturn(true);
        var adminKey = cacheKeys.projectSearch(input, null);

        assertNotEquals(anonymousKey, adminKey);
    }

    @Test
    void projectSearch_sameInput_producesSameKey() {
        when(authorRoleChecker.isAdmin()).thenReturn(false);

        var first = cacheKeys.projectSearch(new PaginatedInput<>(projectQuery, 0, 10, "viewCount", Sort.Direction.ASC), null);
        var second = cacheKeys.projectSearch(new PaginatedInput<>(projectQuery, 0, 10, "viewCount", Sort.Direction.ASC), null);

        assertEquals(first, second);
    }

    @Test
    void tagSearch_noAuthenticationDependency() {
        var input = new PaginatedInput<>(tagQuery, 0, 10, null, null);

        assertDoesNotThrow(() -> cacheKeys.tagSearch(input, Language.ENGLISH));
        verifyNoInteractions(authorRoleChecker);
    }

    @Test
    void tagSearch_nameAndLanguageVariations_produceDifferentKeys() {
        var base = new PaginatedInput<>(tagQuery, 0, 10, null, null);
        var englishKey = cacheKeys.tagSearch(base, Language.ENGLISH);

        when(tagQuery.name()).thenReturn("spring");
        assertNotEquals(englishKey, cacheKeys.tagSearch(base, Language.ENGLISH));

        when(tagQuery.name()).thenReturn(null);
        assertNotEquals(englishKey, cacheKeys.tagSearch(base, Language.PORTUGUESE));
    }

    @Test
    void tagSearch_paginationVariations_produceDifferentKeys() {
        var first = new PaginatedInput<TagQueryModel>(null, 0, 10, null, null);
        var second = new PaginatedInput<TagQueryModel>(null, 1, 10, null, null);
        var third = new PaginatedInput<>(tagQuery, 0, 10, "name", Sort.Direction.ASC);

        assertNotEquals(cacheKeys.tagSearch(first, null), cacheKeys.tagSearch(second, null));
        assertNotEquals(cacheKeys.tagSearch(first, null), cacheKeys.tagSearch(third, null));
    }

    @Test
    void batch_sameOrder_producesSameKeyAndLanguageIsPartOfKey() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        var english = cacheKeys.batch(ids, Language.ENGLISH);
        var repeated = cacheKeys.batch(ids, Language.ENGLISH);
        var portuguese = cacheKeys.batch(ids, Language.PORTUGUESE);

        assertEquals(english, repeated);
        assertNotEquals(english, portuguese);
    }

    @Test
    void batch_orderIsSensitive() {
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();

        assertNotEquals(cacheKeys.batch(List.of(first, second), Language.ENGLISH),
                cacheKeys.batch(List.of(second, first), Language.ENGLISH));
    }

    @Test
    void batch_nullIdsAndEmptyIds_produceDifferentKeys() {
        assertNotEquals(cacheKeys.batch(null, Language.ENGLISH), cacheKeys.batch(List.of(), Language.ENGLISH));
    }

    @Test
    void featured_usesReadableCacheNamePrefix() {
        assertEquals("featured-posts:ENGLISH", cacheKeys.featured(Language.ENGLISH));
        assertEquals("featured-posts:-", cacheKeys.featured(null));
    }

    @Test
    void slug_producesReadableDeterministicKey() {
        assertEquals("my-post:ENGLISH:true", cacheKeys.slug("my-post", Language.ENGLISH, true));
        assertEquals("my-post:ENGLISH:false", cacheKeys.slug("my-post", Language.ENGLISH, false));
        assertEquals("my-post:-:false", cacheKeys.slug("my-post", null, false));
    }

    private PaginatedInput<PostQueryModel> postInput(PostQueryModel query, int page, int size, String sort, Sort.Direction direction) {
        return new PaginatedInput<>(query, page, size, sort, direction);
    }
}
