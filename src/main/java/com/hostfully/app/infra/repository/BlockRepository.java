package com.hostfully.app.infra.repository;

import com.hostfully.app.infra.entity.BlockEntity;
import com.hostfully.app.infra.entity.PropertyEntity;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<BlockEntity, Long> {

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BlockEntity b"
            + "        WHERE b.property.externalId = :propertyId"
            + "        AND (:externalId IS NULL OR b.externalId <> :externalId)"
            + "        AND b.startDate < :endDate"
            + "        AND b.endDate > :startDate")
    Boolean hasOverlapping(String propertyId, LocalDate startDate, LocalDate endDate, String externalId);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlockEntity b WHERE b.externalId = :externalId")
    int deleteByExternalId(String externalId);

    @Modifying
    @Transactional
    @Query("UPDATE BlockEntity b SET b.property = :property, b.reason = :reason, b.startDate = :startDate, "
            + "b.endDate = :endDate WHERE b.externalId = :externalId")
    int updateByExternalId(
            PropertyEntity property, String reason, LocalDate startDate, LocalDate endDate, String externalId);

    boolean existsByExternalId(String externalId);

    @Query("SELECT b FROM BlockEntity b LEFT JOIN FETCH b.property where b.externalId = :externalId")
    Optional<BlockEntity> findByExternalId(String externalId);
}
