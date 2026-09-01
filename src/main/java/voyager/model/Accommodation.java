package voyager.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** A place to stay during a specified date range. */
public class Accommodation extends Plan {
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private final LocalDate fromDate;
    private final LocalDate toDate;

    /** Creates an accommodation using its inclusive start and end dates. */
    public Accommodation(String description, LocalDate fromDate, LocalDate toDate) {
        super(PlanType.ACCOMMODATION, description);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    /** Returns the first date of the accommodation stay. */
    public LocalDate getFromDate() {
        return fromDate;
    }

    /** Returns the final date of the accommodation stay. */
    public LocalDate getToDate() {
        return toDate;
    }

    /** Returns the accommodation in list-display format. */
    @Override
    public String toString() {
        return getDisplayPrefix() + getDescription()
                + " (from: " + fromDate.format(DISPLAY_DATE)
                + " to: " + toDate.format(DISPLAY_DATE) + ")";
    }

    /** Returns whether the supplied date falls within this stay's inclusive range. */
    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(fromDate) && !date.isAfter(toDate);
    }
}
