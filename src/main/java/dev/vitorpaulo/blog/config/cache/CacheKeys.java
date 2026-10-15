package dev.vitorpaulo.blog.config.cache;

import dev.vitorpaulo.blog.config.security.AuthorRoleChecker;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostQueryModel;
import dev.vitorpaulo.blog.model.ProjectQueryModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CacheKeys {

	private static final String ANONYMOUS = "anonymous";
	private static final String NULL_MARKER = "-";
	private static final String BATCH = "batch";

	private final AuthorRoleChecker authorRoleChecker;

	public String postSearch(PaginatedInput<PostQueryModel> input, AuthorModel author) {
		final var canonical = new StringBuilder();
		append(canonical, CacheConfig.POST_SEARCH);
		appendPagination(canonical, input);

		final var query = input.query();
		append(canonical, query == null ? null : query.query());
		append(canonical, query == null ? null : query.authorId());
		append(canonical, query == null ? null : query.language());
		append(canonical, query == null ? null : query.tagId());
		appendCaller(canonical, author);

		return digest(CacheConfig.POST_SEARCH, canonical);
	}

	public String projectSearch(PaginatedInput<ProjectQueryModel> input, AuthorModel author) {
		final var canonical = new StringBuilder();
		append(canonical, CacheConfig.PROJECT_SEARCH);
		appendPagination(canonical, input);

		final var query = input.query();
		append(canonical, query == null ? null : query.query());
		append(canonical, query == null ? null : query.authorId());
		append(canonical, query == null ? null : query.language());
		append(canonical, query == null ? null : query.tagId());
		appendCaller(canonical, author);

		return digest(CacheConfig.PROJECT_SEARCH, canonical);
	}

	public String tagSearch(PaginatedInput<TagQueryModel> input, Language language) {
		final var canonical = new StringBuilder();
		append(canonical, CacheConfig.TAG_SEARCH);
		appendPagination(canonical, input);

		final var query = input.query();
		append(canonical, query == null ? null : query.name());
		append(canonical, query == null ? null : query.language());
		append(canonical, language);

		return digest(CacheConfig.TAG_SEARCH, canonical);
	}

	public String batch(List<UUID> ids, Language language) {
		final var canonical = new StringBuilder();
		append(canonical, BATCH);
		if (ids == null) {
			append(canonical, null);
		} else {
			ids.forEach(id -> append(canonical, id));
		}
		append(canonical, language);

		return digest(BATCH, canonical);
	}

	public String featured(Language language) {
		return CacheConfig.FEATURED_POSTS + ":" + name(language);
	}

	public String slug(String slug, Language language, boolean authenticated) {
		return slug + ":" + name(language) + ":" + authenticated;
	}

	private void appendPagination(StringBuilder canonical, PaginatedInput<?> input) {
		append(canonical, input.page());
		append(canonical, input.size());
		append(canonical, input.sort());
		append(canonical, input.direction());
	}

	private void appendCaller(StringBuilder canonical, AuthorModel author) {
		append(canonical, author == null ? ANONYMOUS : author.id());
		append(canonical, authorRoleChecker.isAdmin());
	}

	private void append(StringBuilder canonical, Object value) {
		if (value == null) {
			canonical.append(NULL_MARKER);
			return;
		}
		final var text = String.valueOf(value);
		canonical.append(text.length()).append(':').append(text);
	}

	private String digest(String prefix, StringBuilder canonical) {
		try {
			final var digest = MessageDigest.getInstance("SHA-256");
			final var hex = HexFormat.of().formatHex(digest.digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
			return prefix + ":" + hex;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm unavailable", e);
		}
	}

	private static String name(Language language) {
		return language == null ? NULL_MARKER : language.name();
	}
}
