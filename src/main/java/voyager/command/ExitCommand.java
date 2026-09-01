package voyager.command;

import voyager.model.Itinerary;
import voyager.storage.Storage;
import voyager.ui.Ui;

/** Represents a request to end the application loop. */
public final class ExitCommand extends Command {
    /** Performs no action because the loop checks {@link #isExit()} after execution. */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) {
        // The application loop performs the farewell after this command signals exit.
    }

    /** Signals that the application loop should end. */
    @Override
    public boolean isExit() {
        return true;
    }
}
