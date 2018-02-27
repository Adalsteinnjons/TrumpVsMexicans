package ch.unibas.dmi.cs108.sand.gui;

import ch.unibas.dmi.cs108.sand.network.Server;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.awt.Desktop;
import java.io.IOException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.Enumeration;

/**
 * Created by Nils, Syarif on 3/15/2017.
 * Server side of chat application
 * All messages are sent and receive as objects of Message class.
 */
public class ServerController {
    private int port;


    @FXML
    private TextArea textAreaMessages;
    private static Server myServer;


    /** create instance of Server and start Server */
    public void initialize(){
        (new Thread(){
            public void run(){
                textAreaMessages.appendText("You are logged in as a server.\n");
                textAreaMessages.appendText("Your Ip Address is " + getIpAddress()+"\n");
                textAreaMessages.appendText("Waiting for clients...\n");
                myServer = new Server(port){
                    public void display(String in){
                        textAreaMessages.appendText(in+"\n");
                    }
                };
                myServer.start();
            }
        }).start();

    }

    /** Button "close connection" */
    public void closeConnection(){
        myServer.logout();
        System.exit(1);
    }

    /** This method will automatically call the IP address of the host machine and convert it to String
     *  so we can display it on the Server text area. */
    public String getIpAddress(){
        String address;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    address = inetAddress.getHostAddress();
                    if (!inetAddress.isLoopbackAddress() && address.length() < 18) {
                        return address;
                    }
                }
            }
        } catch (SocketException ex) {
            String msg = ex.getMessage();
            //do nothing
        }
        return null;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static Server getMyServer(){
        return myServer;
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
