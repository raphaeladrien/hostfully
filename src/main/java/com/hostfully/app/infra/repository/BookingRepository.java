package com.hostfully.app.infra.repository;

import com.hostfully.app.infra.entity.BookingEntity;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BookingEntity b"
            + "   WHERE b.property.externalId = :propertyId"
            + "   AND b.status = 'CONFIRMED'"
            + "   AND (:bookingId IS NULL OR b.externalId <> :bookingId)"
            + "   AND b.startDate <= :endDate"
            + "   AND b.endDate >= :startDate")
    boolean hasOverlapping(String propertyId, LocalDate startDate, LocalDate endDate, String bookingId);
}
