package meepmoop;

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
import meepmoop.model.Itinerary;
import meepmoop.model.Plan;
import meepmoop.ui.Ui;

/**
 * Provides a graphical chat interface for the MeepMoop itinerary chatbot.
 */
public final class MainWindow extends Application {
    private static final String WINDOW_TITLE = "MeepMoop Travel Planner";
    private static final String INPUT_PROMPT = "Enter a command, e.g. activity Museum";
    private static final String ACCENT_COLOR = "#f97316";
    private static final String ERROR_COLOR = "#b91c1c";

    private final ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
    private final VBox conversation = new VBox(10);
    private final VBox itineraryItems = new VBox(8);
    private MeepMoop meepMoop;
    private ScrollPane conversationPane;
    private TextField commandInput;
    private Button sendButton;

    /** Creates the graphical application. */
    public MainWindow() {
    }

    /** Displays the primary chat window. */
    @Override
    public void start(Stage stage) {
        meepMoop = new MeepMoop(MeepMoop.DEFAULT_DATA_FILE,
                new Ui(new PrintStream(responseBytes, true, StandardCharsets.UTF_8)));

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #fffaf5; -fx-font-family: 'System';");
        root.setTop(createHeader());
        root.setCenter(createMainContent());

        showBotResponse(readResponse());
        showBotResponse("Hello! I'm MeepMoop. How can I assist you today?");
        refreshItineraryList();

        Scene scene = new Scene(root, 1060, 680);
        stage.setTitle(WINDOW_TITLE);
        stage.setMinWidth(840);
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

    /** Creates the itinerary sidebar and chat panel in a 30-70 horizontal split. */
    private HBox createMainContent() {
        VBox itineraryPanel = createItineraryPanel();
        VBox chatPanel = new VBox(12, createConversationPane(), createInputBar());
        itineraryPanel.setMinWidth(240);
        chatPanel.setMinWidth(520);
        HBox.setHgrow(itineraryPanel, Priority.ALWAYS);
        HBox.setHgrow(chatPanel, Priority.ALWAYS);
        itineraryPanel.prefWidthProperty().bind(chatPanel.widthProperty().multiply(3.0 / 7.0));
        VBox.setVgrow(conversationPane, Priority.ALWAYS);
        HBox mainContent = new HBox(18, itineraryPanel, chatPanel);
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
        boolean isRunning = meepMoop.handleCommand(command);
        showBotResponse(readResponse());
        if (meepMoop.wasLastCommandValid()) {
            refreshItineraryList();
        }
        if (!isRunning) {
            showBotResponse("Goodbye! Have a great day!");
            closeApplication();
        }
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
            String backgroundColor = meepMoop != null && meepMoop.wasLastCommandValid()
                    ? "#ffffff" : "#fef2f2";
            String borderColor = meepMoop != null && meepMoop.wasLastCommandValid()
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
        Itinerary itinerary = meepMoop.getItinerary();
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
