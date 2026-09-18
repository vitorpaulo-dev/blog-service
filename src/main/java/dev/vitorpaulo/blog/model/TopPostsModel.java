package dev.vitorpaulo.blog.model;

import java.util.List;

public record TopPostsModel(
    List<TopItemModel> allTime,
    List<TopItemModel> last24h
) {}
