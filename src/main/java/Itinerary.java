/** Stores a fixed-size, in-memory collection of itinerary plans. */
public class Itinerary {
    private static final int MAX_PLANS = 100;
    private final Plan[] plans = new Plan[MAX_PLANS];
    private int count;

    /** Adds a plan if this itinerary has remaining capacity. */
    public boolean add(Plan plan) {
        if (count == MAX_PLANS) {
            return false;
        }
        plans[count++] = plan;
        return true;
    }

    /** Returns the number of plans currently stored. */
    public int getCount() {
        return count;
    }

    /** Retrieves a plan by the one-based number shown to the user. */
    public Plan get(int planNumber) {
        if (planNumber < 1 || planNumber > count) {
            return null;
        }
        return plans[planNumber - 1];
    }
}
