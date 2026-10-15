package dev.vitorpaulo.blog.config.cache;

import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import dev.vitorpaulo.blog.output.post.PostReadOutput;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.output.project.ProjectReadOutput;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheInvalidationMatrixTest {

	@Test
	void findFeatured_isCachedOnFeaturedPosts() {
		assertCacheable(PostOutput.class, "findFeatured", CacheConfig.FEATURED_POSTS, "@cacheKeys.featured(#language)");
	}

	@Test
	void postSearch_isCachedOnPostSearch() {
		assertCacheable(PostOutput.class, "search", CacheConfig.POST_SEARCH, "@cacheKeys.postSearch(#pageableInput, #author)");
	}

	@Test
	void postFindBySlug_isCachedOnPostSlug() {
		assertCacheable(PostReadOutput.class, "findBySlug", CacheConfig.POST_SLUG, "@cacheKeys.slug(#slug, #language, #authenticated)");
	}

	@Test
	void projectFindBySlug_isCachedOnProjectSlug() {
		assertCacheable(ProjectReadOutput.class, "findBySlug", CacheConfig.PROJECT_SLUG, "@cacheKeys.slug(#slug, #language, #authenticated)");
	}

	@Test
	void projectSearch_isCachedOnProjectSearch() {
		assertCacheable(ProjectOutput.class, "search", CacheConfig.PROJECT_SEARCH, "@cacheKeys.projectSearch(#pageableInput, #author)");
	}

	@Test
	void projectFindAllById_isCachedOnProjectBatch() {
		assertCacheable(ProjectOutput.class, "findAllById", CacheConfig.PROJECT_BATCH, "@cacheKeys.batch(#ids, #language)");
	}

	@Test
	void tagSearch_isCachedOnTagSearch() {
		assertCacheable(TagOutput.class, "search", CacheConfig.TAG_SEARCH, "@cacheKeys.tagSearch(#input, #language)");
	}

	@Test
	void tagFindAllById_isCachedOnTagBatch() {
		assertCacheable(TagOutput.class, "findAllById", CacheConfig.TAG_BATCH, "@cacheKeys.batch(#ids, #language)");
	}

	@Test
	void postWrapperFindBySlug_isNotAnnotated() {
		var method = findMethod(PostOutput.class, "findBySlug");
		assertNull(method.getAnnotation(Cacheable.class));
		assertNull(method.getAnnotation(CacheEvict.class));
	}

	@Test
	void postMutations_evictFeaturedSearchAndSlug() {
		assertEvicts(PostOutput.class, "save", CacheConfig.FEATURED_POSTS, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG);
		assertEvicts(PostOutput.class, "update", CacheConfig.FEATURED_POSTS, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG);
		assertEvicts(PostOutput.class, "deleteByIds", CacheConfig.FEATURED_POSTS, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG);
		assertEvicts(PostOutput.class, "setFeaturedWeights", CacheConfig.FEATURED_POSTS, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG);
	}

	@Test
	void postReact_evictsSlugOnly() {
		assertEvicts(PostOutput.class, "react", CacheConfig.POST_SLUG);
	}

	@Test
	void projectMutations_evictProjectSearchSlugBatch() {
		assertEvicts(ProjectOutput.class, "save", CacheConfig.PROJECT_SEARCH, CacheConfig.PROJECT_SLUG, CacheConfig.PROJECT_BATCH);
		assertEvicts(ProjectOutput.class, "update", CacheConfig.PROJECT_SEARCH, CacheConfig.PROJECT_SLUG, CacheConfig.PROJECT_BATCH);
	}

	@Test
	void projectDeleteAll_additionallyEvictsPostSlug() {
		assertEvicts(ProjectOutput.class, "deleteAll",
			CacheConfig.PROJECT_SEARCH, CacheConfig.PROJECT_SLUG, CacheConfig.PROJECT_BATCH, CacheConfig.POST_SLUG);
	}

	@Test
	void projectReact_evictsSlugOnly() {
		assertEvicts(ProjectOutput.class, "react", CacheConfig.PROJECT_SLUG);
	}

	@Test
	void tagMutations_evictTagSearchAndBatch() {
		assertEvicts(TagOutput.class, "save", CacheConfig.TAG_SEARCH, CacheConfig.TAG_BATCH);
		assertEvicts(TagOutput.class, "update", CacheConfig.TAG_SEARCH, CacheConfig.TAG_BATCH);
	}

	@Test
	void tagDeleteAll_additionallyEvictsPostSearchAndSlug() {
		assertEvicts(TagOutput.class, "deleteAll",
			CacheConfig.TAG_SEARCH, CacheConfig.TAG_BATCH, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG);
	}

	@Test
	void audioRetry_evictsPostSlug() {
		assertEvicts(AudioOutput.class, "retry", CacheConfig.POST_SLUG);
	}

	private void assertCacheable(Class<?> owner, String methodName, String cacheName, String keyExpression) {
		var annotation = findMethod(owner, methodName).getAnnotation(Cacheable.class);
		assertNotNull(annotation, methodName + " on " + owner.getSimpleName() + " must be @Cacheable");
		assertEquals(Set.of(cacheName), Set.of(annotation.cacheNames()));
		assertEquals(keyExpression, annotation.key());
	}

	private void assertEvicts(Class<?> owner, String methodName, String... cacheNames) {
		var annotation = findMethod(owner, methodName).getAnnotation(CacheEvict.class);
		assertNotNull(annotation, methodName + " on " + owner.getSimpleName() + " must be @CacheEvict");
		assertTrue(annotation.allEntries(), methodName + " on " + owner.getSimpleName() + " must use allEntries");
		assertEquals(Set.of(cacheNames), Set.of(annotation.cacheNames()));
	}

	private java.lang.reflect.Method findMethod(Class<?> owner, String methodName) {
		for (var method : owner.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		throw new AssertionError("Method " + methodName + " not found on " + owner.getSimpleName());
	}
}
