package dev.vitorpaulo.blog.usecase.dashboard;

import dev.vitorpaulo.blog.model.TopProjectsModel;
import dev.vitorpaulo.blog.output.dashboard.DashboardOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTopProjectsUseCase {

    private final DashboardOutput dashboardOutput;

    public TopProjectsModel execute(int limit) {
        return dashboardOutput.getTopProjects(limit);
    }
}
