import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the shared description, booking state, and display behavior of plans. */
class PlanTest {
    @Test
    void constructor_newPlan_storesDescriptionAndStartsUnbooked() {
        TestPlan plan = new TestPlan("Museum");

        assertEquals("Museum", plan.getDescription());
        assertFalse(plan.isBooked());
    }

    @Test
    void setBooked_bookingThenUnbooking_updatesState() {
        TestPlan plan = new TestPlan("Museum");

        plan.setBooked(true);
        assertTrue(plan.isBooked());

        plan.setBooked(false);
        assertFalse(plan.isBooked());
    }

    @Test
    void getDisplayPrefix_differentBookingStates_returnsCorrectMarkers() {
        TestPlan plan = new TestPlan("Museum");

        assertEquals("[P] [ ] ", plan.displayPrefix("P"));

        plan.setBooked(true);
        assertEquals("[P] [X] ", plan.displayPrefix("P"));
    }

    /** Minimal concrete plan used to expose protected behavior for testing. */
    private static final class TestPlan extends Plan {
        private TestPlan(String description) {
            super(description);
        }

        private String displayPrefix(String typeMarker) {
            return getDisplayPrefix(typeMarker);
        }

        @Override
        public String toString() {
            return getDescription();
        }
    }
}
