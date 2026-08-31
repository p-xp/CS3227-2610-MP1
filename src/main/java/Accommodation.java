/** A place to stay during a specified date range. */
public class Accommodation extends Plan {
    private final String fromDate;
    private final String toDate;

    /** Creates an accommodation using ISO dates validated by the command parser. */
    public Accommodation(String description, String fromDate, String toDate) {
        super(PlanType.ACCOMMODATION, description);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    /** Returns the first date of the accommodation stay. */
    public String getFromDate() {
        return fromDate;
    }

    /** Returns the final date of the accommodation stay. */
    public String getToDate() {
        return toDate;
    }

    /** Returns the accommodation in list-display format. */
    @Override
    public String toString() {
        return getDisplayPrefix() + getDescription()
                + " (from: " + fromDate + " to: " + toDate + ")";
    }
}
