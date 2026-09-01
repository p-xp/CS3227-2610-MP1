package voyager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests command recall storage for the graphical chat input. */
class CommandHistoryTest {
    @Test
    void record_singleCommand_makesCommandAvailableForRecall() {
        CommandHistory commandHistory = new CommandHistory();

        commandHistory.record("activity Museum");

        assertTrue(commandHistory.hasMostRecentCommand());
        assertEquals("activity Museum", commandHistory.getMostRecentCommand());
    }

    @Test
    void record_multipleCommands_replacesPreviousCommand() {
        CommandHistory commandHistory = new CommandHistory();
        commandHistory.record("activity Museum");

        commandHistory.record("activity Park");

        assertEquals("activity Park", commandHistory.getMostRecentCommand());
    }

    @Test
    void newHistory_hasNoCommandAvailableForRecall() {
        CommandHistory commandHistory = new CommandHistory();

        assertFalse(commandHistory.hasMostRecentCommand());
    }
}
