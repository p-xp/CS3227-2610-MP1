package meepmoop;

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

import meepmoop.model.Activity;
import meepmoop.model.Itinerary;
import meepmoop.model.PlanType;
import meepmoop.parser.Parser;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

/** Tests command-level rollback when persistence fails. */
class MeepMoopTest {
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
        MeepMoop meepMoop = new MeepMoop(new Itinerary(), new Parser(),
                new Storage(temporaryDirectory.resolve("data.txt")),
                new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)));

        meepMoop.handleCommand("unknown-command");
        assertFalse(meepMoop.wasLastCommandValid());

        meepMoop.handleCommand("list");
        assertTrue(meepMoop.wasLastCommandValid());
    }

    @Test
    void handleCommand_endToEndSequencePersistsRequestedChanges() throws IOException {
        Path dataFile = temporaryDirectory.resolve("data.txt");
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        MeepMoop meepMoop = new MeepMoop(dataFile,
                new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)));

        assertTrue(meepMoop.handleCommand("activity Museum /at 2026-09-01 0900"));
        assertTrue(meepMoop.handleCommand("book 1"));
        assertTrue(meepMoop.handleCommand("find museum"));
        assertTrue(meepMoop.handleCommand("view 2026-09-01"));
        assertTrue(meepMoop.handleCommand("delete 1"));
        assertFalse(meepMoop.handleCommand("exit"));

        assertEquals(0, meepMoop.getItinerary().getCount());
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
        MeepMoop meepMoop = new MeepMoop(dataFile, ui);
        java.io.InputStream originalInput = System.in;
        try {
            System.setIn(new ByteArrayInputStream("list\nexit\n".getBytes(StandardCharsets.UTF_8)));
            meepMoop.run();
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
        MeepMoop meepMoop = new MeepMoop(dataFile,
                new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)));

        meepMoop.run();

        assertEquals("Unable to load saved data." + System.lineSeparator()
                + "____________________________________________________________" + System.lineSeparator(),
                capturedBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void handleCommand_remainingValidCommandsPersistTheirExpectedPlans() throws IOException {
        Path dataFile = temporaryDirectory.resolve("data.txt");
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        MeepMoop meepMoop = new MeepMoop(dataFile,
                new Ui(new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)));

        assertTrue(meepMoop.handleCommand("stay Hotel /from 2026-09-01 /to 2026-09-03"));
        assertTrue(meepMoop.handleCommand("transport Flight /from Singapore /to Tokyo"));
        assertTrue(meepMoop.handleCommand("book 2"));
        assertTrue(meepMoop.handleCommand("unbook 2"));
        assertTrue(meepMoop.handleCommand("list"));

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
            new MeepMoop(itinerary, new Parser(), storage, new Ui(capturedOutput)).handleCommand(input);
        } finally {
            System.setOut(originalOutput);
        }
        return capturedBytes.toString(StandardCharsets.UTF_8);
    }
}
