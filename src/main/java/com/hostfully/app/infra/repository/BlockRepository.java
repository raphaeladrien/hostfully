package com.hostfully.app.infra.repository;

import com.hostfully.app.infra.entity.BlockEntity;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<BlockEntity, Long> {

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BlockEntity b"
            + "        WHERE b.property.externalId = :propertyId"
            + "          AND b.startDate < :endDate"
            + "          AND b.endDate > :startDate")
    Boolean hasOverlapping(String propertyId, LocalDate startDate, LocalDate endDate);
}
