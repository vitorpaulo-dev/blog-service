package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.input.mapper.UploadInputMapper;
import dev.vitorpaulo.blog.input.request.PresignUploadRequest;
import dev.vitorpaulo.blog.input.request.SignUrlsRequest;
import dev.vitorpaulo.blog.input.response.PresignUploadResponse;
import dev.vitorpaulo.blog.model.upload.SignedUrlModel;
import dev.vitorpaulo.blog.usecase.upload.CreatePresignedUploadUseCase;
import dev.vitorpaulo.blog.usecase.upload.SignUploadKeysUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/upload")
@RequiredArgsConstructor
public class UploadController {

	private final CreatePresignedUploadUseCase createPresignedUploadUseCase;
	private final SignUploadKeysUseCase signUploadKeysUseCase;
	private final UploadInputMapper uploadInputMapper;

	@PostMapping("/presign")
	public PresignUploadResponse presign(@Valid @RequestBody PresignUploadRequest request) {
		return uploadInputMapper.toResponse(createPresignedUploadUseCase.execute(uploadInputMapper.toModel(request)));
	}

	@PostMapping("/sign")
	public Map<String, String> sign(@Valid @RequestBody SignUrlsRequest request) {
		return signUploadKeysUseCase.execute(request.keys()).stream()
			.collect(Collectors.toMap(SignedUrlModel::key, SignedUrlModel::url, (a, b) -> a));
	}
}
