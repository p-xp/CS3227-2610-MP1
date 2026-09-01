package voyager.command;

import java.io.IOException;
import java.time.LocalDateTime;

import voyager.exception.VoyagerException;
import voyager.model.Activity;
import voyager.model.Itinerary;
import voyager.model.Plan;
import voyager.storage.Storage;
import voyager.ui.Ui;

/** Adds an activity to the itinerary and saves the updated itinerary. */
public final class ActivityCommand extends Command {
    private final String description;
    private final LocalDateTime scheduledAt;

    /** Creates an activity command with the parser-validated activity details. */
    public ActivityCommand(String description, LocalDateTime scheduledAt) {
        this.description = description;
        this.scheduledAt = scheduledAt;
    }

    /** Adds the activity, removing it again if saving fails. */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) throws VoyagerException {
        Plan plan = new Activity(description, scheduledAt);
        if (itinerary.hasDuplicate(plan)) {
            throw new VoyagerException("Duplicate item. An identical itinerary item already exists.");
        }
        if (!itinerary.add(plan)) {
            ui.showItineraryFull();
            return;
        }
        int addedPlanNumber = itinerary.getCount();
        assert itinerary.get(addedPlanNumber) == plan
                : "a newly added activity must be the last plan before saving";
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            Plan removedPlan = itinerary.remove(addedPlanNumber);
            assert removedPlan == plan
                    : "a failed activity save must remove the activity that was just added";
            ui.showSaveError();
            return;
        }
        ui.showPlanAdded(plan, itinerary.getCount());
    }
}
