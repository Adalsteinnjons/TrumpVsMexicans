package ch.unibas.dmi.cs108.sand.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

/** This is the controller of the login ch.unibas.dmi.cs108.sand.gui */
public class LoginController {

    @FXML
    private TextField textFieldName;
    @FXML
    private TextField textFieldIP;
    @FXML
    private CheckBox checkBoxConnectAs;
    @FXML
    private TextField textFieldPort;
    @FXML
    private Button connectBtn;
    @FXML
    private Label warning;

    /** Hide warning label */
    public void initialize(){
        warning.setVisible(false);
				setCompName();
    }

    /**
     * If a textfield is left empty the login button will be disabled.
     */
    @FXML
    public void handleKeyReleased(){
        String ip = textFieldIP.getText();
        String port = textFieldPort.getText();
        String name = textFieldName.getText();
        boolean disableButton = ip.trim().isEmpty() || port.trim().isEmpty() || name.trim().isEmpty();
        connectBtn.setDisable(disableButton);
    }
		
    /** connect button is pressed, if Checkbox is checked, call the Sever scene,
     *  otherwise client scene will be called
     *  User inputs are validated here.
     *  We pass validated user inputs to the corresponding controller*/
    public void connect() throws Exception {
        Parent root;
        if (checkBoxConnectAs.isSelected()) {
            if(textFieldIP.getText().equals("")){
                warning.setText("Server-Ip cannot be empty");
                warning.setVisible(true);
            }else if(textFieldPort.getText().equals("")){
                warning.setText("Port cannot be empty");
                warning.setVisible(true);
            }else {
                try {
                    //URL resource = (getClass().getResource("Server.fxml"));
                    int port = Integer.valueOf(textFieldPort.getText());
                    if(!portIsOpen(port)){
                        warning.setText("This port is not available");
                        warning.setVisible(true);
                        System.out.println("This port is not available / there is already a Server running here.");
                        return;
                    }

                    URL resource = ServerController.class.getResource("/fxml/Server.fxml");
                    //System.out.println(resource);
                    //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Server.fxml"));
                    FXMLLoader fxmlLoader = new FXMLLoader(resource);
                    root = fxmlLoader.load();
                    ServerController controller = fxmlLoader.<ServerController>getController();
                    controller.setPort(port);

                    Stage stage = new Stage();
                    stage.setTitle("Server");
                    stage.setScene(new Scene(root, 600, 400));
                    stage.setResizable(false);
                    stage.show();
                    // Hide this current window (if this is what you want)
                    connectBtn.getScene().getWindow().hide();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if(textFieldIP.getText().equals("")){
                warning.setText("Server-Ip cannot be empty");
                warning.setVisible(true);
            }else if(textFieldPort.getText().equals("")){
                warning.setText("Port cannot be empty");
                warning.setVisible(true);
            }else if(textFieldName.getText().equals("")){
                warning.setText("Username cannot be empty");
                warning.setVisible(true);
            }else {
                try {

                    String ip = textFieldIP.getText();
                    int port = Integer.valueOf(textFieldPort.getText());
                    if(!serverIsOpen(ip,port)){
                        warning.setText("There is no Server running on "+ip+": "+port);
                        warning.setVisible(true);
                        System.out.println("There is no Server running on "+ip+": "+port);
                        return;
                    }

                    URL resource = ClientController.class.getResource("/fxml/Client.fxml");
                    //System.out.println(resource);
                    FXMLLoader fxmlLoader = new FXMLLoader(resource);
                    root = fxmlLoader.load();
                    ClientController controller = fxmlLoader.<ClientController>getController();
                    controller.setPort(port);
                    // pass user inputs to the controller
                    controller.setIp(ip);
                    controller.setUsername(textFieldName.getText().replaceAll("([^'\\w\\s])",""));
                    Stage stage = new Stage();
                    stage.setTitle("Client-" + textFieldName.getText());
                    stage.setScene(new Scene(root, 880, 610));


                    stage.setX(100);
                    stage.setResizable(false);
                    stage.show();
                    // Hide this current window (if this is what you want)
                    connectBtn.getScene().getWindow().hide();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    boolean portIsOpen(int port){
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    boolean serverIsOpen(String host, int port){
        try{
            Socket mySocket = new Socket(host,port);
            mySocket.close();
            return true;
        } catch (IOException e){
            return false;
        }
    }

    public void nameHitEnter(){
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ipHitEnter(){
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
		
/** Get computername and suggest as nickname */
		public void setCompName() {
				try {
                    String name = System.getProperty("user.name");
                    textFieldName.textProperty().set(name);
				} catch (Exception e) {
                    System.err.println("Username not resolved");
                }
		}

}
