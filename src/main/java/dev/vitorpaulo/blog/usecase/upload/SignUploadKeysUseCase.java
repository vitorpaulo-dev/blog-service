package dev.vitorpaulo.blog.usecase.upload;

import dev.vitorpaulo.blog.model.upload.SignedUrlModel;
import dev.vitorpaulo.blog.model.upload.UploadFolder;
import dev.vitorpaulo.blog.output.upload.StorageOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SignUploadKeysUseCase {

	private final StorageOutput storageOutput;

	public List<SignedUrlModel> execute(List<String> keys) {
		final var allowedKeys = keys.stream()
			.filter(UploadFolder::isAllowedKey)
			.distinct()
			.toList();
		if (allowedKeys.isEmpty()) {
			return List.of();
		}

		return storageOutput.signKeys(allowedKeys);
	}
}
