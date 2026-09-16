package dev.vitorpaulo.blog.repository;

import dev.vitorpaulo.blog.domain.SubscriberEntity;
import dev.vitorpaulo.blog.model.Frequency;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SubscriberRepository extends JpaRepository<SubscriberEntity, UUID> {

    Optional<SubscriberEntity> findByEmail(String email);

    @Query("""
        SELECT s
        FROM SubscriberEntity s
        WHERE (cast(:email as string) IS NULL OR LOWER(s.email) LIKE LOWER(CONCAT('%', cast(:email as string), '%')))
          AND (cast(:status as string) IS NULL OR s.status = cast(:status as string))
          AND (cast(:language as string) IS NULL OR s.language = cast(:language as string))
          AND (cast(:frequency as string) IS NULL OR s.frequency = cast(:frequency as string))
    """)
    Page<SubscriberEntity> search(
        @Param("email") String email,
        @Param("status") SubscriberStatus status,
        @Param("language") Language language,
        @Param("frequency") Frequency frequency,
        Pageable pageable
    );
}
