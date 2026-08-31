package meepmoop.command;

import java.io.IOException;
import java.time.LocalDateTime;

import meepmoop.model.Activity;
import meepmoop.model.Itinerary;
import meepmoop.model.Plan;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

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
    public void execute(Itinerary itinerary, Ui ui, Storage storage) {
        Plan plan = new Activity(description, scheduledAt);
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
