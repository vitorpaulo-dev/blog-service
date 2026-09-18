package dev.vitorpaulo.blog.model;

import java.util.List;

public record TopProjectsModel(
    List<TopItemModel> allTime,
    List<TopItemModel> last24h
) {}
