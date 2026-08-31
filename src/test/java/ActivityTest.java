import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
