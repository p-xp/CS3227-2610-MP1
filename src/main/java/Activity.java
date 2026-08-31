/** A visit, tour, or other activity in an itinerary. */
public class Activity extends Plan {
    /** Creates an activity with the supplied description. */
    public Activity(String description) {
        super(PlanType.ACTIVITY, description);
    }

    /** Returns the activity in list-display format. */
    @Override
    public String toString() {
        return getDisplayPrefix() + getDescription();
    }
}
