package ch.unibas.dmi.cs108.sand.gui;

import ch.unibas.dmi.cs108.sand.network.CommandList;
import ch.unibas.dmi.cs108.sand.network.Message;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Syarif Hidayatullah on 4/5/2017. The lobby window is opened if user doubleclick on any available
 * lobby listed in the listView*/
public class LobbyController {
    private static boolean trumpsSelected;
    private static boolean mexicansSelected;

    @FXML
    private ImageView imageViewTrump;
    @FXML
    private ImageView imageViewMexican;
    @FXML
    private TextFlow lobbyMessages;
    @FXML
    private TextField messageTextField;
    @FXML
    private Button startGameButton;

    private ObservableList<String> ObsGamesList;
    private Animation animationTrump;
    private Animation animationMexican;
    private static final Logger log = LogManager.getLogger(LobbyController.class);

    /** Images of Trump and Mexican are initialised and set focus on the textField */
    public void initialize(){
        trumpsSelected = false;
        mexicansSelected = false;
        messageTextField.requestFocus();

        Image trump = new Image(getClass().getResourceAsStream("/Sprite/TrumpSprite.png"));
        imageViewTrump.setImage(trump);
        imageViewTrump.setFitHeight(250);
        imageViewTrump.setFitWidth(250);
        imageViewTrump.setViewport(new Rectangle2D(0, 0, 629, 500));
        animationTrump = new SpriteAnimation(imageViewTrump, Duration.millis(900), 20, 3,0,0,629,500);
        animationTrump.setCycleCount(Animation.INDEFINITE);

        Image mexican = new Image(getClass().getResourceAsStream("/Sprite/SpriteMexican1.png"));
        imageViewMexican.setImage(mexican);
        imageViewMexican.setFitHeight(250);
        imageViewMexican.setFitWidth(250);
        imageViewMexican.setViewport(new Rectangle2D(0, 0, 550, 762));
        animationMexican = new SpriteAnimation(imageViewMexican, Duration.millis(900), 20,7,0,0,550,762);
        animationMexican.setCycleCount(Animation.INDEFINITE);
    }

    /** this method is called each time the user click on the image of trump
     *  The other user will be informed that trump is chosen by the current user
     *  Animation of Trump is played and animation of mexican is stopped*/
    public void trumpsChoosen(){
        if(!trumpsSelected){
            ClientController.getClient().writeToServer(new Message(CommandList.LOBBY_CHAT, "I've choosen Trump"));
            trumpsSelected = true;
            mexicansSelected = false;
            animationTrump.play();
            animationMexican.stop();
            log.info("Trump's chosen");
        }
    }

    /** this method is called each time the user click on the image of Mexican
     *  The other user will be informed that Mexican is chosen by the current user
     *  Animation of Mexican is played and animation of trump is stopped*/
    public void mexicansChoosen(){
        if(!mexicansSelected){
            ClientController.getClient().writeToServer(new Message(CommandList.LOBBY_CHAT, "I've choosen Mexican"));
            mexicansSelected = true;
            trumpsSelected = false;
            animationMexican.play();
            animationTrump.stop();
            log.info("Mexicans chosen");
        }
    }

    /** Send method is used to send a message to the other client who is also in the lobby */
    public void send(){
        String getText = messageTextField.getText();
        if(getText.equals("")){
            return;
        }
        ClientController.getClient().lobbyChat(getText);
        messageTextField.clear();
    }

    /** hitEnter method is called if the user press the enter button and the focus is owned by the textfield. It will then call the send method */
    public void hitEnter(){
        send();
    }

    /** StartGame method is called if the start game button is pressed.
     * The game server will be informed which character is chosen and the button is disabled*/
    public void startGame(){
        ClientController.getClient().startGame(mexicansSelected);
        startGameButton.setDisable(true);
        lobbyMessages.getChildren().add(new Text("Waiting for the second client to press the start game button"+"\n"));
        log.info(ClientController.getUsername()+"has pressed the start game button");
    }

    /** This method is used to display the message on the TextFlow in the lobby.
     * @param clientName the name of current user
     * @param  message the message which is sent by the user to the server */
    void setLobbyMessages(String clientName, String message){
        String show = message.replace("\\%", "%");
        Text text = new Text(show + "\n");
        if (show.startsWith(clientName)) {
            text.setStyle("-fx-font-size: 12; -fx-fill: #0b701c;");
        } else {
            text.setStyle("-fx-font-size: 12; -fx-fill: #2b6acc;");
        }
        Platform.runLater(
                () -> {
                    lobbyMessages.getChildren().add(text);
                }
        );
    }

    void clearGamesList(){
        ObsGamesList.clear();
    }

    /** hide the lobby window */
    void hideWindow(){
        startGameButton.getScene().getWindow().hide();
    }

    /** Open default browser and go to our blog */
    public void link(){
        try {
            Desktop.getDesktop().browse(new URI("http://www.sand-unibas.blogspot.com"));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Something wrong has happened when trying to open the blog");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("Something wrong has happened when trying to open the blog");
        }
    }
}
