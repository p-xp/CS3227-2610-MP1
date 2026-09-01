package voyager.command;

import java.io.IOException;

import voyager.exception.VoyagerException;
import voyager.model.Itinerary;
import voyager.model.Plan;
import voyager.model.Transport;
import voyager.storage.Storage;
import voyager.ui.Ui;

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
    public void execute(Itinerary itinerary, Ui ui, Storage storage) throws VoyagerException {
        Plan plan = new Transport(description, fromLocation, toLocation);
        if (itinerary.hasDuplicate(plan)) {
            throw new VoyagerException("Duplicate item. An identical itinerary item already exists.");
        }
        if (!itinerary.add(plan)) {
            ui.showItineraryFull();
            return;
        }
        int addedPlanNumber = itinerary.getCount();
        assert itinerary.get(addedPlanNumber) == plan
                : "newly added transport must be the last plan before saving";
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            Plan removedPlan = itinerary.remove(addedPlanNumber);
            assert removedPlan == plan
                    : "a failed transport save must remove the transport that was just added";
            ui.showSaveError();
            return;
        }
        ui.showPlanAdded(plan, itinerary.getCount());
    }
}
