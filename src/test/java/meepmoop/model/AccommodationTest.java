package meepmoop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** Tests the console display representation of an accommodation. */
class AccommodationTest {
    @Test
    void toString_unbookedAccommodation_returnsDatesAndUnbookedMarker() {
        Accommodation accommodation = new Accommodation(
                "Hotel", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3));

        assertEquals(PlanType.ACCOMMODATION, accommodation.getType());
        assertEquals(LocalDate.of(2026, 9, 1), accommodation.getFromDate());
        assertEquals(LocalDate.of(2026, 9, 3), accommodation.getToDate());
        assertEquals("[S] [ ] Hotel (from: 1 Sep 2026 to: 3 Sep 2026)",
                accommodation.toString());
    }

    @Test
    void toString_bookedAccommodation_returnsDatesAndBookedMarker() {
        Accommodation accommodation = new Accommodation(
                "Hotel", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3));
        accommodation.setBooked(true);

        assertEquals("[S] [X] Hotel (from: 1 Sep 2026 to: 3 Sep 2026)",
                accommodation.toString());
    }

    @Test
    void occursOn_includesBothStayBoundariesOnly() {
        Accommodation accommodation = new Accommodation(
                "Hotel", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3));

        assertFalse(accommodation.occursOn(LocalDate.of(2026, 8, 31)));
        assertTrue(accommodation.occursOn(LocalDate.of(2026, 9, 1)));
        assertTrue(accommodation.occursOn(LocalDate.of(2026, 9, 3)));
        assertFalse(accommodation.occursOn(LocalDate.of(2026, 9, 4)));
    }
}
