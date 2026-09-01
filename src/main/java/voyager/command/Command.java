package voyager.command;

import voyager.exception.VoyagerException;
import voyager.model.Itinerary;
import voyager.storage.Storage;
import voyager.ui.Ui;

/** Represents one executable user command. */
public abstract class Command {
    /** Performs this command using the application's current collaborators. */
    public abstract void execute(Itinerary itinerary, Ui ui, Storage storage) throws VoyagerException;

    /** Returns whether executing this command should end the application loop. */
    public boolean isExit() {
        return false;
    }
}
