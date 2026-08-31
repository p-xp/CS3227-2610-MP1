import java.io.IOException;

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
        try {
            storage.save(itinerary);
        } catch (IOException exception) {
            plan.setBooked(!shouldBook);
            ui.showSaveError();
            return;
        }
        ui.showBookingUpdated(plan, shouldBook);
    }
}
