package dev.vitorpaulo.blog.usecase.upload;

import dev.vitorpaulo.blog.model.upload.SignedUrlModel;
import dev.vitorpaulo.blog.output.upload.StorageOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignUploadKeysUseCaseTest {

	@InjectMocks
	private SignUploadKeysUseCase useCase;

	@Mock
	private StorageOutput storageOutput;

	@Test
	void filtersOutDisallowedKeysAndDuplicates() {
		when(storageOutput.signKeys(List.of("post/banner/a.jpg")))
			.thenReturn(List.of(new SignedUrlModel("post/banner/a.jpg", "https://signed/a")));

		final var result = useCase.execute(List.of(
			"post/banner/a.jpg",
			"../../etc/passwd",
			"post/banner/a.jpg",
			"random/url.jpg"
		));

		assertEquals(1, result.size());
		assertEquals("post/banner/a.jpg", result.get(0).key());
		assertEquals("https://signed/a", result.get(0).url());
		verify(storageOutput).signKeys(List.of("post/banner/a.jpg"));
	}

	@Test
	void noAllowedKeys_returnsEmptyWithoutCallingOutput() {
		final var result = useCase.execute(List.of("evil/key.jpg"));

		assertEquals(0, result.size());
		verify(storageOutput, never()).signKeys(java.util.Collections.emptyList());
	}

	@Test
	void eachFolderIsAllowed() {
		when(storageOutput.signKeys(List.of(
			"post/banner/a.jpg",
			"post/content/b.jpg",
			"project/logo/c.png",
			"project/banner/d.png",
			"project/content/e.png"
		))).thenReturn(List.of());

		useCase.execute(List.of(
			"post/banner/a.jpg",
			"post/content/b.jpg",
			"project/logo/c.png",
			"project/banner/d.png",
			"project/content/e.png"
		));

		verify(storageOutput).signKeys(List.of(
			"post/banner/a.jpg",
			"post/content/b.jpg",
			"project/logo/c.png",
			"project/banner/d.png",
			"project/content/e.png"
		));
	}
}
