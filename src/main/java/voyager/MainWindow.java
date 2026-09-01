package voyager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import voyager.model.Itinerary;
import voyager.model.Plan;
import voyager.ui.Ui;

/**
 * Provides a graphical chat interface for the Voyager itinerary chatbot.
 */
public final class MainWindow extends Application {
    private static final String WINDOW_TITLE = "Voyager Travel Planner";
    private static final String INPUT_PROMPT = "Enter a command, e.g. activity Museum";
    private static final String ACCENT_COLOR = "#f97316";
    private static final String ERROR_COLOR = "#b91c1c";
    private static final double SIDEBAR_WIDTH = 220;
    private static final double COLLAPSED_COMMAND_PANEL_WIDTH = 64;
    private static final String[] COMMAND_REFERENCE = {
        "activity <description> [/at YYYY-MM-DD HHmm]",
        "stay <name> /from <date> /to <date>",
        "transport <name> /from <location> /to <location>",
        "book <item number>",
        "unbook <item number>",
        "delete <item number>",
        "list",
        "view <date>",
        "find <keywords>",
        "help (toggle this panel)",
        "exit"
    };

    private final ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
    private final VBox conversation = new VBox(10);
    private final VBox itineraryItems = new VBox(8);
    private Voyager voyager;
    private ScrollPane commandReferencePane;
    private VBox commandReferencePanel;
    private Label commandReferenceTitle;
    private Button commandReferenceToggleButton;
    private ScrollPane conversationPane;
    private TextField commandInput;
    private Button sendButton;

    /** Creates the graphical application. */
    public MainWindow() {
    }

    /** Displays the primary chat window. */
    @Override
    public void start(Stage stage) {
        voyager = new Voyager(Voyager.DEFAULT_DATA_FILE,
                new Ui(new PrintStream(responseBytes, true, StandardCharsets.UTF_8)));

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #fffaf5; -fx-font-family: 'System';");
        root.setTop(createHeader());
        root.setCenter(createMainContent());

        showBotResponse(readResponse());
        showBotResponse("Hello! I'm Voyager. How can I assist you today?");
        refreshItineraryList();

        Scene scene = new Scene(root, 1180, 680);
        stage.setTitle(WINDOW_TITLE);
        stage.setMinWidth(900);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
        commandInput.requestFocus();
    }

    /** Creates the static heading for the chat window. */
    private VBox createHeader() {
        Label title = new Label(WINDOW_TITLE);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        Label helpText = new Label("Plan activities, stays, and transport with chat commands.");
        helpText.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        VBox header = new VBox(4, title, helpText);
        header.setPadding(new Insets(0, 0, 18, 0));
        return header;
    }

    /** Creates the itinerary, chat, and command-reference panels. */
    private HBox createMainContent() {
        VBox itineraryPanel = createItineraryPanel();
        commandReferencePanel = createCommandReferencePanel();
        VBox chatPanel = new VBox(12, createConversationPane(), createInputBar());
        itineraryPanel.setPrefWidth(SIDEBAR_WIDTH);
        itineraryPanel.setMinWidth(SIDEBAR_WIDTH);
        itineraryPanel.setMaxWidth(SIDEBAR_WIDTH);
        setCommandReferencePanelWidth(SIDEBAR_WIDTH);
        chatPanel.setMinWidth(420);
        HBox.setHgrow(chatPanel, Priority.ALWAYS);
        VBox.setVgrow(conversationPane, Priority.ALWAYS);
        HBox mainContent = new HBox(18, itineraryPanel, chatPanel, commandReferencePanel);
        HBox.setHgrow(chatPanel, Priority.ALWAYS);
        return mainContent;
    }

    /** Creates the independently scrollable itinerary sidebar. */
    private VBox createItineraryPanel() {
        Label title = new Label("YOUR ITINERARY");
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_COLOR + ";");
        itineraryItems.setPadding(new Insets(4));
        ScrollPane itineraryPane = new ScrollPane(itineraryItems);
        itineraryPane.setFitToWidth(true);
        itineraryPane.setStyle("-fx-background: #ffffff; -fx-background-color: #ffffff;"
                + " -fx-border-color: #fed7aa; -fx-border-radius: 10; -fx-background-radius: 10;");
        VBox itineraryPanel = new VBox(10, title, itineraryPane);
        VBox.setVgrow(itineraryPane, Priority.ALWAYS);
        return itineraryPanel;
    }

    /** Creates a collapsible reference list of commands accepted by the chatbot. */
    private VBox createCommandReferencePanel() {
        commandReferenceTitle = new Label("COMMANDS");
        commandReferenceTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                + ACCENT_COLOR + ";");

        commandReferenceToggleButton = new Button("Hide");
        commandReferenceToggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + ACCENT_COLOR
                + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        commandReferenceToggleButton.setOnAction(event -> toggleCommandReference());
        HBox heading = new HBox(commandReferenceTitle, commandReferenceToggleButton);
        HBox.setHgrow(commandReferenceTitle, Priority.ALWAYS);
        heading.setAlignment(Pos.CENTER_LEFT);

        VBox commandItems = new VBox(8);
        commandItems.setPadding(new Insets(4));
        for (String command : COMMAND_REFERENCE) {
            Label commandLabel = new Label(command);
            commandLabel.setWrapText(true);
            commandLabel.setMaxWidth(SIDEBAR_WIDTH - 24);
            commandLabel.setStyle("-fx-background-color: #fff7ed; -fx-border-color: #fed7aa;"
                    + " -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8;"
                    + " -fx-text-fill: #374151; -fx-font-family: 'Monospaced'; -fx-font-size: 11px;");
            commandItems.getChildren().add(commandLabel);
        }

        commandReferencePane = new ScrollPane(commandItems);
        commandReferencePane.setFitToWidth(true);
        commandReferencePane.setStyle("-fx-background: #ffffff; -fx-background-color: #ffffff;"
                + " -fx-border-color: #fed7aa; -fx-border-radius: 10; -fx-background-radius: 10;");
        VBox commandReferencePanel = new VBox(10, heading, commandReferencePane);
        VBox.setVgrow(commandReferencePane, Priority.ALWAYS);
        return commandReferencePanel;
    }

    /** Collapses or expands the command-reference panel to adjust the chat panel's available space. */
    private boolean toggleCommandReference() {
        boolean isShowing = commandReferencePane.isVisible();
        commandReferencePane.setVisible(!isShowing);
        commandReferencePane.setManaged(!isShowing);
        commandReferenceTitle.setVisible(!isShowing);
        commandReferenceTitle.setManaged(!isShowing);
        commandReferenceToggleButton.setText(isShowing ? "Show" : "Hide");
        setCommandReferencePanelWidth(isShowing ? COLLAPSED_COMMAND_PANEL_WIDTH : SIDEBAR_WIDTH);
        return !isShowing;
    }

    /** Sets the command-reference panel's fixed width for its expanded or collapsed state. */
    private void setCommandReferencePanelWidth(double width) {
        commandReferencePanel.setPrefWidth(width);
        commandReferencePanel.setMinWidth(width);
        commandReferencePanel.setMaxWidth(width);
    }

    /** Creates the scrolling transcript area. */
    private ScrollPane createConversationPane() {
        conversation.setPadding(new Insets(12));
        conversationPane = new ScrollPane(conversation);
        conversationPane.setFitToWidth(true);
        conversationPane.setStyle("-fx-background: #ffffff; -fx-background-color: #ffffff;"
                + " -fx-border-color: #e5e7eb; -fx-border-radius: 10; -fx-background-radius: 10;");
        return conversationPane;
    }

    /** Creates the command text field and submission button. */
    private HBox createInputBar() {
        commandInput = new TextField();
        commandInput.setPromptText(INPUT_PROMPT);
        commandInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 12;");
        commandInput.setOnAction(event -> submitCommand());
        HBox.setHgrow(commandInput, Priority.ALWAYS);

        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white;"
                + " -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 18;");
        sendButton.setOnAction(event -> submitCommand());

        HBox inputBar = new HBox(8, commandInput, sendButton);
        inputBar.setAlignment(Pos.CENTER);
        inputBar.setPadding(new Insets(2, 0, 0, 0));
        return inputBar;
    }

    /** Sends the text field's command to the chatbot and adds its reply to the transcript. */
    private void submitCommand() {
        String command = commandInput.getText().trim();
        if (command.isEmpty()) {
            return;
        }

        showUserMessage(command);
        commandInput.clear();
        if (command.equalsIgnoreCase("help")) {
            boolean isPanelShown = toggleCommandReference();
            showCommandReferenceStatus(isPanelShown);
            return;
        }
        boolean isRunning = voyager.handleCommand(command);
        showBotResponse(readResponse());
        if (voyager.wasLastCommandValid()) {
            refreshItineraryList();
        }
        if (!isRunning) {
            showBotResponse("Goodbye! Have a great day!");
            closeApplication();
        }
    }

    /** Displays a confirmation that the command-reference panel changed visibility. */
    private void showCommandReferenceStatus(boolean isPanelShown) {
        String message = isPanelShown ? "Command reference shown." : "Command reference hidden.";
        addMessage(message, Pos.CENTER_LEFT, "#ffffff", "#e5e7eb");
    }

    /** Closes the JavaFX window and ends the application after the exit command. */
    private void closeApplication() {
        Platform.exit();
    }

    /** Adds a right-aligned message representing a user command. */
    private void showUserMessage(String message) {
        addMessage(message, Pos.CENTER_RIGHT, "#dbeafe");
    }

    /** Adds a left-aligned message representing one chatbot response. */
    private void showBotResponse(String message) {
        if (!message.isBlank()) {
            String backgroundColor = voyager != null && voyager.wasLastCommandValid()
                    ? "#ffffff" : "#fef2f2";
            String borderColor = voyager != null && voyager.wasLastCommandValid()
                    ? "#e5e7eb" : "#fecaca";
            addMessage(message, Pos.CENTER_LEFT, backgroundColor, borderColor);
        }
    }

    /** Adds a styled message label to the transcript. */
    private void addMessage(String message, Pos alignment, String backgroundColor) {
        addMessage(message, alignment, backgroundColor, "transparent");
    }

    /** Adds a styled message label to the transcript with a subtle border. */
    private void addMessage(String message, Pos alignment, String backgroundColor, String borderColor) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(650);
        messageLabel.setStyle("-fx-background-color: " + backgroundColor
                + "; -fx-border-color: " + borderColor + "; -fx-background-radius: 10;"
                + " -fx-border-radius: 10; -fx-padding: 10 12; -fx-text-fill: "
                + (borderColor.equals("#fecaca") ? ERROR_COLOR : "#374151") + ";");
        HBox messageRow = new HBox(messageLabel);
        messageRow.setAlignment(alignment);
        conversation.getChildren().add(messageRow);
        scrollToLatestMessage();
    }

    /** Scrolls the conversation after layout so that its latest message is visible. */
    private void scrollToLatestMessage() {
        Platform.runLater(() -> conversationPane.setVvalue(1.0));
    }

    /** Rebuilds the sidebar from the current itinerary in its one-based display order. */
    private void refreshItineraryList() {
        itineraryItems.getChildren().clear();
        Itinerary itinerary = voyager.getItinerary();
        if (itinerary == null || itinerary.getCount() == 0) {
            Label emptyLabel = new Label("No plans yet. Add one through chat.");
            emptyLabel.setWrapText(true);
            emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-padding: 12;");
            itineraryItems.getChildren().add(emptyLabel);
            return;
        }

        for (int index = 1; index <= itinerary.getCount(); index++) {
            Plan plan = itinerary.get(index);
            Label planLabel = new Label(index + ". " + plan);
            planLabel.setWrapText(true);
            planLabel.setMaxWidth(220);
            planLabel.setStyle("-fx-background-color: #fff7ed; -fx-border-color: #fed7aa;"
                    + " -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10;"
                    + " -fx-text-fill: #374151;");
            itineraryItems.getChildren().add(planLabel);
        }
    }

    /** Returns text written by the command-line UI since the previous read. */
    private String readResponse() {
        String response = responseBytes.toString(StandardCharsets.UTF_8);
        responseBytes.reset();
        return response.replace("____________________________________________________________", "").trim();
    }
}
