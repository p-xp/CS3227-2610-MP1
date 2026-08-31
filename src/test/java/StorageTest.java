import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests file creation, round trips, validation, capacity, and I/O failures. */
class StorageTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void load_missingFileAndParent_returnsEmptyItineraryWithoutWarning() throws IOException {
        Storage storage = new Storage(temporaryDirectory.resolve("missing/data.txt"));

        Storage.LoadResult result = storage.load();

        assertEquals(0, result.getItinerary().getCount());
        assertFalse(result.hasCorruptedRecords());
        assertFalse(Files.exists(temporaryDirectory.resolve("missing")));
    }

    @Test
    void save_missingParent_createsDirectoryAndEmptyFile() throws IOException {
        Path dataFile = temporaryDirectory.resolve("new-folder/data.txt");
        Storage storage = new Storage(dataFile);

        storage.save(new Itinerary());

        assertTrue(Files.isDirectory(dataFile.getParent()));
        assertTrue(Files.isRegularFile(dataFile));
        assertEquals("", Files.readString(dataFile, StandardCharsets.UTF_8));
    }

    @Test
    void saveThenLoad_allTypesTextOrderAndBookingStates_roundTrips() throws IOException {
        Path dataFile = temporaryDirectory.resolve("data.txt");
        Storage storage = new Storage(dataFile);
        Itinerary itinerary = new Itinerary();
        Activity activity = new Activity("Café | 夜景 \\ tour");
        Accommodation accommodation = new Accommodation(
                "海辺 Hotel", "2026-09-01", "2026-09-03");
        Transport transport = new Transport("Flight ✈", "Singapore", "São Paulo");
        accommodation.setBooked(true);
        itinerary.add(activity);
        itinerary.add(accommodation);
        itinerary.add(transport);

        storage.save(itinerary);
        Storage.LoadResult result = storage.load();

        assertFalse(result.hasCorruptedRecords());
        assertEquals(3, result.getItinerary().getCount());
        assertEquals("[A] [ ] Café | 夜景 \\ tour", result.getItinerary().get(1).toString());
        assertEquals("[S] [X] 海辺 Hotel (from: 2026-09-01 to: 2026-09-03)",
                result.getItinerary().get(2).toString());
        assertEquals("[T] [ ] Flight ✈ (from: Singapore to: São Paulo)",
                result.getItinerary().get(3).toString());
    }

    @Test
    void save_afterStateChanges_rewritesSnapshot() throws IOException {
        Storage storage = new Storage(temporaryDirectory.resolve("data.txt"));
        Itinerary itinerary = new Itinerary();
        Activity removed = new Activity("Removed");
        Activity remaining = new Activity("Remaining");
        itinerary.add(removed);
        itinerary.add(remaining);
        storage.save(itinerary);

        remaining.setBooked(true);
        itinerary.remove(1);
        storage.save(itinerary);
        Itinerary loaded = storage.load().getItinerary();

        assertEquals(1, loaded.getCount());
        assertEquals("[A] [X] Remaining", loaded.get(1).toString());

        remaining.setBooked(false);
        storage.save(itinerary);
        assertEquals("[A] [ ] Remaining", storage.load().getItinerary().get(1).toString());
    }

    @Test
    void load_mixedValidBlankAndMalformedRecords_skipsBadRecordsAndWarns() throws IOException {
        Path dataFile = temporaryDirectory.resolve("data.txt");
        List<String> lines = List.of(
                activityRecord("Valid first", false),
                "",
                "X | 0 | " + encode("unknown type"),
                "A | 2 | " + encode("invalid status"),
                "A | 0 | !!!",
                "A | 0 | " + Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(new byte[] {(byte) 0xC3, (byte) 0x28}),
                "A | 0 | ",
                "S | 0 | " + encode("Hotel") + " | " + encode("2026-02-30")
                        + " | " + encode("2026-03-01"),
                "S | 0 | " + encode("Hotel") + " | " + encode("2026-09-03")
                        + " | " + encode("2026-09-01"),
                "T | 0 | " + encode("Bus") + " | " + encode("Origin"),
                activityRecord("Valid last", true));
        Files.write(dataFile, lines, StandardCharsets.UTF_8);

        Storage.LoadResult result = new Storage(dataFile).load();

        assertTrue(result.hasCorruptedRecords());
        assertEquals(2, result.getItinerary().getCount());
        assertEquals("[A] [ ] Valid first", result.getItinerary().get(1).toString());
        assertEquals("[A] [X] Valid last", result.getItinerary().get(2).toString());
    }

    @Test
    void load_moreThanCapacity_skipsOverflowAndWarns() throws IOException {
        Path dataFile = temporaryDirectory.resolve("data.txt");
        List<String> lines = new ArrayList<>();
        for (int number = 1; number <= 101; number++) {
            lines.add(activityRecord("Plan " + number, false));
        }
        Files.write(dataFile, lines, StandardCharsets.UTF_8);

        Storage.LoadResult result = new Storage(dataFile).load();

        assertTrue(result.hasCorruptedRecords());
        assertEquals(100, result.getItinerary().getCount());
        assertEquals("Plan 100", result.getItinerary().get(100).getDescription());
    }

    @Test
    void load_pathIsDirectory_throwsIOException() throws IOException {
        Path dataFile = Files.createDirectory(temporaryDirectory.resolve("directory.txt"));

        assertThrows(IOException.class, () -> new Storage(dataFile).load());
    }

    @Test
    void save_parentIsFile_throwsIOExceptionAndDoesNotCreateDataFile() throws IOException {
        Path blocker = Files.createFile(temporaryDirectory.resolve("blocker"));
        Path dataFile = blocker.resolve("data.txt");

        assertThrows(IOException.class,
                () -> new Storage(dataFile).save(new Itinerary()));
        assertTrue(Files.isRegularFile(blocker));
        assertFalse(Files.exists(dataFile));
    }

    /** Creates a valid encoded activity record for corruption and capacity tests. */
    private static String activityRecord(String description, boolean booked) {
        return "A | " + (booked ? "1" : "0") + " | " + encode(description);
    }

    /** Encodes test data using the documented storage field representation. */
    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
