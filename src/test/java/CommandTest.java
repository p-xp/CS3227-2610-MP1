import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests the initial command classes extracted from the command loop. */
class CommandTest {
    private static final String SEPARATOR = "____________________________________________________________";

    @TempDir
    private Path temporaryDirectory;

    @Test
    void listCommand_executeShowsPlansWithoutChangingItinerary() throws MeepException {
        Itinerary itinerary = new Itinerary();
        itinerary.add(new Activity("Museum"));
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)) {
            new ListCommand().execute(itinerary, new Ui(output), storage());
        }

        assertEquals(1, itinerary.getCount());
        assertEquals("Here are the items in your itinerary:" + System.lineSeparator()
                + "1. [A] [ ] Museum" + System.lineSeparator()
                + SEPARATOR + System.lineSeparator(), capturedBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void exitCommand_signalsExitWithoutChangingItinerary() throws MeepException {
        Itinerary itinerary = new Itinerary();
        itinerary.add(new Activity("Museum"));
        Command command = new ExitCommand();

        command.execute(itinerary, new Ui(), storage());

        assertTrue(command.isExit());
        assertEquals(1, itinerary.getCount());
    }

    @Test
    void listCommand_doesNotSignalExit() {
        assertFalse(new ListCommand().isExit());
    }

    @Test
    void viewCommand_executeShowsOnlyPlansOnRequestedDate() throws MeepException {
        Itinerary itinerary = new Itinerary();
        itinerary.add(new Activity("Museum", LocalDate.of(2026, 9, 1).atTime(10, 0)));
        itinerary.add(new Activity("Park", LocalDate.of(2026, 9, 2).atTime(10, 0)));
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)) {
            new ViewCommand(LocalDate.of(2026, 9, 1)).execute(itinerary, new Ui(output), storage());
        }

        assertEquals(2, itinerary.getCount());
        assertEquals("Here are the items in your itinerary on 1 Sep 2026:" + System.lineSeparator()
                + "[A] [ ] Museum (at: 1 Sep 2026 10am)" + System.lineSeparator()
                + SEPARATOR + System.lineSeparator(), capturedBytes.toString(StandardCharsets.UTF_8));
    }

    /** Returns storage that cannot be affected because these commands do not save. */
    private Storage storage() {
        return new Storage(temporaryDirectory.resolve("meepmoop.txt"));
    }
}
