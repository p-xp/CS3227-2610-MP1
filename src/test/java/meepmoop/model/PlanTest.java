package meepmoop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the shared type, description, booking state, and display behavior of plans. */
class PlanTest {
    @Test
    void constructor_newPlan_storesTypeAndDescriptionAndStartsUnbooked() {
        TestPlan plan = new TestPlan("Museum");

        assertEquals(PlanType.ACTIVITY, plan.getType());
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

        assertEquals("[A] [ ] ", plan.displayPrefix());

        plan.setBooked(true);
        assertEquals("[A] [X] ", plan.displayPrefix());
    }

    /** Minimal concrete plan used to expose protected behavior for testing. */
    private static final class TestPlan extends Plan {
        private TestPlan(String description) {
            super(PlanType.ACTIVITY, description);
        }

        private String displayPrefix() {
            return getDisplayPrefix();
        }

        @Override
        public String toString() {
            return getDescription();
        }
    }
}
