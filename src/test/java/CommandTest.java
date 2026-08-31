import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

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

    /** Returns storage that cannot be affected because these commands do not save. */
    private Storage storage() {
        return new Storage(temporaryDirectory.resolve("meepmoop.txt"));
    }
}
