package meepmoop.command;

import java.io.IOException;

import meepmoop.exception.MeepException;
import meepmoop.model.Itinerary;
import meepmoop.model.Plan;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

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
     * @throws MeepException if the requested plan number is not in the itinerary
     */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) throws MeepException {
        Plan removedPlan = itinerary.remove(planNumber);
        if (removedPlan == null) {
            throw new MeepException("Invalid item number");
        }
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            itinerary.restore(planNumber, removedPlan);
            ui.showSaveError();
            return;
        }
        ui.showPlanDeleted(removedPlan, itinerary.getCount());
    }
}
