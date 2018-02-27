package ch.unibas.dmi.cs108.sand.network;

import ch.unibas.dmi.cs108.sand.logic.GameServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.unibas.dmi.cs108.sand.network.Server.clientNames;

/** ServerHandler class that defines how to interact with clients
 * protocol: see CommandList.java
 * */
public class ServerHandler extends Thread{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;
    private boolean running = true;
    private GameServer myGameServer = null;
    private Server server;
    private int id;
    private CommandList playingAs;

    /** create a new ServerHandler
     * @param id the Server sets a new ip for every new ServerHandler. This is the ip of the Client. */
    ServerHandler(Socket socket, Server server, int id) {
        this.server = server;
        this.socket = socket;
        this.id = id;
    }

    /** setup connection with a Client
     * handle incoming Messages*/
    public void run () {
        try{
            out = new PrintWriter(socket.getOutputStream(),true);
            //in = new BufferedReader(socket.getInputStream());
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));


            //while (running) {
                // fordere Client auf, einen Username anzugeben
                out.println(CommandList.IDENTIFY);
                //System.out.println("S: IDENTIFY");

                Message temp = Message.parse(in.readLine());
                if(temp==null){
                    return;
                }
                if(temp.getCommand()== CommandList.IDENTIFYING){
                    clientName = temp.getMessage();
                }

                if (clientName == null) {
                    return;
                }

                // handle douplicate names wiht additional numbers
                synchronized (clientNames) {
                    if (clientName == null) {
                        return;
                    }
                    int c = 0;
                    while( clientNames.containsValue(clientName) ){
                        String name = clientName;
                        c += 1;
                        if (c != 1) {
                            name = name.substring(0, name.length()-1) + String.valueOf(c);
                        } else {
                            name += String.valueOf(c);
                        }
                        clientName = name;
                    }
                    clientNames.put(id,clientName);

                    //break;
                }
            //}

            // include into the broadcast of the Server
            out.println(new Message(CommandList.WELCOME,String.valueOf(id)));
            Server.broadcast.put(id,out);
            updateAvailableClients();
            server.display("Welcome, "+clientName);
            broadcastGames();

            PingPong pingPong = new PingPong(out) {
                @Override
                void noPong() {
                    server.display("Lost connection with "+clientName);
                    try{
                        removeClient(clientName);
                        close();//close while loops in PingPong
                    } catch (IOException e){
                        clientNames.remove(clientName);
                        Server.broadcast.remove(out);
                        try {
                            socket.close();
                        } catch (IOException f) {
                            f.printStackTrace();
                        }
                    }

                }
            };
            pingPong.start();

            while (running) {
                // listen to Clients and broadcast messages
                Message m = Message.parse(in.readLine());
                if (m == null) {
                    return;
                }
                //System.out.println(m);
                switch(m.getCommand()){
                    case MESSAGE:
                        String msg = m.getMessage();
                        for (PrintWriter myClient : Server.broadcast.values()) {
                            myClient.println(new Message(CommandList.MESSAGE,clientName + ": " + msg).toString());
                        }
                        break;

                    case NEW_NAME:
                        String newName = m.getMessage();
                        System.out.println("S: NEWNAME: "+clientName+" to "+newName);
                        String oldName = clientName;
                        updateClientName(newName);
                        for (PrintWriter myClient : Server.broadcast.values()) {//informiere alle Clients wenn jemand seinen Namen ändert
                            myClient.println(new Message(CommandList.MESSAGE," "+oldName+" changed his name to "+clientName).toString());
                        }
                        server.display(oldName+" changed his name to "+newName);
                        out.println(new Message(CommandList.NAME_CHANGED).toString());//confirm that the name was changed
                        break;

                    case PING:
                        out.println(new Message(CommandList.PONG).toString());
                        //System.out.println("PING");
                        break;

                    case PONG:
                        pingPong.pong();
                        //System.out.println("PONG");
                        break;

                    case CREATE_GAMESERVER:
                        Server.gameCounter++;
                        myGameServer = new GameServer(this,m.getMessage());//chatServer und Game-Name übergeben
                        myGameServer.start();
                        //myGameServer.startGame();
                        //myGameServer.join(out,Boolean.valueOf(m.getMessage()));//m.getMessage ist entweder true oder false
                        Server.games.add(myGameServer);
                        broadcastGames();
                        //System.out.println("created GameServer GameServer"+Server.gameCounter+", broadcasting games");
                        break;

                    case JOIN_LOBBY:
                        String lobbyName = m.getMessage();
                        for(GameServer g:Server.games){
                            if(g.getGameName().equals(lobbyName)){
                                //System.out.println(clientName+" joining "+g.getGameName());
                                g.enterGameLobby(out,clientName,id);
                                myGameServer = g;
                            }
                        }
                        broadcastGames();

                        break;

                    case LEAVE_LOBBY:
                        myGameServer.leaveLobby(id);
                        broadcastGames();
                        break;

                    case JOIN_GAME:
                        String gameToJoin= m.getMessage();
                        Pattern pattern = Pattern.compile("(.*)=(.*)");
                        Matcher matcher = pattern.matcher(gameToJoin);
                        String gameName="";
                        boolean asMexican = false;
                        playingAs = CommandList.TRUMP;
                        while (matcher.find()) {
                            gameName = matcher.group(1);
                            String asMex = matcher.group(2);
                            if(asMex=="true"){
                                asMexican = true;
                                playingAs = CommandList.MEXICAN;
                            }
                        }
                        for(GameServer g:Server.games){
                            if(g.getGameName().equals(gameName)){
                                //System.out.println(clientName+" joining "+g.getGameName());
                                playingAs = g.join(out,clientName,playingAs);
                                myGameServer = g;
                            }
                        }
                        broadcastGames();
                        break;

                    case LOBBY_CHAT:
                        myGameServer.lobbyChat(clientName,m);
                        break;

                    case GAME_READY:
                        boolean mex = Boolean.valueOf(m.getMessage());
                        if(mex){
                            playingAs = CommandList.MEXICAN;
                        } else{
                            playingAs = CommandList.TRUMP;
                        }
                        playingAs = myGameServer.join(out,clientName,playingAs);
                        break;

                    case END_GAME:
                        //call the method only, wenn the window is closed by Client
                        //if the window is close after finishing the game, the gameserver will be null
                        if(myGameServer!=null){
                            myGameServer.endGameByClient(m.getMessage());
                        }
                        break;

                    case WHISPER_CHAT:
                        String message = m.getMessage();
                        //System.out.println(message);
                        String[] messages = message.split("\\$");
                        PrintWriter sender = Server.broadcast.get(Integer.valueOf(messages[0]));
                        PrintWriter receiver = Server.broadcast.get(Integer.valueOf(messages[2]));

                        sender.println(new Message(CommandList.MESSAGE,clientName + ": " + messages[1]).toString());
                        receiver.println(new Message(CommandList.MESSAGE,clientName + ": " + messages[1]).toString());
                        break;

                    //the Server gets a Game-Update from a single Client with new Characters
                    case CHARACTER:
                        myGameServer.update(m,playingAs);
                        break;

                    //the Client has collected a resource
                    case RESOURCE:
                        myGameServer.update(m,playingAs);
                        break;

                    case LOGOUT:
                        removeClient(clientName);
                        pingPong.close();
                        break;
                }
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }
        // reset and close ServerHandler
        finally {
            if (clientName != null) {
                clientNames.remove(clientName);
            }
            if (out != null) {
                Server.broadcast.remove(out);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeClient(String clientName) throws IOException{
        server.display("Goodbye, "+clientName);
        synchronized (clientNames) {
            out.println(new Message(CommandList.GOODBYE).toString());
            running = false;
            clientNames.remove(id);
            Server.broadcast.remove(id);
            updateAvailableClients();
            socket.close();
        }
    }

    private void updateAvailableClients(){
        String availableClients = User.hashMapToString(Server.clientNames);
        for (PrintWriter myClient : Server.broadcast.values()) {
            myClient.println(new Message(CommandList.SHOW_CLIENTS, availableClients));
        }
    }

    /** change a client name in the hashtable */
    private void updateClientName(String newName){
        //System.out.println(clientNames);
        clientNames.remove(clientName);
        int c = 0;
        while( clientNames.containsValue(newName) ){
            c += 1;
            if (c != 1) {
                newName = newName.substring(0, newName.length()-1) + String.valueOf(c);
            } else {
                newName += String.valueOf(c);
            }
        }
        clientNames.put(id,newName);
        clientName = newName;
    }

    /** send all available GameServer that are not full to all Clients*/
    private void broadcastGames(){
        /*if(server.games.size()==0){
            return;
        }*/
        ArrayList<String> list = new ArrayList<String>();
        for(GameServer g:Server.games){
            if(!g.isFull()){
                if(!g.isFull()){//ignore/hide full games
                    list.add(g.getGameName());
                }
            }
        }
        String implode = "";
        for(String h:list){
            implode += h+",";
        }
        //System.out.println("broadcasting games: AVAILABLEGAMES%"+implode);
        for (PrintWriter myClient : Server.broadcast.values()) {
            myClient.println(new Message(CommandList.AVAILABLE_GAMES,implode));
        }
    }

    public void removeGameServer(){
        Server.games.remove(myGameServer);
        myGameServer = null;
    }

    /** close ServerHandler */
    void close(){
        running = false;
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public void setServer(Server server) {
        this.server = server;
    }
}