import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests the console display representation of an accommodation. */
class AccommodationTest {
    @Test
    void toString_unbookedAccommodation_returnsDatesAndUnbookedMarker() {
        Accommodation accommodation = new Accommodation(
                "Hotel", "2026-09-01", "2026-09-03");

        assertEquals(PlanType.ACCOMMODATION, accommodation.getType());
        assertEquals("2026-09-01", accommodation.getFromDate());
        assertEquals("2026-09-03", accommodation.getToDate());
        assertEquals("[S] [ ] Hotel (from: 2026-09-01 to: 2026-09-03)",
                accommodation.toString());
    }

    @Test
    void toString_bookedAccommodation_returnsDatesAndBookedMarker() {
        Accommodation accommodation = new Accommodation(
                "Hotel", "2026-09-01", "2026-09-03");
        accommodation.setBooked(true);

        assertEquals("[S] [X] Hotel (from: 2026-09-01 to: 2026-09-03)",
                accommodation.toString());
    }
}
