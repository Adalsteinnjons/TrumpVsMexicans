package ch.unibas.dmi.cs108.sand.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Created by Arkad on 4/19/2017.
 */
public class WhisperDialogController {
    @FXML
    private TextField userId;

    public void initialize(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                userId.requestFocus();
            }
        });
    }

    int getUserId(){
        String id = userId.getText();
        try {
            int intValue = Integer.parseInt(id);
        }catch (NumberFormatException e) {
            System.out.println("Input is not a valid integer");
            return -1;
        }
        return Integer.valueOf(id);
    }
}
