package ch.unibas.dmi.cs108.sand.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Created by Syarif on 24.03.17.
 */
public class NewNameController {

    @FXML
    private TextField username;

    /** set focus on the textfield */
    public void initialize(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                username.requestFocus();
            }
        });
    }

    /** take the username from the TextField and return as String */
    String changeUsername(){
        return username.getText().replaceAll("([^'\\w\\s])","");
    }

}
