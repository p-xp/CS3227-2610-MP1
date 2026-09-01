package voyager.command;

import java.io.IOException;

import voyager.exception.VoyagerException;
import voyager.model.Itinerary;
import voyager.model.Plan;
import voyager.storage.Storage;
import voyager.ui.Ui;

/** Removes one plan from the itinerary and saves the updated itinerary. */
public final class DeleteCommand extends Command {
    private final int planNumber;

    /** Creates a delete command for a validated one-based plan number. */
    public DeleteCommand(int planNumber) {
        this.planNumber = planNumber;
    }

    /**
     * Removes the requested plan, restoring it at its original position if saving fails.
     *
     * @throws VoyagerException if the requested plan number is not in the itinerary
     */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) throws VoyagerException {
        int originalPlanCount = itinerary.getCount();
        Plan removedPlan = itinerary.remove(planNumber);
        if (removedPlan == null) {
            throw new VoyagerException("Invalid item number");
        }
        assert itinerary.getCount() == originalPlanCount - 1
                : "removing one plan must reduce the itinerary count by one";
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            itinerary.restore(planNumber, removedPlan);
            assert itinerary.getCount() == originalPlanCount && itinerary.get(planNumber) == removedPlan
                    : "a failed deletion save must restore the plan and its original position";
            ui.showSaveError();
            return;
        }
        ui.showPlanDeleted(removedPlan, itinerary.getCount());
    }
}
