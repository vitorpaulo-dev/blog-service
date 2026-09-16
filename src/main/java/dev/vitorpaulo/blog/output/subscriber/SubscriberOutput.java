package dev.vitorpaulo.blog.output.subscriber;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.SubscriberEntity;
import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.mapper.SubscriberOutputMapper;
import dev.vitorpaulo.blog.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubscriberOutput {

    private final SubscriberRepository subscriberRepository;
    private final SubscriberOutputMapper subscriberOutputMapper;

    @Transactional
    public SubscriberModel save(SubscriberModel model) {
        final var entity = subscriberOutputMapper.toEntity(model);
        return subscriberOutputMapper.toModel(subscriberRepository.save(entity));
    }

    @Transactional
    public SubscriberModel update(SubscriberModel model) {
        final var entity = subscriberRepository.findById(model.id())
            .orElseThrow(() -> new NotFoundException(ExceptionCode.SUBSCRIBER_NOT_FOUND));

        subscriberOutputMapper.updateEntity(model, entity);

        return subscriberOutputMapper.toModel(subscriberRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Optional<SubscriberModel> findByEmail(String email) {
        return subscriberRepository.findByEmail(email)
            .map(subscriberOutputMapper::toModel);
    }

    @Transactional(readOnly = true)
    public SubscriberModel findById(UUID id) {
        return subscriberRepository.findById(id)
            .map(subscriberOutputMapper::toModel)
            .orElseThrow(() -> new NotFoundException(ExceptionCode.SUBSCRIBER_NOT_FOUND));
    }

    public PaginatedOutput<SubscriberModel> search(PaginatedInput<SubscriberQueryModel> input) {
        final var query = input.query();
        final var pageable = PageRequest.of(input.page(), input.size(), Sort.by(input.direction(), mapSortProperty(input.sort())));
        final var result = subscriberRepository.search(
            query.email(),
            query.status(),
            query.language(),
            query.frequency(),
            pageable
        );

        return new PaginatedOutput<>(
            result.getContent().stream()
                .map(subscriberOutputMapper::toModel)
                .toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    private String mapSortProperty(String sort) {
        return switch (sort != null ? sort : "") {
            case "email", "status", "language", "frequency" -> sort;
            default -> "createdAt";
        };
    }
}
