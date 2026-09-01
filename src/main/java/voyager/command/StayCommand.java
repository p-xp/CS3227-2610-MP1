package voyager.command;

import java.io.IOException;
import java.time.LocalDate;

import voyager.exception.VoyagerException;
import voyager.model.Accommodation;
import voyager.model.Itinerary;
import voyager.model.Plan;
import voyager.storage.Storage;
import voyager.ui.Ui;

/** Adds an accommodation stay to the itinerary and saves the updated itinerary. */
public final class StayCommand extends Command {
    private final String description;
    private final LocalDate from;
    private final LocalDate to;

    /** Creates a stay command with parser-validated accommodation details. */
    public StayCommand(String description, LocalDate from, LocalDate to) {
        this.description = description;
        this.from = from;
        this.to = to;
    }

    /** Adds the stay, removing it again if saving fails. */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) throws VoyagerException {
        Plan plan = new Accommodation(description, from, to);
        if (itinerary.hasDuplicate(plan)) {
            throw new VoyagerException("Duplicate item. An identical itinerary item already exists.");
        }
        if (!itinerary.add(plan)) {
            ui.showItineraryFull();
            return;
        }
        int addedPlanNumber = itinerary.getCount();
        assert itinerary.get(addedPlanNumber) == plan
                : "a newly added stay must be the last plan before saving";
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            Plan removedPlan = itinerary.remove(addedPlanNumber);
            assert removedPlan == plan
                    : "a failed stay save must remove the stay that was just added";
            ui.showSaveError();
            return;
        }
        ui.showPlanAdded(plan, itinerary.getCount());
    }
}
