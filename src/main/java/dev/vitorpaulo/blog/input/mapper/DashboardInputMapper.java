package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.input.response.DashboardStatsResponse;
import dev.vitorpaulo.blog.input.response.TopPostsResponse;
import dev.vitorpaulo.blog.input.response.TopProjectsResponse;
import dev.vitorpaulo.blog.input.response.TopItemResponse;
import dev.vitorpaulo.blog.model.DashboardStatsModel;
import dev.vitorpaulo.blog.model.TopPostsModel;
import dev.vitorpaulo.blog.model.TopProjectsModel;
import dev.vitorpaulo.blog.model.TopItemModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DashboardInputMapper {

    DashboardStatsResponse toResponse(DashboardStatsModel model);

    TopItemResponse toResponse(TopItemModel model);

    TopPostsResponse toResponse(TopPostsModel model);

    TopProjectsResponse toResponse(TopProjectsModel model);
}
