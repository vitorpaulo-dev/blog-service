package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.domain.SubscriberEntity;
import dev.vitorpaulo.blog.model.SubscriberModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubscriberOutputMapper {

    SubscriberModel toModel(SubscriberEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubscriberEntity toEntity(SubscriberModel model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(SubscriberModel model, @MappingTarget SubscriberEntity entity);
}
