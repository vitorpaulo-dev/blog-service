package dev.vitorpaulo.blog.input.response;

import java.util.List;

public record TopProjectsResponse(
    List<TopItemResponse> allTime,
    List<TopItemResponse> last24h
) {}
