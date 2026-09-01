package meepmoop.ui;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import meepmoop.model.Itinerary;
import meepmoop.model.Plan;

/** Handles all text displayed to the user by the MeepMoop command-line interface. */
public final class Ui {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String LIST_REFRESHED_MESSAGE = "List has been manually refreshed.";
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private final PrintStream output;
    private boolean isLastResponseError;

    /** Creates a user interface that writes to the standard output stream. */
    public Ui() {
        this(System.out);
    }

    /** Creates a user interface that writes to the supplied output stream. */
    public Ui(PrintStream output) {
        this.output = Objects.requireNonNull(output, "output must not be null");
    }

    /** Shows the standard welcome message. */
    public void showWelcome() {
        output.println("Hello! I'm MeepMoop. How can I assist you today?");
        showSeparator();
    }

    /** Shows the standard farewell message. */
    public void showGoodbye() {
        output.println("Goodbye! Have a great day!");
        showSeparator();
    }

    /** Shows an error explaining that saved data could not be loaded. */
    public void showLoadingError() {
        output.println("Unable to load saved data.");
        showSeparator();
    }

    /** Warns that one or more corrupted saved records were ignored. */
    public void showCorruptedDataWarning() {
        output.println("Warning: Some saved data could not be loaded.");
        showSeparator();
    }

    /** Shows a command validation error. */
    public void showError(String message) {
        isLastResponseError = true;
        output.println(message);
        showSeparator();
    }

    /** Shows an error explaining that a state change could not be saved. */
    public void showSaveError() {
        isLastResponseError = true;
        output.println("Unable to save data.");
        showSeparator();
    }

    /** Shows an error explaining that the itinerary has reached its capacity. */
    public void showItineraryFull() {
        isLastResponseError = true;
        output.println("Itinerary is full");
        showSeparator();
    }

    /** Shows confirmation that a plan was added. */
    public void showPlanAdded(Plan plan, int planCount) {
        output.println("Got it. I've added this " + plan.getType().getDisplayName() + ":");
        output.println(plan);
        showPlanCount(planCount);
        showSeparator();
    }

    /** Shows confirmation that a plan's booking state was changed. */
    public void showBookingUpdated(Plan plan, boolean isBooked) {
        output.println((isBooked ? "Booked: " : "Unbooked: ") + plan);
        showSeparator();
    }

    /** Shows confirmation that a plan was removed. */
    public void showPlanDeleted(Plan plan, int planCount) {
        output.println("Noted. I've removed this item:");
        output.println(plan);
        showPlanCount(planCount);
        showSeparator();
    }

    /** Shows confirmation that the itinerary list was refreshed in the graphical interface. */
    public void showList() {
        output.println(LIST_REFRESHED_MESSAGE);
        showSeparator();
    }

    /** Clears the error status before a new command is processed. */
    public void clearResponseStatus() {
        isLastResponseError = false;
    }

    /** Returns whether the most recently displayed command response was an error. */
    public boolean isLastResponseError() {
        return isLastResponseError;
    }

    /** Shows all itinerary items that occur on the requested date. */
    public void showPlansOn(LocalDate date, Itinerary itinerary) {
        output.println("Here are the items in your itinerary on "
                + date.format(DISPLAY_DATE) + ":");
        for (Plan plan : itinerary.getPlansOn(date)) {
            output.println(plan);
        }
        showSeparator();
    }

    /** Shows plans whose descriptions match every supplied find keyword. */
    public void showPlansMatchingKeywords(String keywords, Itinerary itinerary) {
        List<Integer> matchingPlanNumbers = itinerary.getPlanNumbersMatchingKeywords(
                List.of(keywords.split("\\s+")));
        if (matchingPlanNumbers.isEmpty()) {
            output.println("No match found for keyword \"" + keywords + "\".");
        } else {
            output.println("Here are the matching items in your itinerary:");
            for (int planNumber : matchingPlanNumbers) {
                output.println(planNumber + ". " + itinerary.get(planNumber));
            }
        }
        showSeparator();
    }

    /** Shows the standard item-count sentence. */
    private void showPlanCount(int planCount) {
        output.println("Now you have " + planCount + " items in your itinerary.");
    }

    /** Shows the shared horizontal separator. */
    private void showSeparator() {
        output.println(SEPARATOR);
    }
}
