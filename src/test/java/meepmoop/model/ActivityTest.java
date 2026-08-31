package meepmoop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Tests the console display representation of an activity. */
class ActivityTest {
    @Test
    void toString_unbookedActivity_returnsUnbookedDisplayFormat() {
        Activity activity = new Activity("Museum visit");

        assertEquals(PlanType.ACTIVITY, activity.getType());
        assertEquals("[A] [ ] Museum visit", activity.toString());
    }

    @Test
    void toString_bookedActivity_returnsBookedDisplayFormat() {
        Activity activity = new Activity("Museum visit");
        activity.setBooked(true);

        assertEquals("[A] [X] Museum visit", activity.toString());
    }

    @Test
    void toString_datedActivity_returnsHumanReadableDateAndTime() {
        Activity activity = new Activity("Museum visit", LocalDateTime.of(2026, 9, 1, 18, 0));

        assertEquals(LocalDateTime.of(2026, 9, 1, 18, 0), activity.getScheduledAt());
        assertEquals("[A] [ ] Museum visit (at: 1 Sep 2026 6pm)", activity.toString());
        assertEquals(true, activity.occursOn(LocalDate.of(2026, 9, 1)));
    }
}
