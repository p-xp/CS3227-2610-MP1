package meepmoop.command;

import meepmoop.exception.MeepException;
import meepmoop.model.Itinerary;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

/** Represents one executable user command. */
public abstract class Command {
    /** Performs this command using the application's current collaborators. */
    public abstract void execute(Itinerary itinerary, Ui ui, Storage storage) throws MeepException;

    /** Returns whether executing this command should end the application loop. */
    public boolean isExit() {
        return false;
    }
}
