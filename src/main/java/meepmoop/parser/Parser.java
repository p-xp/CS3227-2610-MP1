package meepmoop.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import meepmoop.exception.MeepException;

/** Parses and validates one command-line input without performing the command. */
public final class Parser {
    private static final String INVALID_INPUT = "Invalid input";
    private static final String INVALID_ACTIVITY_FORMAT =
            "Invalid activity format. Use: activity <description> [/at YYYY-MM-DD HHmm]";
    private static final String INVALID_ACTIVITY_DATE =
            "Invalid activity date. Use: YYYY-MM-DD HHmm";
    private static final String INVALID_STAY_FORMAT =
            "Invalid stay format. Use: stay <name> /from <date> /to <date>";
    private static final String INVALID_STAY_DATES =
            "Invalid stay dates. Use valid dates in YYYY-MM-DD order";
    private static final String INVALID_TRANSPORT_FORMAT =
            "Invalid transport format. Use: transport <name> /from <location> /to <location>";
    private static final String INVALID_ITEM_NUMBER = "Invalid item number";
    private static final String INVALID_VIEW_DATE = "Invalid view date. Use: view YYYY-MM-DD";
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HHmm").withResolverStyle(ResolverStyle.STRICT);
    private static final Pattern MARKER_PATTERN = Pattern.compile(
            "(?<!\\S)/(from|to)(?!\\S)", Pattern.CASE_INSENSITIVE);

    /** Identifies the operation requested by a parsed command. */
    public enum CommandType {
        ACTIVITY,
        STAY,
        TRANSPORT,
        BOOK,
        UNBOOK,
        DELETE,
        LIST,
        VIEW,
        EXIT
    }

    /**
     * Holds validated arguments for one command.
     *
     * <p>Only arguments relevant to the command type are populated. Text
     * arguments are null and the item number is zero when they do not apply.</p>
     */
    public static final class ParsedCommand {
        private final CommandType type;
        private final String description;
        private final LocalDate from;
        private final LocalDate to;
        private final LocalDateTime dateTime;
        private final String fromLocation;
        private final String toLocation;
        private final int itemNumber;

        private ParsedCommand(CommandType type, String description, LocalDate from,
                              LocalDate to, LocalDateTime dateTime, int itemNumber) {
            this(type, description, from, to, dateTime, null, null, itemNumber);
        }

        private ParsedCommand(CommandType type, String description, LocalDate from,
                              LocalDate to, LocalDateTime dateTime, String fromLocation,
                              String toLocation, int itemNumber) {
            this.type = type;
            this.description = description;
            this.from = from;
            this.to = to;
            this.dateTime = dateTime;
            this.fromLocation = fromLocation;
            this.toLocation = toLocation;
            this.itemNumber = itemNumber;
        }

        /** Returns the operation represented by this command. */
        public CommandType getType() {
            return type;
        }

        /** Returns the activity, stay, or transport description when applicable. */
        public String getDescription() {
            return description;
        }

        /** Returns the start date, or the requested view date, when applicable. */
        public LocalDate getFrom() {
            return from;
        }

        /** Returns the end date when applicable. */
        public LocalDate getTo() {
            return to;
        }

        /** Returns the positive one-based item number when applicable. */
        public int getItemNumber() {
            return itemNumber;
        }

        /** Returns an activity's date and time when applicable. */
        public LocalDateTime getDateTime() {
            return dateTime;
        }

        /** Returns a transport command's origin. */
        public String getFromLocation() {
            return fromLocation;
        }

        /** Returns a transport command's destination. */
        public String getToLocation() {
            return toLocation;
        }
    }

    /**
     * Parses and validates one raw command-line input.
     *
     * @throws MeepException if the command or any of its arguments is invalid
     */
    public ParsedCommand parse(String rawInput) throws MeepException {
        String input = rawInput == null ? "" : rawInput.trim();
        if (input.isEmpty()) {
            throw new MeepException(INVALID_INPUT);
        }

        String[] inputParts = input.split("\\s+", 2);
        String keyword = inputParts[0].toLowerCase(Locale.ROOT);
        String details = inputParts.length == 2 ? inputParts[1].trim() : "";

        return switch (keyword) {
        case "activity" -> parseActivity(details);
        case "stay" -> parseStay(details);
        case "transport" -> parseTransport(details);
        case "book" -> parseItemCommand(CommandType.BOOK, details);
        case "unbook" -> parseItemCommand(CommandType.UNBOOK, details);
        case "delete" -> parseItemCommand(CommandType.DELETE, details);
        case "list" -> parseArgumentlessCommand(CommandType.LIST, details);
        case "view" -> parseView(details);
        case "exit" -> parseArgumentlessCommand(CommandType.EXIT, details);
        default -> throw new MeepException(INVALID_INPUT);
        };
    }

    /** Validates and creates an activity command. */
    private static ParsedCommand parseActivity(String details) throws MeepException {
        if (details.isEmpty()) {
            throw new MeepException(INVALID_ACTIVITY_FORMAT);
        }
        int marker = details.toLowerCase(Locale.ROOT).lastIndexOf(" /at ");
        if (marker < 0) {
            return new ParsedCommand(CommandType.ACTIVITY, details, null, null, null, 0);
        }
        String description = details.substring(0, marker).trim();
        String dateTimeText = details.substring(marker + 5).trim();
        if (description.isEmpty() || dateTimeText.toLowerCase(Locale.ROOT).contains(" /at ")) {
            throw new MeepException(INVALID_ACTIVITY_FORMAT);
        }
        try {
            return new ParsedCommand(CommandType.ACTIVITY, description, null, null,
                    LocalDateTime.parse(dateTimeText, DATE_TIME_FORMAT), 0);
        } catch (DateTimeParseException exception) {
            throw new MeepException(INVALID_ACTIVITY_DATE);
        }
    }

    /** Validates stay syntax and chronological ISO dates. */
    private static ParsedCommand parseStay(String details) throws MeepException {
        String[] parts = parseThreeParts(details, INVALID_STAY_FORMAT);
        LocalDate[] dates = validateStayDates(parts[1], parts[2]);
        return new ParsedCommand(CommandType.STAY, parts[0], dates[0], dates[1], null, 0);
    }

    /** Validates transport syntax and extracts its route fields. */
    private static ParsedCommand parseTransport(String details) throws MeepException {
        String[] parts = parseThreeParts(details, INVALID_TRANSPORT_FORMAT);
        return new ParsedCommand(CommandType.TRANSPORT, parts[0], null, null, null,
                parts[1], parts[2], 0);
    }

    /** Extracts three nonempty fields around exactly one /from and one /to marker. */
    private static String[] parseThreeParts(String details, String errorMessage) throws MeepException {
        Matcher matcher = MARKER_PATTERN.matcher(details);
        if (!matcher.find() || !matcher.group(1).equalsIgnoreCase("from")) {
            throw new MeepException(errorMessage);
        }
        int fromStart = matcher.start();
        int fromEnd = matcher.end();

        if (!matcher.find() || !matcher.group(1).equalsIgnoreCase("to")) {
            throw new MeepException(errorMessage);
        }
        int toStart = matcher.start();
        int toEnd = matcher.end();

        if (matcher.find()) {
            throw new MeepException(errorMessage);
        }

        String description = details.substring(0, fromStart).trim();
        String from = details.substring(fromEnd, toStart).trim();
        String to = details.substring(toEnd).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new MeepException(errorMessage);
        }
        return new String[] {description, from, to};
    }

    /** Requires real ISO dates whose start is not after their end. */
    private static LocalDate[] validateStayDates(String fromText, String toText) throws MeepException {
        try {
            LocalDate fromDate = LocalDate.parse(fromText);
            LocalDate toDate = LocalDate.parse(toText);
            if (fromDate.isAfter(toDate)) {
                throw new MeepException(INVALID_STAY_DATES);
            }
            return new LocalDate[] {fromDate, toDate};
        } catch (DateTimeParseException exception) {
            throw new MeepException(INVALID_STAY_DATES);
        }
    }

    /** Validates and creates a command with a positive one-based item number. */
    private static ParsedCommand parseItemCommand(CommandType type, String details) throws MeepException {
        if (!details.matches("\\d+")) {
            throw new MeepException(INVALID_ITEM_NUMBER);
        }
        try {
            int itemNumber = Integer.parseInt(details);
            if (itemNumber < 1) {
                throw new MeepException(INVALID_ITEM_NUMBER);
            }
            return new ParsedCommand(type, null, null, null, null, itemNumber);
        } catch (NumberFormatException exception) {
            throw new MeepException(INVALID_ITEM_NUMBER);
        }
    }

    /** Rejects unexpected arguments for list and exit commands. */
    private static ParsedCommand parseArgumentlessCommand(CommandType type, String details) throws MeepException {
        if (!details.isEmpty()) {
            throw new MeepException(INVALID_INPUT);
        }
        return new ParsedCommand(type, null, null, null, null, 0);
    }

    /** Parses a date-only view command, accepting and ignoring one supplied time token. */
    private static ParsedCommand parseView(String details) throws MeepException {
        String[] fields = details.split("\\s+");
        if (details.isEmpty() || fields.length > 2) {
            throw new MeepException(INVALID_VIEW_DATE);
        }
        try {
            LocalDate date = LocalDate.parse(fields[0]);
            return new ParsedCommand(CommandType.VIEW, null, date, null, null, 0);
        } catch (DateTimeParseException exception) {
            throw new MeepException(INVALID_VIEW_DATE);
        }
    }
}
