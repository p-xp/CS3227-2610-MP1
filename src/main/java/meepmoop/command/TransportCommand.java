package meepmoop.command;

import meepmoop.model.Itinerary;
import meepmoop.model.Plan;
import meepmoop.model.Transport;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

import java.io.IOException;

/** Adds transport to the itinerary and saves the updated itinerary. */
public final class TransportCommand extends Command {
    private final String description;
    private final String fromLocation;
    private final String toLocation;

    /** Creates a transport command with parser-validated route details. */
    public TransportCommand(String description, String fromLocation, String toLocation) {
        this.description = description;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    /** Adds the transport plan, removing it again if saving fails. */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) {
        Plan plan = new Transport(description, fromLocation, toLocation);
        if (!itinerary.add(plan)) {
            ui.showItineraryFull();
            return;
        }
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            itinerary.remove(itinerary.getCount());
            ui.showSaveError();
            return;
        }
        ui.showPlanAdded(plan, itinerary.getCount());
    }
}
