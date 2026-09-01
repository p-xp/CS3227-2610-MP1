package voyager.command;

import java.util.Objects;

import voyager.model.Itinerary;
import voyager.storage.Storage;
import voyager.ui.Ui;

/** Displays plans whose descriptions contain every supplied keyword. */
public final class FindCommand extends Command {
    private final String keywords;

    /** Creates a find command for the supplied space-separated keywords. */
    public FindCommand(String keywords) {
        this.keywords = Objects.requireNonNull(keywords, "keywords must not be null");
    }

    /** Displays matching plans without changing the itinerary or its saved data. */
    @Override
    public void execute(Itinerary itinerary, Ui ui, Storage storage) {
        ui.showPlansMatchingKeywords(keywords, itinerary);
    }
}
