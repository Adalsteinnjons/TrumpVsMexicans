package ch.unibas.dmi.cs108.sand.gui;

import ch.unibas.dmi.cs108.sand.logic.Character;
import ch.unibas.dmi.cs108.sand.network.CommandList;
import ch.unibas.dmi.cs108.sand.network.Message;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Syarif Hidayatullah on 4/18/2017.
 */
public class GameController {
    @FXML
    private TextFlow money;
    @FXML
    private TextField messageTextField;
    @FXML
    private TextFlow chatMessages;
    @FXML
    private BorderPane pane;

    private static Logger log = LogManager.getLogger(GameController.class);
    private Timeline tacos;
    private int tacosCollected=20;
    private Random random = new Random();

    private Rectangle wall = new Rectangle(500,50,75,75);

    private ImageView oldTacos;
    private Image trumpImg = new Image(getClass().getResourceAsStream("/Sprite/TrumpSprite.png"));
    private Image trumpFightingImg = new Image(getClass().getResourceAsStream("/Sprite/TrumpFighting.png"));
    private Image mexicanImg = new Image(getClass().getResourceAsStream("/Sprite/SpriteMexican1.png"));
    private Image bullImg = new Image(getClass().getResourceAsStream("/Sprite/BullWalking.png"));
    private Image bullDyingImg = new Image(getClass().getResourceAsStream("/Sprite/BullDying.png"));
    private Image bullFightingImg = new Image(getClass().getResourceAsStream("/Sprite/BullFighting.png"));
    private Image donkeyImg = new Image(getClass().getResourceAsStream("/Sprite/DonkeyWalking.png"));
    private Image donkeyFightingImg = new Image(getClass().getResourceAsStream("/Sprite/DonkeyFighting.png"));
    private Image donkeyDyingImg = new Image(getClass().getResourceAsStream("/Sprite/DonkeyDying.png"));
    private Image trumpDyingImg = new Image(getClass().getResourceAsStream("/Sprite/TrumpDying.png"));
    private Image mexicanDyingImg = new Image(getClass().getResourceAsStream("/Sprite/MexicanDying.png"));
    private Image mexicanFightingImg = new Image(getClass().getResourceAsStream("/Sprite/MexicanFighting.png"));
    static ConcurrentHashMap<Integer,ImageView> mexicans = new ConcurrentHashMap<Integer,ImageView>();
    static ConcurrentHashMap<Integer,ImageView> trumps = new ConcurrentHashMap<Integer,ImageView>();


    /** check if an imageview needs to change its state (from attacking to walking or from walking to attacking)
     * @return null if the state didn't change, "walk" if it needs to change from fighting to walking, "fight" if it needs to change from walking to attacking*/
    private String switchAttackingState(Character c, ImageView i){
        if(i==null){
            return null;
        }
        boolean attacking = false;
        switch (c.getType()) {
            case TRUMP:
                attacking = i.getViewport().getHeight() != 500;
                break;
            case BULL:
                attacking = i.getViewport().getHeight() != 323;
                break;
            case MEXICAN:
                attacking = i.getViewport().getHeight() != 762;
                break;
            case DONKEY:
                attacking = i.getViewport().getHeight() != 563;
                break;
        }

        if(c.isAttacking() != attacking){
            if(c.isAttacking()){
                return "fight";
            } else{
                return "walk";
            }
        }

        return null;
    }

    private void switchAnimation(Character c){
        ImageView j = renderImage(c);
        switch(c.getSide()){
            case MEXICAN:
                Platform.runLater(
                        () -> {
                            pane.getChildren().remove(mexicans.get(c.getId()));
                            mexicans.replace(c.getId(),j);
                            pane.getChildren().add(j);
                        }
                );
                break;
            case TRUMP:
                Platform.runLater(
                        () -> {
                            pane.getChildren().remove(trumps.get(c.getId()));
                            trumps.replace(c.getId(),j);
                            pane.getChildren().add(j);
                        }
                );
                break;
        }
    }

    /** Update Games - incoming characters from server
	 * can either be updated characters (e.g. Trump moved 10 px)
	 * or new Characters from clicking on one of the lanes */
    void setPos(String in) {
        Character c = Character.parse(in);
        if(c.getSide()== CommandList.MEXICAN){
            if(mexicans.containsKey(c.getId())){
                if(c.getHealth()<=0){
                    remove(c);
                } else {
                    String switching = switchAttackingState(c,mexicans.get(c.getId()));
                    if(switching == null){
                        draw(mexicans.get(c.getId()),c);
                    }
                    else if(switching .equals("fight")){
                        switchAnimation(c);
                    } else if(switching .equals("walk")){
                        switchAnimation(c);
                    }
                }
            } else{//new Character, just placed by player
                ImageView i = renderImage(c);
                draw(i,null);
                mexicans.put(c.getId(),i);
            }
        } else if(c.getSide()== CommandList.TRUMP){
            if(trumps.containsKey(c.getId())){
                if(c.getHealth()<=0){
                    remove(c);

                }else {
                    String switching = switchAttackingState(c, trumps.get(c.getId()));
                    if (switching == null) {
                        draw(trumps.get(c.getId()), c);
                    } else if (switching.equals("fight")) {
                        switchAnimation(c);
                    } else if (switching.equals("walk")) {
                        switchAnimation(c);
                    }
                }
            } else{//new Character, just placed by player
                ImageView i = renderImage(c);
                draw(i,null);
                trumps.put(c.getId(),i);
            }
        }
    }

    /** move ImageView forward or draw a new one if update isn't set */
    public void draw(ImageView i,Character update){
        Platform.runLater(
                () -> {
                    if(update!=null){
                        i.setX(update.getXPos());
                    } else{
                        pane.getChildren().add(i);
                    }
                    if(oldTacos!= null){
                        oldTacos.toFront();
                    }
                }
        );
    }

    /** remove ImageView from GUI, Dying Animation of the Characters */
    public void remove(Character c){
        CommandList type = c.getType();
        int id = c.getId();

        ImageView i;
        if(c.getSide()==CommandList.TRUMP){
            i = trumps.get(id);
        }else{
            i = mexicans.get(id);
        }

        Platform.runLater(
                () -> {
                    switch (type){
                        case BULL:
                            ImageView b = new ImageView(bullDyingImg);
                            b.setX(i.getX());
                            b.setY(i.getY());
                            pane.getChildren().remove(i);

                            pane.getChildren().add(b);
                            Animation animationBullDying = new SpriteAnimation(b,Duration.millis(1200),40,10,0,0,381,322);
                            animationBullDying.setCycleCount(1);
                            animationBullDying.play();
                            b.setViewport(new Rectangle2D(0,0,381,322));
                            b.setFitHeight(100);
                            b.setFitWidth(120);
                            trumps.remove(id);
                            animationBullDying.setOnFinished(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pane.getChildren().remove(b);
                                }
                            });
                            break;
                        case TRUMP:
                            AudioClip sound1 = playSound("sounds/trumpDead.wav");
                            sound1.play();
                            ImageView img = new ImageView(trumpDyingImg);
                            img.setX(i.getX());
                            img.setY(i.getY());
                            pane.getChildren().remove(i);

                            pane.getChildren().add(img);
                            Animation animationTrumpDie = new SpriteAnimation(img,Duration.millis(1200),40,8,0,0,464,456);
                            animationTrumpDie.setCycleCount(1);
                            animationTrumpDie.play();
                            img.setViewport(new Rectangle2D(0,0,464,456));
                            img.setFitHeight(130);
                            img.setFitWidth(100);
                            trumps.remove(id);
                            animationTrumpDie.setOnFinished(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pane.getChildren().remove(img);
                                }
                            });
                            break;
                        case DONKEY:
                            ImageView d = new ImageView(donkeyDyingImg);
                            d.setX(i.getX());
                            d.setY(i.getY());
                            pane.getChildren().remove(i);

                            pane.getChildren().add(d);
                            Animation animationDonkeyDying = new SpriteAnimation(d, Duration.millis(1200), 40,8,0,0,411,539);
                            animationDonkeyDying.setCycleCount(1);
                            animationDonkeyDying.play();
                            d.setViewport(new Rectangle2D(0,0,284,522));
                            d.setFitHeight(110);
                            d.setFitWidth(90);
                            mexicans.remove(id);
                            animationDonkeyDying.setOnFinished(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pane.getChildren().remove(d);
                                }
                            });
                            break;
                        case MEXICAN:
                            AudioClip sound2 = playSound("sounds/mexDead.wav");
                            sound2.play();
                            ImageView m = new ImageView(mexicanDyingImg);
                            m.setX(i.getX());
                            m.setY(i.getY());
                            pane.getChildren().remove(i);

                            pane.getChildren().add(m);
                            Animation animationMexDie = new SpriteAnimation(m, Duration.millis(1200), 40,14,0,0,284,522);
                            animationMexDie.setCycleCount(1);
                            animationMexDie.play();
                            m.setViewport(new Rectangle2D(0,0,284,522));
                            m.setFitHeight(90);
                            m.setFitWidth(70);
                            mexicans.remove(id);
                            animationMexDie.setOnFinished(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    pane.getChildren().remove(m);
                                }
                            });
                            break;
                        default:
                            throw new IllegalArgumentException("Cannot delete Character");
                    }
                }
        );
    }

    private ImageView renderImage(Character c){
        Image image = null;
        int width2D=0;
        int height2D=0;
        double width =0;
        double height = 0;
        double posX=0;
        double posY= 110*(c.getLane()-1)+40;
        int column=0;
        String url = "";
        int count = 0;

        if(c.getType()== CommandList.TRUMP){
            if (c.isAttacking()){
                posX = c.getXPos();
                image = trumpFightingImg;
                width2D = 495;
                height2D = 466;
                width = 130;
                height = 100;
                column = 4;
                count = 25;
                url = "sounds/trump.wav";

            } else {
                posX = c.getXPos();
                image = trumpImg;
                width2D = 629;
                height2D = 500;
                width = 130;
                height = 100;
                column = 3;
                count = 20;
                url ="sounds/trump.wav";
            }

        } else if(c.getType()==CommandList.MEXICAN){
            if (c.isAttacking()){
                posX = c.getXPos();
                image = mexicanFightingImg;
                width2D = 374;
                height2D = 544;
                width = 100;
                height = 100;
                column = 5;
                count = 21;
                url = "sounds/mexican.wav";
            } else {
                posX = c.getXPos();
                image = mexicanImg;
                width2D = 550;
                height2D = 762;
                width = 100;
                height = 100;
                column = 7;
                count = 20;
                url = "sounds/mexican.wav";
            }

        } else if(c.getType() == CommandList.BULL){
            if (c.isAttacking()){
                posX = c.getXPos();
                image = bullFightingImg;
                width2D = 460;
                height2D = 359;
                width = 120;
                height = 100;
                column = 4;
                count = 20;
                url = "sounds/trump.wav";
            } else {
                posX = c.getXPos();
                image = bullImg;
                width2D = 383;
                height2D = 323;
                width = 120;
                height = 100;
                column = 5;
                count = 25;
                url = "sounds/trump.wav";
            }

        } else if (c.getType() == CommandList.DONKEY){
            if (c.isAttacking()){
                posX = c.getXPos();
                image = donkeyFightingImg;
                width2D = 552;
                height2D = 583;
                width = 90;
                height = 110;
                count = 20;
                column = 7;
                url = "sounds/mexican.wav";
            } else {
                posX = c.getXPos();
                image = donkeyImg;
                width2D = 427;
                height2D = 563;
                width = 90;
                height = 110;
                column = 4;
                count = 20;
                url = "sounds/mexican.wav";

            }
        }

        AudioClip sound = playSound(url);
        sound.play();

        ImageView imageView = new ImageView(image);

        Animation animation = new SpriteAnimation(imageView, Duration.millis(900), count, column,0,0,width2D,height2D);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.play();

        imageView.setViewport(new Rectangle2D(0, 0, width2D, height2D));
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
        imageView.setX(posX);
        imageView.setY(posY);
        imageView.setId(Integer.toString(c.getId()));

        // to boost performance
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);

        return imageView;
    }


    public Timeline getTacos() {
        return tacos;
    }

    Timeline setTacos(){
        Image tacosImg = new Image(getClass().getResourceAsStream("/img/Taco.png"));
        Image coinImg = new Image(getClass().getResourceAsStream("/img/Cash.png"));
        CommandList side = ClientController.getClient().playingAs();

        tacos = new Timeline(new KeyFrame(Duration.seconds(10), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                ImageView imageView = new ImageView(tacosImg);
                imageView.setCache(true);
                imageView.setCacheHint(CacheHint.SPEED);
                imageView.setFitHeight(100);
                imageView.setFitWidth(150);
                imageView.setX(400);
                imageView.setY(50);
                if (side == CommandList.TRUMP){
                    imageView.setImage(coinImg);
                    imageView.setFitWidth(90);
                }
                oldTacos = imageView;

                TranslateTransition transition = new TranslateTransition();
                
                imageView.setOnMouseClicked(e ->{
                    //System.out.println("Tacos clicked");
                    AudioClip clip = playSound("sounds/bonusTacos.wav");
                    clip.setVolume(0.5);
                    clip.play();
                    pane.getChildren().remove(imageView);
                    transition.stop();
                    ClientController.getClient().sendGame(new Message(CommandList.RESOURCE));


                });
                transition.setDuration(Duration.seconds(5));
                transition.setNode(imageView);
                transition.setToX(random.nextInt(400));
                transition.setToY(random.nextInt(400));
                transition.setAutoReverse(true);
                transition.setCycleCount(2);
                transition.setOnFinished(e -> Platform.runLater(
						() -> {
							pane.getChildren().remove(imageView);
						}
				));
                transition.play();
                pane.getChildren().add(imageView);
                AudioClip clip = playSound("sounds/tacosOut2.wav");
                clip.setVolume(0.7);
                clip.play();
            }
        }));
        tacos.setCycleCount(Timeline.INDEFINITE);
        tacos.play();
        return tacos;
    }

    void setTacoC(int r){
        tacosCollected = r;
        Text text = new Text("Collected Tacos \n" + String.valueOf(tacosCollected));
        text.setStyle("-fx-font-size: 14; -fx-fill: #cb5217;");
        Platform.runLater(
                () -> {
                    money.getChildren().setAll(text);
                }
        );
    }

    /** set initial tacos or money, so player can start playing immediately */
    void initStartTacos(){
        Text text = new Text("Collected Tacos \n" + String.valueOf(tacosCollected));
        text.setStyle("-fx-font-size: 14; -fx-fill: #cb5217;");
        money.getChildren().add(text);
    }

    public Rectangle getWall() {
        return wall;
    }

    public Pane getPane() {
        return pane;
    }

    public void send(){
        String getText = messageTextField.getText();
        if(getText.equals("")){
            return;
        }
        ClientController.getClient().lobbyChat(getText);
        messageTextField.clear();
    }

    /** call the send method if user hit enter after typed in some text in the textField */
    public void hitEnter(){
        send();
    }

    /** Display messages on the TextFlow
     * @param clientName Sender of the message, can be taken from the client Controller
     * @param message the message the user sent */
    void setGameMessages(String clientName, String message){
        String show = message.replace("\\%", "%");
        Text text = new Text(show + "\n");
        if (show.startsWith(clientName)) {
            text.setStyle("-fx-font-size: 12; -fx-fill: #0b701c;");
        } else {
            text.setStyle("-fx-font-size: 12; -fx-fill: #2b6acc;");
        }
        Platform.runLater(
                () -> {
                    chatMessages.getChildren().add(text);
                }
        );
    }

    void setEndGameMessage(String message){
        Text text = new Text(message+ "\n");
        Platform.runLater(
                () -> {
                    if(pane.getScene().getWindow().isShowing()){
                        chatMessages.getChildren().add(text);
                    }

                }
        );
    }

    /** @param path of the sound*/
   AudioClip playSound(String path){
       URL url = GameControllerTrump.class.getClassLoader().getResource(path);
        AudioClip mediaPlayer = null;
        try {
            mediaPlayer = new AudioClip(url.toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            log.error("Something went wrong on playsound Method. cannot plays the sound");
        }
        return mediaPlayer;
    }

    void showWinnerDialog(CommandList commandList){
        Platform.runLater(
                () -> {
                    tacos.stop();

                    javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<ButtonType>();
                    dialog.initOwner(pane.getScene().getWindow());
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    if(commandList == CommandList.WINNER){
                        fxmlLoader.setLocation(getClass().getResource("/fxml/winDialog.fxml"));
                    }else if(commandList == CommandList.LOSER){
                        fxmlLoader.setLocation(getClass().getResource("/fxml/loseDialog.fxml"));
                    }

                    try {
                        dialog.getDialogPane().setContent(fxmlLoader.load());
                    } catch (IOException e) {
                        System.err.println("Couldn't load the dialog");
                        e.printStackTrace();
                        return;
                    }

                    dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

                    Optional<ButtonType> result = dialog.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        mexicans.clear();
                        trumps.clear();
                        pane.getScene().getWindow().hide();
                    }
                }
        );
    }

    public TextFlow getChatMessages() {
        return chatMessages;
    }

    /** Open default browser and go to our blog */
    public void link(){
        try {
            Desktop.getDesktop().browse(new URI("http://www.sand-unibas.blogspot.com"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
