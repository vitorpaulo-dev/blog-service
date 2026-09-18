package dev.vitorpaulo.blog.usecase.dashboard;

import dev.vitorpaulo.blog.model.DashboardStatsModel;
import dev.vitorpaulo.blog.output.dashboard.DashboardOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetDashboardStatsUseCase {

    private final DashboardOutput dashboardOutput;

    public DashboardStatsModel execute() {
        return dashboardOutput.getStats();
    }
}
