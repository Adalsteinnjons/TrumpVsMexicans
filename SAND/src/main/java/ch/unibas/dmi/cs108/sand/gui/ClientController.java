package ch.unibas.dmi.cs108.sand.gui;

import ch.unibas.dmi.cs108.sand.network.Client;
import ch.unibas.dmi.cs108.sand.network.CommandList;
import ch.unibas.dmi.cs108.sand.network.Message;
import ch.unibas.dmi.cs108.sand.network.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Nils Hansen, Syarif Hidayatullah on 3/15/2017.
 * This class is the controller of the Chat window. This class has reference to the client object
 */
public class ClientController {

    private static Client client;
    private int port;
    private String ip;
    private static String username;
    private ObservableList<String> gamesList;
    private static boolean isMexican;
    private GameControllerTrump trumpController;
    private GameControllerMexican mexicanController;
    private LobbyController newGameController;
    private FXMLLoader gameLoader;
    private boolean isWhispering;
    private int idToWhisper;
    private int yourId;
    private String lobbyName;
    private NewLobbyController controller;


    @FXML
    private TextFlow textAreaMessages;
    @FXML
    private TextField textFieldMessage;
    @FXML
    private TableView<User> clientList;
    @FXML
    private TableColumn<User,Integer> id;
    @FXML
    private TableColumn<User, String> clientNames;
    @FXML
    private ListView<String> openedGames;
    @FXML
    private BorderPane clientBorderPane;
    @FXML
    private javafx.scene.control.MenuItem whisperModus;
    @FXML
    private MenuItem exitWhisper;
    @FXML
    private Button newGameButton;

    private static Logger log = LogManager.getLogger(ClientController.class);


    /** erstelle neuen Client und überlade die Methode display() */
    public void initialize()throws Exception{
        isWhispering = false;
        exitWhisper.setDisable(true);
        textFieldMessage.requestFocus();
        clientList.setFocusTraversable(false);
        openedGames.setFocusTraversable(false);
        newGameButton.setFocusTraversable(false);
        openedGames.setPlaceholder(new Label("Click 'New Game' to create \n             a new lobby"));
        openedGames.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    if (openedGames.getSelectionModel().getSelectedItem() == null){
                        textAreaMessages.getChildren().add(new Text("Please create a lobby before starting a new game" + "\n"));
                    }else {
                        String gameNameSelected = openedGames.getSelectionModel().getSelectedItem();
                        client.enterGameLobby(gameNameSelected);
                        openedGames.setDisable(true);
                        startNewGame();
                        log.info(username + " enters the lobby");
                    }

                }
            }
        });

        (new Thread(){
            public void run() {

                client = new Client(ip, username, port) {

                    public void display(CommandList command, String msg) {
                        switch (command) {
                            case WELCOME:
                                Platform.runLater(
                                        () -> {
                                            Text text = new Text("Welcome to SAND, " + username + "\n" +
                                                    "Your id is "+msg+ "\n");
                                            text.setStyle("-fx-font-size: 14; -fx-fill: #cb5217;");
                                            Hyperlink info2 = new Hyperlink("http://www.noiseforfun.com/");
                                            info2.setOnAction(new EventHandler<ActionEvent>() {
                                                @Override
                                                public void handle(ActionEvent event) {
                                                    openBrowser("http://www.noiseforfun.com/");
                                                }
                                            });
                                            Text info = new Text("Credit: Sounds used in this game are taken from, amongst others, " );
                                            Text info3 = new Text(" produced by Filippo Vicarelli"+"\n");
                                            textAreaMessages.getChildren().addAll(text,info,info2,info3);
                                            yourId = Integer.valueOf(msg);
                                            log.info(username + " is connected to server");
                                        }
                                );

                                break;
                            case MESSAGE:
                                Platform.runLater(
                                        () -> {
                                            String show = msg.replace("\\%", "%");//msg needs to be final..
                                            Text text = new Text(show + "\n");
                                            if (show.startsWith(username)) {
                                                text.setStyle("-fx-font-size: 12; -fx-fill: #0b701c;");
                                            } else {
                                                text.setStyle("-fx-font-size: 12; -fx-fill: #2b6acc;");
                                            }
                                            textAreaMessages.getChildren().add(text);
                                        }
                                );

                                break;
                        }
                    }

                    public void showAvailableGames(ArrayList<String> games) {
                        try {
                            gamesList = FXCollections.observableArrayList(games);
                            Platform.runLater(
                                    () -> {
                                        openedGames.setItems(gamesList);
                                        log.info("update available lobbies");
                                    }
                            );

                        } catch (NullPointerException e) {
                            newGameController.clearGamesList();
                            log.error("Something went wrong when trying to update the opened games");
                        }
                    }

                    /**
                     * practically update character in GUI
                     */
                    public void sendCharacter(String updates) {
                        if (trumpController != null) {
                            trumpController.setPos(updates);
                        } else if (mexicanController != null) {
                            mexicanController.setPos(updates);
                        } else{
                            log.error("NEITHER TRUMP NOR MEX");

                        }
                    }

                    /** update the GUI with the new resource value */
                    public void setTacoCounter(int t){
                        if (trumpController != null) {
                            trumpController.setTacoC(t);
                        } else if (mexicanController != null) {
                            mexicanController.setTacoC(t);
                        } else{
                            System.err.println("NEITHER TRUMP NOR MEX");
                        }
                    }

                   public void letsPlayGame(CommandList playAs) {

                       if (playAs == CommandList.MEXICAN) {
                           isMexican = true;
                           gameLoader = new FXMLLoader(getClass().getResource("/fxml/gameWindowMexican.fxml"));
                           //mexicanController = gameLoader.getController();
                           System.out.println("playing as mexican");
                       } else if (playAs == CommandList.TRUMP) {
                           isMexican = false;
                           gameLoader = new FXMLLoader(getClass().getResource("/fxml/gameWindowTrump.fxml"));
                           //trumpController = gameLoader.getController();
                           System.out.println("playing as trump");
                       }

                       Platform.runLater(
                               () -> {
                                   Parent root = null;
                                   try {
                                       root = gameLoader.load();
                                       if(isMexican){
                                           mexicanController = gameLoader.getController();
                                       } else{
                                           trumpController = gameLoader.getController();
                                       }

                                   } catch (IOException e) {
                                       e.printStackTrace();
                                   }
                                   System.out.println("lets start the party");
                                   Stage gameStage = new Stage();
                                   gameStage.setTitle("Trampeltier VS Sombreros");
                                   gameStage.setScene(new Scene(root, 1500, 800));
                                   gameStage.setResizable(true);
                                   gameStage.setFullScreen(true);
                                   gameStage.setOnHiding(new EventHandler<WindowEvent>() {
                                       @Override
                                       public void handle(WindowEvent event) {
                                           //System.out.println("Game window closed by client");
                                           if(mexicanController!=null){
                                               mexicanController.getTacos().stop();
                                               mexicanController.getClip().stop();
                                               //todo close game without winner
                                               client.writeToServer(new Message(CommandList.END_GAME,username));
                                               openedGames.setDisable(false);
                                           }else if(trumpController != null){
                                               //todo close game without winner
                                               trumpController.getTacos().stop();
                                               trumpController.getClip().stop();
                                               client.writeToServer(new Message(CommandList.END_GAME,username));
                                               openedGames.setDisable(false);
                                           }
                                           mexicanController = null;
                                           trumpController = null;
                                           newGameController = null;
                                       }
                                   });
                                   gameStage.show();
                                   newGameController.hideWindow();
                                   newGameController = null;
                               }
                       );

                   }


                    public void displayLobbyChat(String message){
                        if(mexicanController != null){
                            mexicanController.setGameMessages(username,message);
                        }else if(trumpController != null){
                            trumpController.setGameMessages(username,message);
                        } if(newGameController != null){
                            newGameController.setLobbyMessages(username,message);
                        }
                    }

                    public void endGameByClient(String message){
                        if(mexicanController != null){
                            mexicanController.getTacos().stop();
                           mexicanController.setEndGameMessage(message);
                        }else if(trumpController != null){
                            trumpController.getTacos().stop();
                            trumpController.setEndGameMessage(message);
                        }
                    }

                    public void showAvailableClients(ArrayList<User> users) {
                       ObservableList<User> userList = FXCollections.observableArrayList(users);

                       id.setCellValueFactory(new PropertyValueFactory<User, Integer>("id"));
                       clientNames.setCellValueFactory(new PropertyValueFactory<User, String>("name"));
                       clientList.setItems(userList);
                    }

                    public void displayWinnerAlert(){
                        System.out.println("Congratulation, you won the game!");
                        if(trumpController!=null && mexicanController == null){
                            trumpController.displayWinnerAlert();
                        }else if(mexicanController != null && trumpController == null){
                            mexicanController.displayWinnerAlert();
                        }else {
                            System.out.println("Something went wrong both Controller have been initialized");
                        }
                    }

                    public void displayLoserAlert(){
                        System.out.println("Sorry, you lost the game!");
                        if(trumpController!=null && mexicanController == null){
                            trumpController.displayLoserAlert();
                        }else if(mexicanController != null && trumpController == null){
                            mexicanController.displayLoserAlert();
                        }else {
                            System.out.println("Something went wrong both Controller have been initialized");
                        }
                    }

                };

                if(clientBorderPane.getScene().getWindow() != null){
                    clientBorderPane.getScene().getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            client.requestLogout();
                            System.exit(1);
                        }
                    });
                }

            }
        }).start();

    log.info("finished initialising");
    }

    /** Get text from the Chat-Input and send it to the server to be broadcast
     *  if client is in whisper mode, client will send id along with the Message to the server
     *  the server will then take a PrintWriter with the same id from the hashMap and
     *  send the message only to the sender and the receiver with the same id*/
    public void send(){
        String getText = textFieldMessage.getText();
        if(getText.equals("")){
            return;
        }
        getText = getText.replace("%","\\%");
        if(isWhispering){
            Message message = new Message(CommandList.WHISPER_CHAT,String.valueOf(yourId)+"$"+textFieldMessage.getText()+"$"+String.valueOf(idToWhisper));
            client.writeToServer(message);
        }else{
            Message message = new Message(getText);
            client.writeToServer(message);

        }
        textFieldMessage.clear();
    }

    /** call the send method if user hit enter after typed in some text in the textField */
    public void hitEnter(){
        send();
    }

    /** These setter method is used to pass user inputs (Port number) from the login dialog to this controller */
    public void setPort(int port) {
        this.port = port;
    }

    /** These setter method is used to pass user inputs (Host ip) from the login dialog to this controller */
    public void setIp(String ip) {
        this.ip = ip;
    }
    /** These setter method is used to pass user inputs (user name) from the login dialog to this controller */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Exit button from the settings at the menu bar
     *  this method will send logout request to the server*/
    public void logout(){
        client.requestLogout();
    }

    /** client will be initialized in this controller, therefore client should be static
     * to make it accessible from the main method*/
    public static Client getClient(){
        return client;
    }

    /** This method will open the whispering dialog
     * the user must type in the id of target client
     * if client press the ok button, the boolean value of isWhispering will be set to true
     * and the exitWhisper mode button will appear */
    @FXML
    public void startWhisperModus(){
        WhisperDialogController controller;
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.initOwner(clientBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/whisperDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
            controller = fxmlLoader.getController();
        } catch (IOException e) {
            log.error("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK && controller.getUserId() != -1) {

            //System.out.println("whispering started");
            idToWhisper = controller.getUserId();
            Stage stage =(Stage)textFieldMessage.getScene().getWindow();
            stage.setTitle("Client-"+ username +", Whisper-Mode");
            isWhispering = true;
            whisperModus.setDisable(true);
            exitWhisper.setDisable(false);
            textAreaMessages.getChildren().add(new Text("You are now in whisper mode with a client with the id "+idToWhisper+"\n"));
        } else {
            log.info("Cancel pressed from whisper dialog");
            textAreaMessages.getChildren().add(new Text("Please input valid integer"+"\n"));
        }


    }

    /** Call this method to leave the whisper mode.
     * it will toggle the boolean  value isWhispering to false*/
    @FXML
    public void exitWhisperModus(){
        isWhispering = false;
        whisperModus.setDisable(false);
        exitWhisper.setDisable(true);
        Stage stage =(Stage)textFieldMessage.getScene().getWindow();
        stage.setTitle("Client-"+ username);
        textAreaMessages.getChildren().add(new Text("You are leaving whisper mode"+"\n"));
    }

    /** This method is called if the user press the 'change username' button in the settings menu
     * User will then asked to input a new username and if OK button is pressed
     * requestNewName method is called. */
    @FXML
    public void showUsernameDialog() {
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.initOwner(clientBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/settingsDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            NewNameController controller = fxmlLoader.getController();
            //System.out.println("OK pressed");
            username = controller.changeUsername(); // username changed
            client.requestNewName(username);
            Stage stage = (Stage)clientBorderPane.getScene().getWindow();
            stage.setTitle("Client-" + username);
        } else {
            //System.out.println("Cancel pressed");
        }
    }

    /** This method is called if user doubleclick on the name of a game-lobby listed in gameList ListView */
    @FXML
    private void startNewGame(){
        Parent root = null;
        URL resource = ServerController.class.getResource("/fxml/Lobby.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);


        try {
            root = fxmlLoader.load();
            newGameController = fxmlLoader.getController();
        } catch (IOException e) {
            System.err.println("Couldn't load new-game dialog");
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Game Lobby");
        stage.setScene(new Scene(root, 640, 660));
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        log.info("Application Closed by click to Close Button(X)");
                        newGameController = null;
                        client.writeToServer(new Message(CommandList.LEAVE_LOBBY));
                        enableOpenedGames();
                    }
                });
            }
        });
        stage.setOnHiding(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        newGameController = null;
                        enableOpenedGames();
                    }
                });
            }
        });
    }

    public void enableOpenedGames(){
        openedGames.setDisable(false);
    }

    /** Method called if user opens Highscore and clicks "Show highscore"
     *  This Method opens a new window with the highscore.
     */
    @FXML
    public void showHighscore(){
        Parent root = null;
        URL resource = ServerController.class.getResource("/fxml/Highscore.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);

        try{
            root = fxmlLoader.load();
            //newHighscoreController = fxmlLoader.getController();
        } catch (IOException e) {
            log.error("Couldn't load show Highscore");
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Highscore");
        stage.setScene(new Scene(root, 600,500));
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    public void showIntro(){
        Parent root = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/Intro.fxml"));

        try {
            root = fxmlLoader.load();
        } catch(IOException e){
            log.error("Couldn't load intro");
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Intro");
        stage.setScene(new Scene(root,600,330));
        stage.setResizable(false);
        stage.show();

    }

    @FXML
    public void showTutorial(){
        Parent root = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/Tutorial.fxml"));

        try {
            root = fxmlLoader.load();
        } catch (IOException e){
            log.error("Couln't load tutorial");
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.setTitle("Tutorial");
        stage.setScene(new Scene(root,600,400));
        stage.setResizable(false);
        stage.show();
    }

    /** Start a new game by creating a lobby and wait for other clients to join
     * createLobby dialog is opened and user is asked to give the lobby a name */
    @FXML
    public void createLobby(){
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.initOwner(clientBorderPane.getScene().getWindow());
        dialog.setTitle("Start a new game");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/createLobby.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
            controller = fxmlLoader.getController();
            controller.setPane(dialog.getDialogPane());
        } catch (IOException e) {
            log.error("Couldn't load create-lobby dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            lobbyName = controller.getLobbyName();
            ClientController.getClient().requestNewGameServer(lobbyName);
            openedGames.setDisable(false);

        }else{
            //System.out.println("Cancel pressed");
        }
    }

    public static boolean isMexican() {
        return isMexican;
    }

    public static String getUsername() {
        return username;
    }

    private void openBrowser(String url){
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Something went wrong when trying to open the web page");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("Something went wrong when trying to open the web page");
        }
    }

    /** Open default browser and go to our blog */
    public void link(){
        openBrowser("http://www.sand-unibas.blogspot.com");

    }
}
