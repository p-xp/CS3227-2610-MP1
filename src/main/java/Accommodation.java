/** A place to stay during a specified date range. */
public class Accommodation extends Plan {
    private final String fromDate;
    private final String toDate;

    /** Creates an accommodation using ISO dates validated by the command parser. */
    public Accommodation(String description, String fromDate, String toDate) {
        super(description);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    /** Returns the accommodation in list-display format. */
    @Override
    public String toString() {
        return getDisplayPrefix("S") + getDescription()
                + " (from: " + fromDate + " to: " + toDate + ")";
    }
}
