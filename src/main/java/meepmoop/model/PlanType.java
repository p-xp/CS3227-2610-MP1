package meepmoop.model;

/** Identifies a kind of itinerary plan and its console display metadata. */
public enum PlanType {
    ACTIVITY("activity", "A"),
    ACCOMMODATION("accommodation", "S"),
    TRANSPORT("transport", "T");

    private final String displayName;
    private final String marker;

    /** Creates a plan type with the wording and marker used by the console UI. */
    PlanType(String displayName, String marker) {
        this.displayName = displayName;
        this.marker = marker;
    }

    /** Returns the lowercase plan name used in confirmation messages. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the single-letter marker used when displaying a plan. */
    public String getMarker() {
        return marker;
    }
}
