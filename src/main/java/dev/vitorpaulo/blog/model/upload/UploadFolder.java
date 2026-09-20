package dev.vitorpaulo.blog.model.upload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum UploadFolder {

	POST_AUDIO("post", "audio"),
	POST_BANNER("post", "banner"),
	POST_CONTENT("post", "content"),
	PROJECT_LOGO("project", "logo"),
	PROJECT_BANNER("project", "banner"),
	PROJECT_CONTENT("project", "content");

	private final String folder;
	private final String subfolder;

	public String getPrefix() {
		return folder + "/" + subfolder + "/";
	}

	public boolean matchesToPrefix(String key) {
		return key != null && key.startsWith(getPrefix());
	}

	public static boolean isAllowedKey(String key) {
		return Arrays.stream(values()).anyMatch(f -> f.matchesToPrefix(key));
	}
}
