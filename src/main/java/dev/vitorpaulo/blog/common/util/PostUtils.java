package dev.vitorpaulo.blog.common.util;

import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.Locale;

public final class PostUtils {

	public static Long computeReadingTime(String content) {
		if (StringUtils.isBlank(content)) return 0L;

		final var words = content.trim().split("\\s+").length;
		return (long) Math.ceil(words / 200.0 * 60);
	}

	public static String slugify(String input) {
		if (StringUtils.isBlank(input)) return "post";

		final var normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
			.replaceAll("\\p{M}+", "");
		var slug = normalized
			.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]+", "-")
			.replaceAll("^-|-$", "");
		if (slug.isBlank()) return "post";

		if (slug.length() > 200) {
			slug = slug.replaceAll("-$", "").substring(0, 200);
		}

		return slug;
	}
}
