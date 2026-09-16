package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.model.ReactionModel;
import dev.vitorpaulo.blog.model.ReactionType;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactToProjectUseCaseTest {

	private static final String IP = "203.0.113.7";
	private static final String PROJECT_SLUG = "my-project";

	@Mock private ProjectOutput projectOutput;
	@Mock private ReactionModel reactionModel;

	@InjectMocks
	private ReactToProjectUseCase reactToProjectUseCase;

	@ParameterizedTest
	@EnumSource(ReactionType.class)
	void execute_delegatesToOutput(ReactionType reactionType) {
		when(projectOutput.react(PROJECT_SLUG, reactionType, IP)).thenReturn(reactionModel);

		final var result = reactToProjectUseCase.execute(PROJECT_SLUG, reactionType, IP);

		assertEquals(reactionModel, result);
		verify(projectOutput).react(PROJECT_SLUG, reactionType, IP);
	}

	@Test
	void execute_unknownSlug_propagatesNotFoundException() {
		when(projectOutput.react(any(), any(), anyString())).thenThrow(new NotFoundException());

		assertThrows(NotFoundException.class,
			() -> reactToProjectUseCase.execute(PROJECT_SLUG, ReactionType.LOVE, IP));
	}
}
