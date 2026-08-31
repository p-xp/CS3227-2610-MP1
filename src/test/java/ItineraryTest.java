import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests itinerary storage, one-based indexing, removal, and capacity limits. */
class ItineraryTest {
    private static final int MAX_PLANS = 100;

    @Test
    void getCount_newItinerary_returnsZero() {
        Itinerary itinerary = new Itinerary();

        assertEquals(0, itinerary.getCount());
    }

    @Test
    void add_validPlan_returnsTrueAndMakesPlanRetrievable() {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");

        assertTrue(itinerary.add(activity));
        assertEquals(1, itinerary.getCount());
        assertSame(activity, itinerary.get(1));
    }

    @Test
    void add_nullPlan_throwsExceptionAndPreservesEmptyItinerary() {
        Itinerary itinerary = new Itinerary();

        NullPointerException exception = assertThrows(
                NullPointerException.class, () -> itinerary.add(null));

        assertEquals("plan must not be null", exception.getMessage());
        assertEquals(0, itinerary.getCount());
    }

    @Test
    void add_ninetyNineExistingPlans_acceptsHundredthPlan() {
        Itinerary itinerary = itineraryWithPlans(MAX_PLANS - 1);
        Activity lastPlan = new Activity("Plan 100");

        assertTrue(itinerary.add(lastPlan));
        assertEquals(MAX_PLANS, itinerary.getCount());
        assertSame(lastPlan, itinerary.get(MAX_PLANS));
    }

    @Test
    void add_oneHundredExistingPlans_rejectsHundredAndFirstPlan() {
        Itinerary itinerary = itineraryWithPlans(MAX_PLANS);
        Activity rejectedPlan = new Activity("Plan 101");

        assertFalse(itinerary.add(rejectedPlan));
        assertEquals(MAX_PLANS, itinerary.getCount());
        assertNull(itinerary.get(MAX_PLANS + 1));
    }

    @Test
    void get_firstAndLastPlanNumbers_returnsMatchingPlans() {
        Itinerary itinerary = new Itinerary();
        Activity firstPlan = new Activity("Museum");
        Activity lastPlan = new Activity("Park");
        itinerary.add(firstPlan);
        itinerary.add(lastPlan);

        assertSame(firstPlan, itinerary.get(1));
        assertSame(lastPlan, itinerary.get(2));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 2, Integer.MAX_VALUE})
    void get_invalidPlanNumber_returnsNull(int planNumber) {
        Itinerary itinerary = new Itinerary();
        itinerary.add(new Activity("Museum"));

        assertNull(itinerary.get(planNumber));
    }

    @Test
    void remove_firstPlan_returnsPlanAndRenumbersRemainingPlans() {
        Itinerary itinerary = new Itinerary();
        Activity firstPlan = new Activity("Museum");
        Activity secondPlan = new Activity("Park");
        itinerary.add(firstPlan);
        itinerary.add(secondPlan);

        assertSame(firstPlan, itinerary.remove(1));
        assertEquals(1, itinerary.getCount());
        assertSame(secondPlan, itinerary.get(1));
    }

    @Test
    void remove_lastPlan_returnsPlanAndReducesCount() {
        Itinerary itinerary = new Itinerary();
        Activity firstPlan = new Activity("Museum");
        Activity lastPlan = new Activity("Park");
        itinerary.add(firstPlan);
        itinerary.add(lastPlan);

        assertSame(lastPlan, itinerary.remove(2));
        assertEquals(1, itinerary.getCount());
        assertSame(firstPlan, itinerary.get(1));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 2, Integer.MAX_VALUE})
    void remove_invalidPlanNumber_returnsNullAndPreservesState(int planNumber) {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        itinerary.add(activity);

        assertNull(itinerary.remove(planNumber));
        assertEquals(1, itinerary.getCount());
        assertSame(activity, itinerary.get(1));
    }

    /** Creates an itinerary containing the requested number of valid plans. */
    private static Itinerary itineraryWithPlans(int count) {
        Itinerary itinerary = new Itinerary();
        for (int planNumber = 1; planNumber <= count; planNumber++) {
            boolean wasAdded = itinerary.add(new Activity("Plan " + planNumber));
            assertTrue(wasAdded, "setup plan should fit within itinerary capacity");
        }
        return itinerary;
    }
}
