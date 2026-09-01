package voyager;

/** Stores the most recently submitted chat command for input recall. */
final class CommandHistory {
    private String mostRecentCommand = "";

    /** Records a non-empty command that was submitted by the user. */
    void record(String command) {
        mostRecentCommand = command;
    }

    /** Returns whether a command is available to recall. */
    boolean hasMostRecentCommand() {
        return !mostRecentCommand.isEmpty();
    }

    /** Returns the command most recently recorded. */
    String getMostRecentCommand() {
        return mostRecentCommand;
    }
}
