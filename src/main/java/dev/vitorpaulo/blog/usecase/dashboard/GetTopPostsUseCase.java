package dev.vitorpaulo.blog.usecase.dashboard;

import dev.vitorpaulo.blog.model.TopPostsModel;
import dev.vitorpaulo.blog.output.dashboard.DashboardOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTopPostsUseCase {

    private final DashboardOutput dashboardOutput;

    public TopPostsModel execute(int limit) {
        return dashboardOutput.getTopPosts(limit);
    }
}
