package meepmoop.storage;

import meepmoop.model.Accommodation;
import meepmoop.model.Activity;
import meepmoop.model.Itinerary;
import meepmoop.model.Plan;
import meepmoop.model.Transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/** Loads and saves itinerary plans in a line-oriented UTF-8 data file. */
public class Storage {
    private static final String FIELD_SEPARATOR = " | ";
    private final Path dataFile;

    /** Creates storage that reads from and writes to the supplied relative or absolute path. */
    public Storage(Path dataFile) {
        this.dataFile = Objects.requireNonNull(dataFile, "dataFile must not be null");
    }

    /**
     * Loads every valid record, retaining its file order and skipping corrupted records.
     * A missing data file represents a new, empty itinerary.
     *
     * @throws IOException if an existing file cannot be read
     */
    public LoadResult load() throws IOException {
        Itinerary itinerary = new Itinerary();
        if (Files.notExists(dataFile)) {
            return new LoadResult(itinerary, false);
        }

        boolean hasCorruptedRecords = false;
        for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            try {
                Plan plan = parsePlan(line);
                if (!itinerary.add(plan)) {
                    hasCorruptedRecords = true;
                }
            } catch (IllegalArgumentException exception) {
                hasCorruptedRecords = true;
            }
        }
        return new LoadResult(itinerary, hasCorruptedRecords);
    }

    /**
     * Writes a complete snapshot, creating the parent directory when necessary.
     * A temporary sibling file prevents a partial write from replacing good data.
     *
     * @throws IOException if the snapshot cannot be written or moved into place
     */
    public void save(Itinerary itinerary) throws IOException {
        Objects.requireNonNull(itinerary, "itinerary must not be null");
        Path absoluteFile = dataFile.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        if (parent == null) {
            throw new IOException("data file has no parent directory");
        }
        Files.createDirectories(parent);

        List<String> records = new ArrayList<>();
        for (int planNumber = 1; planNumber <= itinerary.getCount(); planNumber++) {
            records.add(formatPlan(itinerary.get(planNumber)));
        }

        Path temporaryFile = Files.createTempFile(parent, absoluteFile.getFileName().toString(), ".tmp");
        try {
            Files.write(temporaryFile, records, StandardCharsets.UTF_8);
            moveIntoPlace(temporaryFile, absoluteFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /** Moves a completed snapshot over the old file, preferring an atomic replacement. */
    private static void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Converts one plan into its stable on-disk record. */
    private static String formatPlan(Plan plan) {
        String booked = plan.isBooked() ? "1" : "0";
        return switch (plan.getType()) {
        case ACTIVITY -> {
            Activity activity = (Activity) plan;
            yield activity.getScheduledAt() == null
                    ? String.join(FIELD_SEPARATOR, "A", booked, encode(plan.getDescription()))
                    : String.join(FIELD_SEPARATOR, "A", booked, encode(plan.getDescription()),
                    encode(activity.getScheduledAt().toString()));
        }
        case ACCOMMODATION -> {
            Accommodation accommodation = (Accommodation) plan;
            yield String.join(FIELD_SEPARATOR, "S", booked,
                    encode(plan.getDescription()), encode(accommodation.getFromDate().toString()),
                    encode(accommodation.getToDate().toString()));
        }
        case TRANSPORT -> {
            Transport transport = (Transport) plan;
            yield String.join(FIELD_SEPARATOR, "T", booked,
                    encode(plan.getDescription()), encode(transport.getFromLocation()),
                    encode(transport.getToLocation()));
        }
        };
    }

    /** Parses and validates one nonblank data-file record. */
    private static Plan parsePlan(String line) {
        String[] fields = line.split(" \\| ", -1);
        if (fields.length < 3 || !(fields[1].equals("0") || fields[1].equals("1"))) {
            throw new IllegalArgumentException("invalid record structure");
        }

        Plan plan = switch (fields[0]) {
        case "A" -> parseActivity(fields);
        case "S" -> parseAccommodation(fields);
        case "T" -> parseTransport(fields);
        default -> throw new IllegalArgumentException("unknown plan type");
        };
        plan.setBooked(fields[1].equals("1"));
        return plan;
    }

    /** Parses a legacy undated activity or an activity with a scheduled date and time. */
    private static Activity parseActivity(String[] fields) {
        if (fields.length == 3) {
            return new Activity(decodeNonempty(fields[2]));
        }
        requireFieldCount(fields, 4);
        return new Activity(decodeNonempty(fields[2]), LocalDateTime.parse(decodeNonempty(fields[3])));
    }

    /** Parses an accommodation record and validates its chronological ISO dates. */
    private static Accommodation parseAccommodation(String[] fields) {
        requireFieldCount(fields, 5);
        String description = decodeNonempty(fields[2]);
        String fromDate = decodeNonempty(fields[3]);
        String toDate = decodeNonempty(fields[4]);
        try {
            LocalDate from = LocalDate.parse(fromDate);
            LocalDate to = LocalDate.parse(toDate);
            if (from.isAfter(to)) {
                throw new IllegalArgumentException("accommodation dates are reversed");
            }
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("invalid accommodation date", exception);
        }
        return new Accommodation(description, LocalDate.parse(fromDate), LocalDate.parse(toDate));
    }

    /** Parses a transport record with an origin and destination. */
    private static Transport parseTransport(String[] fields) {
        requireFieldCount(fields, 5);
        return new Transport(decodeNonempty(fields[2]),
                decodeNonempty(fields[3]), decodeNonempty(fields[4]));
    }

    /** Requires the exact number of fields for a record type. */
    private static void requireFieldCount(String[] fields, int expectedCount) {
        if (fields.length != expectedCount) {
            throw new IllegalArgumentException("incorrect field count");
        }
    }

    /** Encodes arbitrary user text without introducing the record delimiter. */
    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /** Decodes a nonempty, well-formed UTF-8 text field. */
    private static String decodeNonempty(String encoded) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encoded);
            String decoded = decodeUtf8(bytes);
            if (decoded.isEmpty()) {
                throw new IllegalArgumentException("empty text field");
            }
            return decoded;
        } catch (CharacterCodingException exception) {
            throw new IllegalArgumentException("invalid UTF-8 field", exception);
        }
    }

    /** Decodes UTF-8 strictly so malformed byte sequences count as corruption. */
    private static String decodeUtf8(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString();
    }

    /** Contains the valid loaded itinerary and whether any corrupted records were skipped. */
    public static final class LoadResult {
        private final Itinerary itinerary;
        private final boolean hasCorruptedRecords;

        private LoadResult(Itinerary itinerary, boolean hasCorruptedRecords) {
            this.itinerary = itinerary;
            this.hasCorruptedRecords = hasCorruptedRecords;
        }

        /** Returns the itinerary reconstructed from valid records. */
        public Itinerary getItinerary() {
            return itinerary;
        }

        /** Returns whether one or more nonblank records were invalid or exceeded capacity. */
        public boolean hasCorruptedRecords() {
            return hasCorruptedRecords;
        }
    }
}
