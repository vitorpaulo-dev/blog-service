package dev.vitorpaulo.blog.common.util;

import java.text.Normalizer;
import java.util.Locale;

public final class PostUtils {

    private PostUtils() {}

    public static int[] computeReadingTime(String content) {
        if (content == null || content.isBlank()) return new int[]{0, 0};
        int words = content.trim().split("\\s+").length;
        int minutes = (int) Math.ceil(words / 200.0);
        return new int[]{minutes * 60, minutes};
    }

    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String lower = normalized.toLowerCase(Locale.ROOT);
        String slug = lower.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (slug.isBlank()) slug = "post";
        if (slug.length() > 200) slug = slug.substring(0, 200).replaceAll("-$", "");
        return slug;
    }
}
