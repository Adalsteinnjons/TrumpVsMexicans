package ch.unibas.dmi.cs108.sand.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by PC on 5/19/2017.
 */
public class IntroController implements Initializable{
    @FXML
    private MediaView media;

    private static final String IntroUrl = "/Videos/intro.mp4";
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        mediaPlayer = new MediaPlayer(new Media(this.getClass().getResource(IntroUrl).toExternalForm()));

        mediaPlayer.setAutoPlay(true);

        media.setMediaPlayer(mediaPlayer);
    }
}
