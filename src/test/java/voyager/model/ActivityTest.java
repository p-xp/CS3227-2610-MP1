package voyager.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

    @Test
    void toString_datedActivityAtTimeBoundaries_usesTwelveHourClock() {
        Activity midnight = new Activity("Midnight", LocalDateTime.of(2026, 9, 1, 0, 0));
        Activity noon = new Activity("Lunch", LocalDateTime.of(2026, 9, 1, 12, 0));
        Activity afternoon = new Activity("Tea", LocalDateTime.of(2026, 9, 1, 13, 30));

        assertEquals("[A] [ ] Midnight (at: 1 Sep 2026 12am)", midnight.toString());
        assertEquals("[A] [ ] Lunch (at: 1 Sep 2026 12pm)", noon.toString());
        assertEquals("[A] [ ] Tea (at: 1 Sep 2026 1pm)", afternoon.toString());
    }

    @Test
    void occursOn_unscheduledActivity_returnsFalseForEveryDate() {
        Activity activity = new Activity("Flexible visit");

        assertFalse(activity.occursOn(LocalDate.of(2026, 9, 1)));
    }
}
