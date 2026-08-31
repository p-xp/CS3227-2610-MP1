import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests command recognition, argument extraction, and parser validation rules. */
class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void parse_activityWithMixedCaseAndWhitespace_returnsTrimmedDescription() throws MeepException {
        Parser.ParsedCommand command = parser.parse("  AcTiViTy   Night  Safari  ");

        assertAll(
                () -> assertEquals(Parser.CommandType.ACTIVITY, command.getType()),
                () -> assertEquals("Night  Safari", command.getDescription()),
                () -> assertNull(command.getFrom()),
                () -> assertNull(command.getTo()),
                () -> assertEquals(0, command.getItemNumber()));
    }

    @Test
    void parse_stayWithMixedCaseMarkers_returnsValidatedFields() throws MeepException {
        Parser.ParsedCommand command = parser.parse(
                "STAY Beach Hotel /FROM 2028-02-29 /tO 2028-03-02");

        assertAll(
                () -> assertEquals(Parser.CommandType.STAY, command.getType()),
                () -> assertEquals("Beach Hotel", command.getDescription()),
                () -> assertEquals("2028-02-29", command.getFrom()),
                () -> assertEquals("2028-03-02", command.getTo()));
    }

    @Test
    void parse_stayWithSameStartAndEndDate_returnsCommand() throws MeepException {
        Parser.ParsedCommand command = parser.parse(
                "stay Hotel /from 2026-09-01 /to 2026-09-01");

        assertEquals(Parser.CommandType.STAY, command.getType());
        assertEquals("2026-09-01", command.getFrom());
        assertEquals("2026-09-01", command.getTo());
    }

    @Test
    void parse_transportWithMultiwordLocations_returnsRouteFields() throws MeepException {
        Parser.ParsedCommand command = parser.parse(
                "transport Airport Shuttle /from Changi Airport /to City Hall");

        assertAll(
                () -> assertEquals(Parser.CommandType.TRANSPORT, command.getType()),
                () -> assertEquals("Airport Shuttle", command.getDescription()),
                () -> assertEquals("Changi Airport", command.getFrom()),
                () -> assertEquals("City Hall", command.getTo()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"book 7", "unbook 7", "delete 7"})
    void parse_itemCommandWithPositiveNumber_returnsNumber(String input) throws MeepException {
        Parser.ParsedCommand command = parser.parse(input);
        Parser.CommandType expectedType = Parser.CommandType.valueOf(
                input.substring(0, input.indexOf(' ')).toUpperCase());

        assertEquals(expectedType, command.getType());
        assertEquals(7, command.getItemNumber());
    }

    @Test
    void parse_itemCommandWithLeadingZeroes_returnsPositiveNumber() throws MeepException {
        Parser.ParsedCommand command = parser.parse("book 001");

        assertEquals(1, command.getItemNumber());
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "LiSt", "exit", "EXIT"})
    void parse_argumentlessCommand_returnsRecognizedType(String input) throws MeepException {
        Parser.ParsedCommand command = parser.parse(input);

        assertEquals(Parser.CommandType.valueOf(input.toUpperCase()), command.getType());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "unknown", "list now", "exit now"})
    void parse_invalidGeneralInput_throwsInvalidInput(String input) {
        assertParseError(input, "Invalid input");
    }

    @ParameterizedTest
    @ValueSource(strings = {"activity", "activity   "})
    void parse_activityWithoutDescription_throwsActivityFormatError(String input) {
        assertParseError(input, "Invalid activity format. Use: activity <description>");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "stay Hotel /to 2026-09-03 /from 2026-09-01",
        "stay /from 2026-09-01 /to 2026-09-03",
        "stay Hotel /from /to 2026-09-03",
        "stay Hotel /from 2026-09-01 /to",
        "stay Hotel /from 2026-09-01 /to 2026-09-03 /to 2026-09-04",
        "stay Hotel /from 2026-09-01 /from 2026-09-02 /to 2026-09-03",
        "stay Hotel/from 2026-09-01 /to 2026-09-03"
    })
    void parse_stayWithMalformedMarkers_throwsStayFormatError(String input) {
        assertParseError(input,
                "Invalid stay format. Use: stay <name> /from <date> /to <date>");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "stay Hotel /from 2026-02-30 /to 2026-03-01",
        "stay Hotel /from 2026-9-1 /to 2026-09-02",
        "stay Hotel /from 2026-09-03 /to 2026-09-01"
    })
    void parse_stayWithInvalidDates_throwsStayDateError(String input) {
        assertParseError(input,
                "Invalid stay dates. Use valid dates in YYYY-MM-DD order");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "transport Bus /to Tokyo /from Singapore",
        "transport /from Singapore /to Tokyo",
        "transport Bus /from /to Tokyo",
        "transport Bus /from Singapore /to",
        "transport Bus /from Singapore /to Tokyo /to Osaka",
        "transport Bus /from Singapore /from Osaka /to Tokyo"
    })
    void parse_transportWithMalformedMarkers_throwsTransportFormatError(String input) {
        assertParseError(input,
                "Invalid transport format. Use: transport <name> /from <location> /to <location>");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "book", "book 0", "book -1", "book x", "book 1 2", "book +1",
        "unbook", "unbook 0", "delete", "delete 999999999999999999999"
    })
    void parse_itemCommandWithInvalidNumber_throwsItemNumberError(String input) {
        assertParseError(input, "Invalid item number");
    }

    /** Asserts that parsing fails with the exact user-facing error message. */
    private void assertParseError(String input, String expectedMessage) {
        MeepException exception = assertThrows(MeepException.class, () -> parser.parse(input));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
