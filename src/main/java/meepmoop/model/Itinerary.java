package meepmoop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Stores an in-memory collection of itinerary plans. */
public class Itinerary {
    private static final int MAX_PLANS = 100;
    private final List<Plan> plans = new ArrayList<>();

    /**
     * Adds a non-null plan if this itinerary has remaining capacity.
     *
     * @throws NullPointerException if {@code plan} is null
     */
    public boolean add(Plan plan) {
        Objects.requireNonNull(plan, "plan must not be null");
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

    /** Returns all plans whose date information includes the supplied date, in list order. */
    public List<Plan> getPlansOn(LocalDate date) {
        List<Plan> matchingPlans = new ArrayList<>();
        for (Plan plan : plans) {
            if (plan.occursOn(date)) {
                matchingPlans.add(plan);
            }
        }
        return matchingPlans;
    }

    /** Returns one-based numbers of plans whose descriptions contain every supplied keyword. */
    public List<Integer> getPlanNumbersMatchingKeywords(List<String> keywords) {
        List<Integer> matchingPlanNumbers = new ArrayList<>();
        for (int index = 0; index < plans.size(); index++) {
            String description = plans.get(index).getDescription().toLowerCase(Locale.ROOT);
            if (keywords.stream().allMatch(keyword -> description.contains(keyword.toLowerCase(Locale.ROOT)))) {
                matchingPlanNumbers.add(index + 1);
            }
        }
        return matchingPlanNumbers;
    }

    /**
     * Restores a plan at a valid one-based position after a failed persistent update.
     * Normal user operations should use {@link #add(Plan)} or {@link #remove(int)}.
     *
     * @throws NullPointerException if {@code plan} is null
     * @throws IndexOutOfBoundsException if the position is not valid for restoration
     */
    public void restore(int planNumber, Plan plan) {
        Objects.requireNonNull(plan, "plan must not be null");
        if (planNumber < 1 || planNumber > plans.size() + 1) {
            throw new IndexOutOfBoundsException("invalid restore position");
        }
        plans.add(planNumber - 1, plan);
    }
}
