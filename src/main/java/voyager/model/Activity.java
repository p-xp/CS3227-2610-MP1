package voyager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** A visit, tour, or other activity in an itinerary. */
public class Activity extends Plan {
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private final LocalDateTime scheduledAt;

    /** Creates an activity with the supplied description. */
    public Activity(String description) {
        this(description, null);
    }

    /** Creates an activity, optionally scheduled at a specific local date and time. */
    public Activity(String description, LocalDateTime scheduledAt) {
        super(PlanType.ACTIVITY, description);
        this.scheduledAt = scheduledAt;
    }

    /** Returns the scheduled local date and time, or null when the activity is unscheduled. */
    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    /** Returns whether this dated activity occurs on the supplied date. */
    @Override
    public boolean occursOn(LocalDate date) {
        return scheduledAt != null && scheduledAt.toLocalDate().equals(date);
    }

    /** Returns the activity in list-display format. */
    @Override
    public String toString() {
        if (scheduledAt == null) {
            return getDisplayPrefix() + getDescription();
        }
        return getDisplayPrefix() + getDescription() + " (at: "
                + scheduledAt.toLocalDate().format(DISPLAY_DATE) + " "
                + formatTime(scheduledAt) + ")";
    }

    /** Formats a time as a compact lower-case 12-hour value for the console. */
    private static String formatTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour() % 12;
        return (hour == 0 ? 12 : hour) + (dateTime.getHour() < 12 ? "am" : "pm");
    }
}
