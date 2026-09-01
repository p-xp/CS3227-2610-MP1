package voyager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import voyager.model.Activity;
import voyager.model.Itinerary;
import voyager.model.PlanType;
import voyager.parser.Parser;
import voyager.storage.Storage;
import voyager.ui.Ui;

/** Tests command-level rollback when persistence fails. */
class VoyagerTest {
    private static final String SAVE_ERROR = "Unable to save data." + System.lineSeparator()
            + "____________________________________________________________" + System.lineSeparator();

    @TempDir
    private Path temporaryDirectory;

    @Test
    void handleCommand_addCannotBeSaved_rollsBackAddition() throws IOException {
        Itinerary itinerary = new Itinerary();

        String output = runCommand("activity Museum", itinerary, failingStorage());

        assertEquals(0, itinerary.getCount());
        assertEquals(SAVE_ERROR, output);
    }

    @Test
    void handleCommand_stayCannotBeSaved_rollsBackAddition() throws IOException {
        Itinerary itinerary = new Itinerary();

        String output = runCommand("stay Hotel /from 2026-09-01 /to 2026-09-03",
                itinerary, failingStorage());

        assertEquals(0, itinerary.getCount());
        assertEquals(SAVE_ERROR, output);
    }

    @Test
    void handleCommand_transportCannotBeSaved_rollsBackAddition() throws IOException {
        Itinerary itinerary = new Itinerary();

        String output = runCommand("transport Train /from Singapore /to Kuala Lumpur",
                itinerary, failingStorage());

        assertEquals(0, itinerary.getCount());
        assertEquals(SAVE_ERROR, output);
    }

    @Test
    void handleCommand_bookingCannotBeSaved_restoresUnbookedState() throws IOException {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        itinerary.add(activity);

        String output = runCommand("book 1", itinerary, failingStorage());

        assertFalse(activity.isBooked());
        assertEquals(SAVE_ERROR, output);
    }

    @Test
    void handleCommand_deletionCannotBeSaved_restoresPlanAtOriginalPosition() throws IOException {
        Itinerary itinerary = new Itinerary();
        Activity first = new Activity("First");
        Activity second = new Activity("Second");
        itinerary.add(first);
        itinerary.add(second);

        String output = runCommand("delete 1", itinerary, failingStorage());

        assertEquals(2, itinerary.getCount());
        assertSame(first, itinerary.get(1));
        assertSame(second, itinerary.get(2));
        assertEquals(SAVE_ERROR, output);
    }

    @Test
    void handleCommand_invalidThenValidCommand_updatesResponseValidity() {
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        Voyager voyager = new Voyager(new Itinerary(), new Parser(),
                new Storage(temporaryDirectory.resolve("data.txt")),
                new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)));

        voyager.handleCommand("unknown-command");
        assertFalse(voyager.wasLastCommandValid());

        voyager.handleCommand("list");
        assertTrue(voyager.wasLastCommandValid());
    }

    @Test
    void handleCommand_endToEndSequencePersistsRequestedChanges() throws IOException {
        Path dataFile = temporaryDirectory.resolve("data.txt");
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        Voyager voyager = new Voyager(dataFile,
                new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)));

        assertTrue(voyager.handleCommand("activity Museum /at 2026-09-01 0900"));
        assertTrue(voyager.handleCommand("book 1"));
        assertTrue(voyager.handleCommand("find museum"));
        assertTrue(voyager.handleCommand("view 2026-09-01"));
        assertTrue(voyager.handleCommand("delete 1"));
        assertFalse(voyager.handleCommand("exit"));

        assertEquals(0, voyager.getItinerary().getCount());
        assertEquals(0, new Storage(dataFile).load().getItinerary().getCount());
        assertTrue(capturedBytes.toString(StandardCharsets.UTF_8).contains("Booked: [A] [X] Museum"));
        assertTrue(capturedBytes.toString(StandardCharsets.UTF_8)
                .contains("Here are the matching items in your itinerary:"));
    }

    @Test
    void run_corruptedSavedDataShowsWarningAndProcessesCommands() throws IOException {
        Path dataFile = temporaryDirectory.resolve("data.txt");
        Files.writeString(dataFile, "bad record" + System.lineSeparator(), StandardCharsets.UTF_8);
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        Ui ui = new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8));
        Voyager voyager = new Voyager(dataFile, ui);
        java.io.InputStream originalInput = System.in;
        try {
            System.setIn(new ByteArrayInputStream("list\nexit\n".getBytes(StandardCharsets.UTF_8)));
            voyager.run();
        } finally {
            System.setIn(originalInput);
        }

        String output = capturedBytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Warning: Some saved data could not be loaded."));
        assertTrue(output.contains("List has been manually refreshed."));
        assertTrue(output.contains("Goodbye! Have a great day!"));
    }

    @Test
    void run_loadingFailureDoesNotStartCommandLoop() throws IOException {
        Path dataFile = Files.createDirectory(temporaryDirectory.resolve("data-directory"));
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        Voyager voyager = new Voyager(dataFile,
                new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)));

        voyager.run();

        assertEquals("Unable to load saved data." + System.lineSeparator()
                + "____________________________________________________________" + System.lineSeparator(),
                capturedBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void handleCommand_remainingValidCommandsPersistTheirExpectedPlans() throws IOException {
        Path dataFile = temporaryDirectory.resolve("data.txt");
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        Voyager voyager = new Voyager(dataFile,
                new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)));

        assertTrue(voyager.handleCommand("stay Hotel /from 2026-09-01 /to 2026-09-03"));
        assertTrue(voyager.handleCommand("transport Flight /from Singapore /to Tokyo"));
        assertTrue(voyager.handleCommand("book 2"));
        assertTrue(voyager.handleCommand("unbook 2"));
        assertTrue(voyager.handleCommand("list"));

        Itinerary loadedItinerary = new Storage(dataFile).load().getItinerary();
        assertEquals(2, loadedItinerary.getCount());
        assertEquals(PlanType.ACCOMMODATION, loadedItinerary.get(1).getType());
        assertEquals(PlanType.TRANSPORT, loadedItinerary.get(2).getType());
        assertFalse(loadedItinerary.get(2).isBooked());
        assertTrue(capturedBytes.toString(StandardCharsets.UTF_8).contains("Unbooked: [T] [ ] Flight"));
    }

    /** Creates storage whose parent path is a file, forcing every save to fail. */
    private Storage failingStorage() throws IOException {
        Path blocker = Files.createFile(temporaryDirectory.resolve("blocker-" + System.nanoTime()));
        return new Storage(blocker.resolve("data.txt"));
    }

    /** Runs one command while capturing its exact console output. */
    private static String runCommand(String input, Itinerary itinerary, Storage storage) {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        try (PrintStream capturedOutput = new PrintStream(
                capturedBytes, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOutput);
            new Voyager(itinerary, new Parser(), storage, new Ui(capturedOutput)).handleCommand(input);
        } finally {
            System.setOut(originalOutput);
        }
        return capturedBytes.toString(StandardCharsets.UTF_8);
    }
}
