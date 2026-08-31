package meepmoop.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import meepmoop.model.Activity;
import meepmoop.model.Itinerary;

/** Tests the exact console messages rendered by the user interface. */
class UiTest {
    private static final String SEPARATOR = "____________________________________________________________";

    @Test
    void showMessages_rendersStandardMessagesAndPlanDetails() {
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)) {
            Ui ui = new Ui(output);
            Itinerary itinerary = new Itinerary();
            Activity activity = new Activity("Museum", LocalDate.of(2026, 9, 1).atTime(9, 30));
            itinerary.add(activity);

            ui.showWelcome();
            ui.showCorruptedDataWarning();
            ui.showPlanAdded(activity, itinerary.getCount());
            activity.setBooked(true);
            ui.showBookingUpdated(activity, true);
            ui.showList(itinerary);
            ui.showPlansOn(LocalDate.of(2026, 9, 1), itinerary);
            ui.showPlanDeleted(activity, 0);
            ui.showItineraryFull();
            ui.showLoadingError();
            ui.showSaveError();
            ui.showError("Invalid input");
            ui.showGoodbye();
        }

        String newline = System.lineSeparator();
        String expected = "Hello! I'm MeepMoop. How can I assist you today?" + newline
                + SEPARATOR + newline
                + "Warning: Some saved data could not be loaded." + newline
                + SEPARATOR + newline
                + "Got it. I've added this activity:" + newline
                + "[A] [ ] Museum (at: 1 Sep 2026 9am)" + newline
                + "Now you have 1 items in your itinerary." + newline
                + SEPARATOR + newline
                + "Booked: [A] [X] Museum (at: 1 Sep 2026 9am)" + newline
                + SEPARATOR + newline
                + "Here are the items in your itinerary:" + newline
                + "1. [A] [X] Museum (at: 1 Sep 2026 9am)" + newline
                + SEPARATOR + newline
                + "Here are the items in your itinerary on 1 Sep 2026:" + newline
                + "[A] [X] Museum (at: 1 Sep 2026 9am)" + newline
                + SEPARATOR + newline
                + "Noted. I've removed this item:" + newline
                + "[A] [X] Museum (at: 1 Sep 2026 9am)" + newline
                + "Now you have 0 items in your itinerary." + newline
                + SEPARATOR + newline
                + "Itinerary is full" + newline
                + SEPARATOR + newline
                + "Unable to load saved data." + newline
                + SEPARATOR + newline
                + "Unable to save data." + newline
                + SEPARATOR + newline
                + "Invalid input" + newline
                + SEPARATOR + newline
                + "Goodbye! Have a great day!" + newline
                + SEPARATOR + newline;

        assertEquals(expected, capturedBytes.toString(StandardCharsets.UTF_8));
    }
}
