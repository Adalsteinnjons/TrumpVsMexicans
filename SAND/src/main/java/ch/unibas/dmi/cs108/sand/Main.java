package ch.unibas.dmi.cs108.sand;

import ch.unibas.dmi.cs108.sand.gui.ClientController;
import ch.unibas.dmi.cs108.sand.gui.ServerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/** Welcome to SAN;
 *  This application is created by Syarif Hidayatullah, Nils Hansen and Adalstein Johnson
 *  All copyright reserved.
 * Sounds used in this game is taken from, amongst others, http://www.noiseforfun.com/ produced by Filippo Vicarelli*/

public class Main extends Application {
    private static String[] arguments;
    private static final Logger log = LogManager.getLogger(Main.class);

    /** Start the application and show the login window. Users can either start
     *  the application as server or as client depends on the arguments. If there are no arguments passed,
     *  Logging gui will be showing up where users can input port number, ip address and username. */
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        if(arguments.length != 0){
            if(arguments[0].equals("server")){
                log.info("Start application as a client using the commandline");
                URL resource = ServerController.class.getResource("/fxml/Server.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(resource);
                root = fxmlLoader.load();
                ServerController serverController = fxmlLoader.<ServerController>getController();
                if(arguments.length > 1){
                    serverController.setPort(Integer.valueOf(arguments[1]));
                }else {
                    serverController.setPort(9999);
                    System.out.println("Start server with default port number (9999)");
                }
                Stage stage = new Stage();
                stage.setTitle("Server");
                stage.setScene(new Scene(root, 600, 400));
                stage.setResizable(false);
                stage.show();
            }else if(arguments[0].equals("client")){
                log.info("Start application as a client using the commandline");
                URL resource = ClientController.class.getResource("/fxml/Client.fxml");
                FXMLLoader fxmlLoader = new FXMLLoader(resource);
                root = fxmlLoader.load();
                ClientController controller = fxmlLoader.<ClientController>getController();

                String username;
                if(arguments.length==3 && arguments[2] != null){
                    username = arguments[2];
                } else{
                    username = "anonymous";
                    log.info("User did not input a username, default username is used");
                }
                String host;
                int port;
                if(arguments.length == 1){
                    System.out.println("Host and port number are unknown. Run client with default host(localhost)" +
                            "and port number (9999)");
                    log.warn("default host and port number");
                    host = "localhost";
                    port = 9999;
                }else {
                    String[] hostAndPort = arguments[1].split(":");
                    host = hostAndPort[0];
                    port = Integer.parseInt(hostAndPort[1]);
                }

                controller.setPort(port);
                controller.setIp(host);
                controller.setUsername(username);
                Stage stage = new Stage();
                stage.setTitle("Client-" + username);
                stage.setScene(new Scene(root, 880, 610));


                stage.setX(100);
                stage.setResizable(false);
                stage.show();
            }

        }else{
            primaryStage.setTitle("Welcome to SAND");
            primaryStage.setScene(new Scene(root, 400, 275));
            primaryStage.setResizable(false);
            primaryStage.show();
            log.info("Start Application without arguments");
        }


    }

    public static void main(String[] args) {
        arguments = args;

        launch(args);
    }

    /** Stop method is called if the 'X' button on the chat window is pressed.
     *  In our case stop method will close both client socket and gameServer if exist.
     *  The application will be shut down using system.exit method. */
    @Override
    public void stop() throws Exception {
        if (ClientController.getClient() != null){
            ClientController.getClient().requestLogout();
        }else if (ServerController.getMyServer() != null) {
            try {
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}