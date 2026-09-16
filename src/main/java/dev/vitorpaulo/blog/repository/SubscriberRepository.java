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
        WHERE (:email IS NULL OR LOWER(s.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:status IS NULL OR s.status = :status)
          AND (:language IS NULL OR s.language = :language)
          AND (:frequency IS NULL OR s.frequency = :frequency)
    """)
    Page<SubscriberEntity> search(
        @Param("email") String email,
        @Param("status") SubscriberStatus status,
        @Param("language") Language language,
        @Param("frequency") Frequency frequency,
        Pageable pageable
    );
}
