package meepmoop.command;

import meepmoop.model.Accommodation;
import meepmoop.model.Itinerary;
import meepmoop.model.Plan;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

import java.io.IOException;
import java.time.LocalDate;

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
    public void execute(Itinerary itinerary, Ui ui, Storage storage) {
        Plan plan = new Accommodation(description, from, to);
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
