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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import meepmoop.ui.Ui;

/**
 * Provides a graphical chat interface for the MeepMoop itinerary chatbot.
 */
public final class MainWindow extends Application {
    private static final String WINDOW_TITLE = "MeepMoop Travel Planner";
    private static final String INPUT_PROMPT = "Enter a command, e.g. activity Museum";

    private final ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
    private final VBox conversation = new VBox(10);
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
        root.setPadding(new Insets(16));
        root.setTop(createHeader());
        root.setCenter(createConversationPane());
        root.setBottom(createInputBar());

        showBotResponse(readResponse());
        showBotResponse("Hello! I'm MeepMoop. How can I assist you today?");

        Scene scene = new Scene(root, 720, 560);
        stage.setTitle(WINDOW_TITLE);
        stage.setMinWidth(520);
        stage.setMinHeight(380);
        stage.setScene(scene);
        stage.show();
        commandInput.requestFocus();
    }

    /** Creates the static heading for the chat window. */
    private VBox createHeader() {
        Label title = new Label(WINDOW_TITLE);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label helpText = new Label("Plan activities, stays, and transport with chat commands.");
        VBox header = new VBox(4, title, helpText);
        header.setPadding(new Insets(0, 0, 12, 0));
        return header;
    }

    /** Creates the scrolling transcript area. */
    private ScrollPane createConversationPane() {
        conversation.setPadding(new Insets(8));
        conversationPane = new ScrollPane(conversation);
        conversationPane.setFitToWidth(true);
        conversationPane.setStyle("-fx-background: #f5f5f5; -fx-background-color: #f5f5f5;");
        return conversationPane;
    }

    /** Creates the command text field and submission button. */
    private HBox createInputBar() {
        commandInput = new TextField();
        commandInput.setPromptText(INPUT_PROMPT);
        commandInput.setOnAction(event -> submitCommand());
        HBox.setHgrow(commandInput, javafx.scene.layout.Priority.ALWAYS);

        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> submitCommand());

        HBox inputBar = new HBox(8, commandInput, sendButton);
        inputBar.setAlignment(Pos.CENTER);
        inputBar.setPadding(new Insets(12, 0, 0, 0));
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
        if (!isRunning) {
            showBotResponse("Goodbye! Have a great day!");
            commandInput.setDisable(true);
            sendButton.setDisable(true);
        }
    }

    /** Adds a right-aligned message representing a user command. */
    private void showUserMessage(String message) {
        addMessage(message, Pos.CENTER_RIGHT, "#dbeafe");
    }

    /** Adds a left-aligned message representing one chatbot response. */
    private void showBotResponse(String message) {
        if (!message.isBlank()) {
            addMessage(message, Pos.CENTER_LEFT, "#ffffff");
        }
    }

    /** Adds a styled message label to the transcript. */
    private void addMessage(String message, Pos alignment, String backgroundColor) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(570);
        messageLabel.setStyle("-fx-background-color: " + backgroundColor
                + "; -fx-background-radius: 8; -fx-padding: 10;");
        HBox messageRow = new HBox(messageLabel);
        messageRow.setAlignment(alignment);
        conversation.getChildren().add(messageRow);
        scrollToLatestMessage();
    }

    /** Scrolls the conversation after layout so that its latest message is visible. */
    private void scrollToLatestMessage() {
        Platform.runLater(() -> conversationPane.setVvalue(1.0));
    }

    /** Returns text written by the command-line UI since the previous read. */
    private String readResponse() {
        String response = responseBytes.toString(StandardCharsets.UTF_8);
        responseBytes.reset();
        return response.replace("____________________________________________________________", "").trim();
    }
}
