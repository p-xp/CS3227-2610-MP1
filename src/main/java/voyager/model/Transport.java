package voyager.model;

/** A transport arrangement between an origin and a destination. */
public class Transport extends Plan {
    private final String fromLocation;
    private final String toLocation;

    /** Creates a transport plan with its origin and destination. */
    public Transport(String description, String fromLocation, String toLocation) {
        super(PlanType.TRANSPORT, description);
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    /** Returns the transport's origin. */
    public String getFromLocation() {
        return fromLocation;
    }

    /** Returns the transport's destination. */
    public String getToLocation() {
        return toLocation;
    }

    /** Returns the transport arrangement in list-display format. */
    @Override
    public String toString() {
        return getDisplayPrefix() + getDescription()
                + " (from: " + fromLocation + " to: " + toLocation + ")";
    }
}
