/** A transport arrangement between an origin and a destination. */
public class Transport extends Plan {
    private final String fromLocation;
    private final String toLocation;

    /** Creates a transport plan with its origin and destination. */
    public Transport(String description, String fromLocation, String toLocation) {
        super(description);
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    /** Returns the transport arrangement in list-display format. */
    @Override
    public String toString() {
        return getDisplayPrefix("T") + getDescription()
                + " (from: " + fromLocation + " to: " + toLocation + ")";
    }
}
