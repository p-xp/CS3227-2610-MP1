package voyager.command;

import voyager.model.Itinerary;
import voyager.storage.Storage;
import voyager.ui.Ui;

/** Displays every plan currently stored in the itinerary. */
public final class ListCommand extends Command {
    /** Displays the itinerary without changing it or its saved data. */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) {
        ui.showList();
    }
}
