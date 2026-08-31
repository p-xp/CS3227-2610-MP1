import java.util.ArrayList;
import java.util.List;

/** Stores an in-memory collection of itinerary plans. */
public class Itinerary {
    private static final int MAX_PLANS = 100;
    private final List<Plan> plans = new ArrayList<>();

    /** Adds a plan if this itinerary has remaining capacity. */
    public boolean add(Plan plan) {
        if (plans.size() == MAX_PLANS) {
            return false;
        }
        plans.add(plan);
        return true;
    }

    /** Returns the number of plans currently stored. */
    public int getCount() {
        return plans.size();
    }

    /** Retrieves a plan by the one-based number shown to the user. */
    public Plan get(int planNumber) {
        if (planNumber < 1 || planNumber > plans.size()) {
            return null;
        }
        return plans.get(planNumber - 1);
    }

    /** Removes and returns a plan by the one-based number shown to the user. */
    public Plan remove(int planNumber) {
        if (planNumber < 1 || planNumber > plans.size()) {
            return null;
        }
        return plans.remove(planNumber - 1);
    }
}
