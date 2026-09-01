package meepmoop.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import meepmoop.exception.MeepException;
import meepmoop.model.Activity;
import meepmoop.model.Itinerary;
import meepmoop.model.PlanType;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

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
        assertEquals("List has been manually refreshed." + System.lineSeparator()
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
    void findCommand_executeShowsOriginalNumbersForMatchingPlans() throws MeepException {
        Itinerary itinerary = new Itinerary();
        itinerary.add(new Activity("Read Book"));
        itinerary.add(new Activity("Book Tokyo Flight"));
        itinerary.add(new Activity("Return book"));
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)) {
            new FindCommand("book flight").execute(itinerary, new Ui(output), storage());
        }

        assertEquals(3, itinerary.getCount());
        assertEquals("Here are the matching items in your itinerary:" + System.lineSeparator()
                + "2. [A] [ ] Book Tokyo Flight" + System.lineSeparator()
                + SEPARATOR + System.lineSeparator(), capturedBytes.toString(StandardCharsets.UTF_8));
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

    @Test
    void deleteCommand_executeRemovesPlanAndSavesUpdatedItinerary() throws MeepException, IOException {
        Itinerary itinerary = new Itinerary();
        itinerary.add(new Activity("Museum"));
        Storage storage = storage();
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)) {
            new DeleteCommand(1).execute(itinerary, new Ui(output), storage);
        }

        assertEquals(0, itinerary.getCount());
        assertEquals(0, storage.load().getItinerary().getCount());
        assertEquals("Noted. I've removed this item:" + System.lineSeparator()
                + "[A] [ ] Museum" + System.lineSeparator()
                + "Now you have 0 items in your itinerary." + System.lineSeparator()
                + SEPARATOR + System.lineSeparator(), capturedBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void deleteCommand_whenSaveFails_restoresPlanAtOriginalPosition() throws MeepException, IOException {
        Itinerary itinerary = new Itinerary();
        Activity first = new Activity("Museum");
        Activity second = new Activity("Park");
        itinerary.add(first);
        itinerary.add(second);
        Path blocker = Files.createFile(temporaryDirectory.resolve("blocker"));
        Storage failingStorage = new Storage(blocker.resolve("meepmoop.txt"));
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)) {
            new DeleteCommand(1).execute(itinerary, new Ui(output), failingStorage);
        }

        assertEquals(2, itinerary.getCount());
        assertEquals(first, itinerary.get(1));
        assertEquals(second, itinerary.get(2));
        assertEquals("Unable to save data." + System.lineSeparator()
                + SEPARATOR + System.lineSeparator(), capturedBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void bookingCommand_executeUpdatesBookingStateAndSaves() throws MeepException, IOException {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        itinerary.add(activity);
        Storage storage = storage();

        new BookingCommand(1, true).execute(itinerary, new Ui(), storage);

        assertTrue(activity.isBooked());
        assertTrue(storage.load().getItinerary().get(1).isBooked());
    }

    @Test
    void bookingCommand_whenSaveFails_restoresPreviousBookingState() throws MeepException, IOException {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        itinerary.add(activity);
        Path blocker = Files.createFile(temporaryDirectory.resolve("booking-blocker"));
        Storage failingStorage = new Storage(blocker.resolve("meepmoop.txt"));

        new BookingCommand(1, true).execute(itinerary, new Ui(), failingStorage);

        assertFalse(activity.isBooked());
    }

    @Test
    void bookingCommand_unbookUpdatesBookingStateAndSaves() throws MeepException, IOException {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        activity.setBooked(true);
        itinerary.add(activity);
        Storage storage = storage();
        ByteArrayOutputStream capturedBytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(capturedBytes, true, StandardCharsets.UTF_8)) {
            new BookingCommand(1, false).execute(itinerary, new Ui(output), storage);
        }

        assertFalse(activity.isBooked());
        assertFalse(storage.load().getItinerary().get(1).isBooked());
        assertEquals("Unbooked: [A] [ ] Museum" + System.lineSeparator()
                + SEPARATOR + System.lineSeparator(), capturedBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void bookingCommand_whenUnbookingCannotBeSaved_restoresBookedState() throws MeepException, IOException {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        activity.setBooked(true);
        itinerary.add(activity);
        Path blocker = Files.createFile(temporaryDirectory.resolve("unbooking-blocker"));
        Storage failingStorage = new Storage(blocker.resolve("meepmoop.txt"));

        new BookingCommand(1, false).execute(itinerary, new Ui(), failingStorage);

        assertTrue(activity.isBooked());
    }

    @Test
    void bookingCommand_invalidPlanNumber_throwsExceptionWithoutChangingItinerary() {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        itinerary.add(activity);

        MeepException exception = assertThrows(
                MeepException.class, () -> new BookingCommand(2, true).execute(itinerary, new Ui(), storage()));

        assertEquals("Invalid item number", exception.getMessage());
        assertFalse(activity.isBooked());
    }

    @Test
    void bookingCommand_alreadyBookedPlan_throwsExceptionWithoutSaving() {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        activity.setBooked(true);
        itinerary.add(activity);

        MeepException exception = assertThrows(
                MeepException.class, () -> new BookingCommand(1, true).execute(itinerary, new Ui(), storage()));

        assertEquals("Item is already booked", exception.getMessage());
        assertTrue(activity.isBooked());
    }

    @Test
    void deleteCommand_invalidPlanNumber_throwsExceptionWithoutChangingItinerary() {
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Museum");
        itinerary.add(activity);

        MeepException exception = assertThrows(
                MeepException.class, () -> new DeleteCommand(2).execute(itinerary, new Ui(), storage()));

        assertEquals("Invalid item number", exception.getMessage());
        assertEquals(1, itinerary.getCount());
        assertSame(activity, itinerary.get(1));
    }

    @Test
    void activityCommand_executeAddsActivityAndSaves() throws IOException, MeepException {
        Itinerary itinerary = new Itinerary();
        Storage storage = storage();

        new ActivityCommand("Museum", null).execute(itinerary, new Ui(), storage);

        assertEquals(1, itinerary.getCount());
        assertEquals("Museum", storage.load().getItinerary().get(1).getDescription());
    }

    @Test
    void addCommands_duplicateItemThrowExceptionWithoutSaving() throws IOException {
        Itinerary itinerary = new Itinerary();
        itinerary.add(new Activity("Museum"));
        Storage storage = storage();

        MeepException exception = assertThrows(
                MeepException.class, () -> new ActivityCommand("Museum", null).execute(itinerary, new Ui(), storage));

        assertEquals("Duplicate item. An identical itinerary item already exists.", exception.getMessage());
        assertEquals(1, itinerary.getCount());
        assertFalse(Files.exists(temporaryDirectory.resolve("meepmoop.txt")));
    }

    @Test
    void activityCommand_whenSaveFails_removesAddedActivity() throws IOException, MeepException {
        Itinerary itinerary = new Itinerary();
        Path blocker = Files.createFile(temporaryDirectory.resolve("activity-blocker"));
        Storage failingStorage = new Storage(blocker.resolve("meepmoop.txt"));

        new ActivityCommand("Museum", null).execute(itinerary, new Ui(), failingStorage);

        assertEquals(0, itinerary.getCount());
    }

    @Test
    void activityCommand_fullItinerary_doesNotAddOrSavePlan() throws IOException, MeepException {
        Itinerary itinerary = fullItinerary();
        Storage storage = storage();

        new ActivityCommand("Museum", null).execute(itinerary, new Ui(), storage);

        assertEquals(100, itinerary.getCount());
        assertFalse(Files.exists(temporaryDirectory.resolve("meepmoop.txt")));
    }

    @Test
    void stayCommand_executeAddsStayAndSaves() throws IOException, MeepException {
        Itinerary itinerary = new Itinerary();
        Storage storage = storage();

        new StayCommand("Hotel", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3))
                .execute(itinerary, new Ui(), storage);

        assertEquals(1, itinerary.getCount());
        assertEquals(PlanType.ACCOMMODATION, storage.load().getItinerary().get(1).getType());
    }

    @Test
    void stayCommand_fullItinerary_doesNotAddOrSavePlan() throws IOException, MeepException {
        Itinerary itinerary = fullItinerary();
        Storage storage = storage();

        new StayCommand("Hotel", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3))
                .execute(itinerary, new Ui(), storage);

        assertEquals(100, itinerary.getCount());
        assertFalse(Files.exists(temporaryDirectory.resolve("meepmoop.txt")));
    }

    @Test
    void transportCommand_executeAddsTransportAndSaves() throws IOException, MeepException {
        Itinerary itinerary = new Itinerary();
        Storage storage = storage();

        new TransportCommand("Flight", "Singapore", "Tokyo").execute(itinerary, new Ui(), storage);

        assertEquals(1, itinerary.getCount());
        assertEquals(PlanType.TRANSPORT, storage.load().getItinerary().get(1).getType());
    }

    @Test
    void transportCommand_fullItinerary_doesNotAddOrSavePlan() throws IOException, MeepException {
        Itinerary itinerary = fullItinerary();
        Storage storage = storage();

        new TransportCommand("Flight", "Singapore", "Tokyo").execute(itinerary, new Ui(), storage);

        assertEquals(100, itinerary.getCount());
        assertFalse(Files.exists(temporaryDirectory.resolve("meepmoop.txt")));
    }

    @Test
    void findAndViewCommands_nullArgumentsThrowDescriptiveExceptions() {
        NullPointerException findException = assertThrows(
                NullPointerException.class, () -> new FindCommand(null));
        NullPointerException viewException = assertThrows(
                NullPointerException.class, () -> new ViewCommand(null));

        assertEquals("keywords must not be null", findException.getMessage());
        assertEquals("date must not be null", viewException.getMessage());
    }

    /** Returns storage that cannot be affected because these commands do not save. */
    private Storage storage() {
        return new Storage(temporaryDirectory.resolve("meepmoop.txt"));
    }

    /** Returns an itinerary containing its maximum permitted number of activity plans. */
    private static Itinerary fullItinerary() {
        Itinerary itinerary = new Itinerary();
        for (int planNumber = 1; planNumber <= 100; planNumber++) {
            assertTrue(itinerary.add(new Activity("Plan " + planNumber)));
        }
        return itinerary;
    }
}
