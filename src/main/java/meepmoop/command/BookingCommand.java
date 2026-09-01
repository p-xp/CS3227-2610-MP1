package meepmoop.command;

import java.io.IOException;

import meepmoop.exception.MeepException;
import meepmoop.model.Itinerary;
import meepmoop.model.Plan;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

/** Changes one plan's booking state and saves the updated itinerary. */
public final class BookingCommand extends Command {
    private final int planNumber;
    private final boolean shouldBook;

    /** Creates a booking or unbooking command for a validated one-based plan number. */
    public BookingCommand(int planNumber, boolean shouldBook) {
        this.planNumber = planNumber;
        this.shouldBook = shouldBook;
    }

    /**
     * Changes the requested plan's booking state, restoring its previous state if saving fails.
     *
     * @throws MeepException if the plan number is invalid or its state already matches this command
     */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) throws MeepException {
        Plan plan = itinerary.get(planNumber);
        if (plan == null) {
            throw new MeepException("Invalid item number");
        } else if (plan.isBooked() == shouldBook) {
            throw new MeepException("Item is already " + (shouldBook ? "booked" : "unbooked"));
        }
        plan.setBooked(shouldBook);
        assert plan.isBooked() == shouldBook
                : "a booking command must set the plan to its requested booking state";
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            assert plan.isBooked() == shouldBook
                    : "a plan must retain its requested state until a failed save is rolled back";
            plan.setBooked(!shouldBook);
            assert plan.isBooked() != shouldBook
                    : "a failed save must restore the plan's prior booking state";
            ui.showSaveError();
            return;
        }
        ui.showBookingUpdated(plan, shouldBook);
    }
}
