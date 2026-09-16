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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriberOutputTest {

    @Mock private SubscriberRepository subscriberRepository;
    @Mock private SubscriberOutputMapper subscriberOutputMapper;
    @Mock private SubscriberEntity subscriberEntity;
    @Mock private SubscriberModel subscriberModel;
    @Mock private SubscriberEntity secondSubscriberEntity;
    @Mock private SubscriberModel secondSubscriberModel;

    @InjectMocks
    private SubscriberOutput subscriberOutput;

    @Test
    void save_mapsAndSaves() {
        when(subscriberOutputMapper.toEntity(subscriberModel)).thenReturn(subscriberEntity);
        when(subscriberRepository.save(subscriberEntity)).thenReturn(subscriberEntity);
        when(subscriberOutputMapper.toModel(subscriberEntity)).thenReturn(subscriberModel);

        final var result = subscriberOutput.save(subscriberModel);

        assertEquals(subscriberModel, result);
    }

    @Test
    void update_existingEntity_updatesFields() {
        var id = UUID.randomUUID();
        var model = new SubscriberModel(id, "reader@example.com", null, null, null, null, null);
        when(subscriberRepository.findById(id)).thenReturn(Optional.of(subscriberEntity));
        when(subscriberRepository.save(subscriberEntity)).thenReturn(subscriberEntity);
        when(subscriberOutputMapper.toModel(subscriberEntity)).thenReturn(subscriberModel);

        var result = subscriberOutput.update(model);

        assertEquals(subscriberModel, result);
        org.mockito.Mockito.verify(subscriberOutputMapper).updateEntity(model, subscriberEntity);
    }

    @Test
    void update_unknownId_throwsNotFoundException() {
        var id = UUID.randomUUID();
        var model = new SubscriberModel(id, "reader@example.com", null, null, null, null, null);
        when(subscriberRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> subscriberOutput.update(model));
        assertEquals(ExceptionCode.SUBSCRIBER_NOT_FOUND, exception.getCode());
    }

    @Test
    void findByEmail_found_returnsModel() {
        when(subscriberRepository.findByEmail("reader@example.com")).thenReturn(Optional.of(subscriberEntity));
        when(subscriberOutputMapper.toModel(subscriberEntity)).thenReturn(subscriberModel);

        var result = subscriberOutput.findByEmail("reader@example.com");

        assertTrue(result.isPresent());
        assertEquals(subscriberModel, result.get());
    }

    @Test
    void findByEmail_missing_returnsEmpty() {
        when(subscriberRepository.findByEmail("reader@example.com")).thenReturn(Optional.empty());

        assertTrue(subscriberOutput.findByEmail("reader@example.com").isEmpty());
    }

    @Test
    void findById_found_returnsModel() {
        var id = UUID.randomUUID();
        when(subscriberRepository.findById(id)).thenReturn(Optional.of(subscriberEntity));
        when(subscriberOutputMapper.toModel(subscriberEntity)).thenReturn(subscriberModel);

        var result = subscriberOutput.findById(id);

        assertEquals(subscriberModel, result);
    }

    @Test
    void findById_unknown_throwsNotFoundException() {
        when(subscriberRepository.findById(any())).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> subscriberOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.SUBSCRIBER_NOT_FOUND, exception.getCode());
    }

    @Test
    void search_mapsFiltersAndPagination() {
        var query = new SubscriberQueryModel("reader", null, null, null);
        var input = new PaginatedInput<SubscriberQueryModel>(query, 2, 10, "email", org.springframework.data.domain.Sort.Direction.ASC);
        org.springframework.data.domain.Page<SubscriberEntity> page =
            new PageImpl<>(List.of(subscriberEntity, secondSubscriberEntity));
        when(subscriberRepository.search(org.mockito.ArgumentMatchers.eq("reader"), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any(PageRequest.class)))
            .thenReturn(page);
        when(subscriberOutputMapper.toModel(subscriberEntity)).thenReturn(subscriberModel);
        when(subscriberOutputMapper.toModel(secondSubscriberEntity)).thenReturn(secondSubscriberModel);

        var result = subscriberOutput.search(input);

        var captor = ArgumentCaptor.forClass(PageRequest.class);
        org.mockito.Mockito.verify(subscriberRepository).search(org.mockito.ArgumentMatchers.eq("reader"), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
        org.junit.jupiter.api.Assertions.assertTrue(captor.getValue().getSort().getOrderFor("email").isAscending());
        assertEquals(List.of(subscriberModel, secondSubscriberModel), result.content());
    }

    @Test
    void search_unknownSortPropertyDefaultsToCreatedAt() {
        var query = new SubscriberQueryModel(null, null, null, null);
        var input = new PaginatedInput<SubscriberQueryModel>(query, 0, 10, null, org.springframework.data.domain.Sort.Direction.DESC);
        org.springframework.data.domain.Page<SubscriberEntity> page = new PageImpl<>(List.of());
        when(subscriberRepository.search(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any(PageRequest.class)))
            .thenReturn(page);

        var result = subscriberOutput.search(input);

        var captor = ArgumentCaptor.forClass(PageRequest.class);
        org.mockito.Mockito.verify(subscriberRepository).search(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.isNull(), captor.capture());
        org.junit.jupiter.api.Assertions.assertTrue(captor.getValue().getSort().getOrderFor("createdAt").isDescending());
        assertTrue(result.content().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
}
