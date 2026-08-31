import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests the console metadata associated with each plan type. */
class PlanTypeTest {
    @Test
    void getters_eachPlanType_returnsItsDisplayMetadata() {
        assertEquals("activity", PlanType.ACTIVITY.getDisplayName());
        assertEquals("A", PlanType.ACTIVITY.getMarker());
        assertEquals("accommodation", PlanType.ACCOMMODATION.getDisplayName());
        assertEquals("S", PlanType.ACCOMMODATION.getMarker());
        assertEquals("transport", PlanType.TRANSPORT.getDisplayName());
        assertEquals("T", PlanType.TRANSPORT.getMarker());
    }
}
