package meepmoop.command;

import java.time.LocalDate;
import java.util.Objects;

import meepmoop.model.Itinerary;
import meepmoop.storage.Storage;
import meepmoop.ui.Ui;

/** Displays plans that occur on one requested date. */
public final class ViewCommand extends Command {
    private final LocalDate date;

    /** Creates a view command for the supplied date. */
    public ViewCommand(LocalDate date) {
        this.date = Objects.requireNonNull(date, "date must not be null");
    }

    /** Displays plans occurring on this command's date without changing saved data. */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) {
        ui.showPlansOn(date, itinerary);
    }
}
