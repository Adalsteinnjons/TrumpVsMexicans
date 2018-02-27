package ch.unibas.dmi.cs108.sand.network;

import java.util.HashSet;

/** Definition of protocol
 * IDENTIFY: Server asks Client to send userName, so:
 * IDENTIFY
 *
 * IDENTIFYING: Client submits his username, e.g.
 * IDENTIFYING%mein erster Username
 *
 * WELCOME: Server sends confirmation that Client has been added to Server and his id, so:
 * WELCOME%1
 *
 * MESSAGE: message being sent from Client to server (so that the Server can broadcast it) or from server to all clients, e.g.
 * MESSAGE%Hello Jack, how are you?
 *
 * CREATE_GAMESERVER: the Client requests the Server to open a new GameServer, so:
 * CREATE_GAMESERVER
 *
 * AVAILABLE_GAMES: Server broadcasts all available games to the Clients; e.g.
 * AVAILABLE_GAMES%Game1,Game2,
 * allowed as name are letters, numbers and whitespace
 *
 * SHOW_CLIENTS: the Server broadcasts a list of all Clients
 * SHOW_CLIENTS%name:Fred,id:1;name:Peter Miller,id:2;
 *
 * JOIN_GAME: the Client requests to enter a GameServer, e.g.
 * JOIN_GAME%gameServer name
 *
 * JOIN_LOBBY: the Client requests to enter a GameLobby, e.g.
 * JOIN_LOBBY%lobbyName with spaces
 *
 * LOBBY_CHAT: like the MESSAGE-command, but these messages are only broadcasted within a lobby
 * LOBBY_CHAT%my message
 *
 * WHISPER_CHAT: a Client sends a Message to just one other Client, e.g. Client with id 3 writes to Client with id 2
 * WHISPER_CHAT%3$Hello how are you$2
 *
 * CHARACTER: a parsed Characater, e.g.
 * id:1 stands for his id (is set by a counter. First Trump will have id 1, second will have id 2, first Mexican will have id 1, second will have id 2 etc.
 * type: type of Character. can bei one of those: TRUMP/MEXICAN/BULL/DONKEY
 * xPos: a double value of the xPosition with dots, not commas, so e.g. 23.4, not 23,4
 * lane: a int, what lane on the Character was put, e.g. 2. At the moment, this value needs to be between 1 and 6
 * health: a int, the value of the Characters health, decreases when being attacked
 * damage: a int, the value of the Characters damage (how much force he has when attacking). This number is subtracted from the other Character's health value (from the one that this Character is attacking)
 * attacking: either true or false. When a Character is in attacking mode, it is true, else it will be false
 * CHARACTER%id:1,type:TRUMP,xPos:150.0,lane:2,health:20,damage:5,attacking:false
 *
 * GAME_READY: GameServer tells its Clients that everyone has joined and Game can be started, and he tells the Client what side he will be playing (side MEXICAN or side TRUMP), e.g.
 * GAME_READY%MEXICAN
 *
 * MEXICAN: the Character "Mexican". this command is never in the command-part of a Message-object, but always in the message-part of a Message object. e.g. in a parsed Character or as part of the GAME_READY-message
 *
 * TRUMP: the Character "Trump". see MEXICAN
 *
 * BULL: the Character "Bull". see MEXICAN
 * BULL cannot be part of the GAME_READY-message-object
 * BULL plays on the side TRUMP
 *
 * DONKEY: the Character "Donkey". see MEXICAN
 * DONKEY cannot be part of the GAME_READY-message-object
 * DONKEY plays on the side MEXICAN
 *
 * WINNER: the Server tells a Client that he won the game, so:
 * WINNER
 *
 * LOSER: the Server tells a Client that he lost the game, so:
 * LOSER
 *
 * HIGHSCORE: server sends highscore message to Clients (which player won in what time), the Client writes this message into the highscore file
 * HIGHSCORE%winnerName%endTime%timeNow
 * e.g.
 * HIGHSCORE%John%44866%1495010193909
 *
 * RESOURCE: when a Client collects a resource, he sends RESOURCE to the Server (without a value). The server knows how much to add to a Client's resource value
 * the Server sends back the total amount of resources he owns.
 * RESOURCE%30
 *
 * NEW_NAME: Client requests new name, e.g.
 * NEW_NAME%my new Client-Name
 *
 * NAME_CHANGED: the Server confirm that your username did change, so:
 * NAME_CHANGED
 *
 * LOGOUT: Client tells Server that he wants to leave, so:
 * LOGOUT
 *
 * GOODBYE: the Server tells the Client to logout and close connection, so:
 * GOODBYE
 *
 * PING: Server asks, if Client is still here, or vice-versa, so:
 * PING
 *
 * PONG: after the Client/Server received a PING, he will send back a PONG, so:
 * PONG
 *
 * */
public enum CommandList {

    IDENTIFY("Server invites Client to submit his username"),
    IDENTIFYING("Client submits his username"),
    WELCOME("Server confirms that Client has been added"),
    MESSAGE("Messages sent between Server and Clients"),

    CREATE_GAMESERVER("Client asks Server to create a new gameServer"),
    AVAILABLE_GAMES("Server sends an available Game to the Clients"),
    SHOW_CLIENTS("Server sends all registered clients"),

    JOIN_GAME("Client requests to enter a certain game"),
    JOIN_LOBBY("Enter the Lobby"),
    LEAVE_LOBBY("Clients tells Server that he wants to leave a lobby"),
    LOBBY_CHAT("Chatting in the Lobby"),
    WHISPER_CHAT("Whisper without playing a game"),
    CHARACTER("Character with its attributes or Array of Characters (JSON)"),
    GAME_READY("GameServer tells its Clients that everyone has joined and Game can be started"),

    //Characters on lane
    MEXICAN("play as Mexican"),
    TRUMP("play as Trump"),
    BULL("stronger Character on the Trump side"),
    DONKEY("stronger Character on the Mexican side"),

    WINNER("inform Client, that he won"),
    LOSER("inform Client, that he lost"),
    HIGHSCORE("server sends highscore to Clients"),
    RESOURCE("When the Client gathers a resource(coin/taco), RESOURCE is sent to the server"),

    NEW_NAME("Client requests new name"),
    NAME_CHANGED("Confirm that Clientname has changed"),
    END_GAME("Client ends game by clicking x button or connection is broken"),
    LOGOUT("Client wants to leave"),
    GOODBYE("Server confirms that he is logging out Client"),
    PING("ask for confirmation, if C/S is still here"),
    PONG("send confirmation, that you are still here");



    private final String description;
    private static HashSet<String> all;

    static{
        all = new HashSet<String>();
        CommandList[] list = CommandList.values();
        for(CommandList c:list){
            all.add(c.toString());
        }
    }

    CommandList(String description) {
        this.description = description;
    }
    public static boolean isCommandList(String in){
        return all.contains(in);
    }

    public String getDescription(){
        return description;
    }

}