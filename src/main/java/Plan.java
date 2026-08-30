/** Represents a bookable item in a travel itinerary. */
public abstract class Plan {
    private final String description;
    private boolean booked;

    /** Creates an unbooked plan with the supplied description. */
    protected Plan(String description) {
        this.description = description;
    }

    /** Returns the description supplied when this plan was created. */
    public String getDescription() {
        return description;
    }

    /** Returns whether this plan has been booked. */
    public boolean isBooked() {
        return booked;
    }

    /** Updates whether this plan has been booked. */
    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    /** Builds common type and booking markers for plan displays. */
    protected String getDisplayPrefix(String typeMarker) {
        return "[" + typeMarker + "] [" + (booked ? "X" : " ") + "] ";
    }

    /** Returns this plan in the format shown in the itinerary list. */
    @Override
    public abstract String toString();
}
