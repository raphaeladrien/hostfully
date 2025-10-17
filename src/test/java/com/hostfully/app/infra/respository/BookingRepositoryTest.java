package com.hostfully.app.infra.respository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BookingRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private PropertyEntity property1;
    private PropertyEntity property2;

    private static final String propertyId1 = "PROP-001";
    private static final String propertyId2 = "PROP-002";

    @BeforeEach
    void setUp() {
        property1 = entityManager.persist(new PropertyEntity(propertyId1, "Beach House", "Beach baby!"));
        property2 = entityManager.persist(new PropertyEntity(propertyId2, "Mountain Cabin", "Relax time"));

        entityManager.flush();
    }

    @ParameterizedTest
    @MethodSource("provideOverlapRanges")
    @DisplayName("should detect an overlapping confirmed booking for the same property")
    void shouldReturnTrueWhenOverlappingBookingExists(LocalDate startDate, LocalDate endDate, String propertyId) {
        createAndSaveBooking(
                "BOOK-1", property1, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));
        createAndSaveBooking(
                "BOOK-2", property2, BookingStatus.CANCELLED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));
        createAndSaveBooking(
                "BOOK-3", property2, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));

        boolean exists = bookingRepository.hasOverlapping(propertyId, startDate, endDate, null);

        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideOverlapRanges")
    @DisplayName("should not detect overlap when the booking being updated is the same")
    void shouldReturnFalseWhenOverlappingBookingIsProvidedForSameBooking(
            LocalDate startDate, LocalDate endDate, String propertyId) {
        createAndSaveBooking(
                "BOOK-1", property1, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));
        createAndSaveBooking(
                "BOOK-2", property2, BookingStatus.CANCELLED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));

        boolean exists = bookingRepository.hasOverlapping(propertyId, startDate, endDate, "BOOK-1");

        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideRanges")
    @DisplayName("should confirm availability when no overlapping confirmed booking exists")
    void shouldReturnFalseWhenNoOverlappingBookingExists(LocalDate startDate, LocalDate endDate, String propertyId) {
        createAndSaveBooking(
                "BOOK-1", property1, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));
        createAndSaveBooking(
                "BOOK-2", property2, BookingStatus.CANCELLED, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 6));
        createAndSaveBooking(
                "BOOK-3", property2, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));

        boolean exists = bookingRepository.hasOverlapping(propertyId, startDate, endDate, null);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("should delete booking by external id provided")
    void deleteBlockByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBooking(
                id, property1, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));

        Assertions.assertThat(bookingRepository.deleteByExternalId(id)).isEqualTo(1);
    }

    @Test
    @DisplayName("returns 0 when no record to delete")
    void returnsZeroWhenNoRecordToDelete() {
        Assertions.assertThat(bookingRepository.deleteByExternalId("my-amazing"))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("when record is found by external id, returns Optional")
    void whenRecordIsFoundByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBooking(
                id, property1, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));

        final Optional<BookingEntity> optionalBooking = bookingRepository.findByExternalId(id);

        SoftAssertions.assertSoftly(softAssertions -> {
            Assertions.assertThat(optionalBooking.isPresent()).isTrue();
            final BookingEntity result = optionalBooking.get();
            Assertions.assertThat(result.getProperty().getAlias()).isEqualTo(property1.getAlias());
            Assertions.assertThat(result.getProperty().getExternalId()).isEqualTo(property1.getExternalId());
            Assertions.assertThat(result.getProperty().getDescription()).isEqualTo(property1.getDescription());
            Assertions.assertThat(result.getExternalId()).isEqualTo(id);
        });
    }

    @Test
    @DisplayName("when record isn't found by external id, returns Optional empty")
    void whenRecordIsNotFoundByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBooking(
                id, property1, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));

        final Optional<BookingEntity> optionalBlock = bookingRepository.findByExternalId("wow-id");

        Assertions.assertThat(optionalBlock.isPresent()).isFalse();
    }

    @Test
    @DisplayName("update booking status by external id")
    void updateStatusByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBooking(
                id, property1, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5));
        createAndSaveBooking(
                "asert-1", property1, BookingStatus.CONFIRMED, LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 20));

        final int result = bookingRepository.updateStatus(BookingStatus.CANCELLED, id);
        entityManager.flush();
        entityManager.clear();

        Optional<BookingEntity> entity = bookingRepository.findByExternalId(id);

        Assertions.assertThat(result).isEqualTo(1);
        Assertions.assertThat(entity.isPresent()).isTrue();
        Assertions.assertThat(entity.get().getStatus().isCancelled()).isTrue();
    }

    private static Stream<Arguments> provideOverlapRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 3), propertyId1),
                arguments(LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 6), propertyId1),
                arguments(LocalDate.of(2025, 1, 4), LocalDate.of(2025, 1, 7), propertyId1),
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 6), propertyId1),
                arguments(LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5), propertyId1),
                arguments(LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 5), propertyId2));
    }

    private static Stream<Arguments> provideRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10), propertyId1),
                arguments(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 8), propertyId1),
                arguments(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 7), propertyId2));
    }

    private Long createAndSaveBooking(
            final String id,
            final PropertyEntity property,
            final BookingStatus status,
            final LocalDate startDate,
            final LocalDate endDate) {
        final BookingEntity booking =
                new BookingEntity(id, property, "Daenerys Targaryen", 4, status, startDate, endDate);
        entityManager.persist(booking);
        entityManager.flush();
        return booking.getId();
    }
}
