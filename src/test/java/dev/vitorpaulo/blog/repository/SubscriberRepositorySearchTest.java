package dev.vitorpaulo.blog.repository;

import dev.vitorpaulo.blog.model.Frequency;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SubscriberRepositorySearchTest {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Test
    void search_withoutFilters_executesAgainstPostgres() {
        Page<?> result = subscriberRepository.search(null, null, null, null, PageRequest.of(0, 10));
        assertTrue(result.getTotalElements() >= 0);
    }

    @Test
    void search_withEmailFilter_executesAgainstPostgres() {
        Page<?> result = subscriberRepository.search("reader", null, null, null, PageRequest.of(0, 10));
        assertTrue(result.getTotalElements() >= 0);
    }

    @Test
    void search_withEmailAndEnumFilters_executesAgainstPostgres() {
        Page<?> result = subscriberRepository.search(
            "reader",
            SubscriberStatus.ACTIVE,
            Language.ENGLISH,
            Frequency.EVERY_POST,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        assertTrue(result.getTotalElements() >= 0);
    }

    @Test
    void search_withEnumFiltersOnly_executesAgainstPostgres() {
        Page<?> result = subscriberRepository.search(
            null,
            SubscriberStatus.ACTIVE,
            null,
            Frequency.MONTHLY_DIGEST,
            PageRequest.of(0, 10)
        );
        assertTrue(result.getTotalElements() >= 0);
    }
}
