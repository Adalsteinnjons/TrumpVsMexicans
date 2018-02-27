package ch.unibas.dmi.cs108.sand.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;

/**
 * Created by Syarif on 4/30/2017.
 */
public class NewLobbyController {
    @FXML
    private TextField lobbyName;
    private DialogPane pane;

    /** set focus on the textfield lobbyName */
    public void initialize(){
        lobbyName.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.replaceAll("([^'\\w\\s])","").equals("")){
                    pane.lookupButton(ButtonType.OK).setDisable(false);
                }else{
                    pane.lookupButton(ButtonType.OK).setDisable(true);
                }
            }
        });
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lobbyName.requestFocus();
            }
        });
    }

    /** getter
     * @return lobby name and delete all special characters except (" ' ")
     */
    String getLobbyName() {
        return lobbyName.getText().replaceAll("([^'\\w\\s])","");
    }

    public void setPane(DialogPane pane) {
        this.pane = pane;
    }
}
