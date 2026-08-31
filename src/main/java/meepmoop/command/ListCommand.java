package meepmoop.command;

import meepmoop.model.Itinerary;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

/** Displays every plan currently stored in the itinerary. */
public final class ListCommand extends Command {
    /** Displays the itinerary without changing it or its saved data. */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) {
        ui.showList(itinerary);
    }
}
