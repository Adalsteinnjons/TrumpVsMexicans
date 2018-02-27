package ch.unibas.dmi.cs108.sand.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

	private int port;
	private Socket mySocket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private boolean connected = false;
	private String userName = "anonymous";//default
	private String myHost = "localhost";//default
	private PingPong pingPong;
	private ArrayList<String> availableGames = new ArrayList<String>();
	private CommandList playAs = null;
	private static final Logger log = LogManager.getLogger(Client.class);

	/** call other constructor with default values */
	public Client() {
		this(9999 );
	}
	/** connect on localhost with given port */
	public Client(int port){
		this.port = port;
		connect(port);
		communicate(in,out);
		//writeToServer();
	}
	/** connect with host and port */
	public Client(String myHost, int port){
		this.myHost= myHost;
		connect(port);
		communicate(in,out);
		//writeToServer();
	}
	/** connect with host, port and username */
	public Client(String myHost,String userName, int port){
		this.port = port;
		this.myHost = myHost;
		this.userName = userName;
		System.out.println("Client "+userName);
		connect(port);
		communicate(in,out);
		log.info("A new Client is initialized");
	}

	/** @return returns what side you are playing */
	public CommandList playingAs(){
		return playAs;
	}

	/** get all the available Games (GameServers) to list in Dropdown-Menu */
	public ArrayList<String> getAvailableGames(){
		return availableGames;
	}

	/** establish connection (Socket) with Server
	 * set Streams "in" and "out"
	 * default port 9999
	 */
	public void connect(){
		connect(9999);
	}
	/** establish connection (Socket) with Server
	 * set Streams "in" and "out"
	 * @param port given by Server
	 */
	private boolean connect(int port){
		boolean socketFine;
		boolean inOutFine;
		try{
			mySocket = new Socket(myHost,port);
			connected = true;
			socketFine = true;
		} catch (IOException e){
			log.error("Something went wrong when trying to connect to the server");
			socketFine = false;
		}

		try{
			out = new PrintWriter(mySocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			inOutFine = true;
		}
		catch(IOException e){
			System.err.println("Error getting In- and OutputStream of Server");
			log.error("Something went wrong when initializing writer and reader");
			inOutFine = false;
		}
		if(socketFine && inOutFine){
			return true;
		}else {
			return false;
		}
	}

	/** Listen to the incoming Messages and handle them
	 * Definition of protocol:
	 * see CommandList.java
	 *
	 * when loosing connection with the server: for now just shut down Client
	 * */
	private void communicate(BufferedReader in, PrintWriter out) {
		Thread communicate = new Thread() {
			public void run() {
				String temp;
				Message m;
				listen:
				while (true) {
					m = null;

					try {
						temp = in.readLine();
						m = Message.parse(temp);
					} catch (SocketException e) {
						System.err.println("Lost connection to Server");
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					//execute Commands in Message Object
					//System.out.println(m);
					switch (m.getCommand()) {
						case IDENTIFY:
							out.println(new Message(CommandList.IDENTIFYING, userName));
							break;

						case WELCOME:
							display(CommandList.WELCOME, m.getMessage());
							startPingPong();
							break;

						case PING:
							out.println(new Message(CommandList.PONG));
							//System.out.println("PING");
							break;

						case PONG:
							pingPong.pong();
							//System.out.println("PONG");
							break;

						case MESSAGE:
							display(m.getCommand(), m.getMessage());
							break;

						case LOBBY_CHAT:
							displayLobbyChat(m.getMessage());
							break;

						case NAME_CHANGED:
							break;

						case AVAILABLE_GAMES:
							String avGames = m.getMessage();
							//System.out.println("avGames:"+avGames);
							if(avGames==null|| avGames.equals("")){//sollte null sein wenn nichts mitgeschickt
								availableGames.clear();
								showAvailableGames(availableGames);
							}
							else{
								Pattern pattern = Pattern.compile("([a-zA-Z'0-9\\s]+),");
								Matcher matcher = pattern.matcher(avGames);
								ArrayList<String> gamesTemp = new ArrayList<String>();
								while (matcher.find()) {
									String game = matcher.group(1);
									gamesTemp.add(game);
								}
								availableGames = gamesTemp;
								//System.out.println("C: available Games received: "+gamesTemp);
								//Join durch methode "enterGameServer"
								//enterGameServer(availableGames.get(0),true);
								showAvailableGames(availableGames);
							}
							break;

						case GAME_READY:
							//System.out.println("You're in CASE GameReady");
							//System.out.println(m);
							try {
								playAs = CommandList.valueOf(m.getMessage());

							} catch (IllegalArgumentException e){
								System.err.println("Couldn't parse "+m);
								e.printStackTrace();
								return;
							}
							letsPlayGame(playAs);

							break;

						case CHARACTER:
							String toParse = m.getMessage();
							sendCharacter(toParse);
							break;

						case RESOURCE:
							//m.getMessage() contains new status of the resource counter, not how many more you got now
							int resource = Integer.parseInt(m.getMessage());
							//System.out.println("client got resource "+resource);
							setTacoCounter(resource);
							break;

						case GOODBYE:
							logout();
							break listen;//break while-true-loop

						case SHOW_CLIENTS:
							String clients = m.getMessage();
							ArrayList<User> users = User.parseUsers(clients);
							showAvailableClients(users);
							break;

						case END_GAME:
							endGameByClient(m.getMessage());
							break;

						case WINNER:
							displayWinnerAlert();
							System.out.println("WINNER");
							break;

						case LOSER:
							displayLoserAlert();
							System.out.println("LOOSER");
							break;

						case HIGHSCORE:
							writeHighscore(m.getMessage());
							break;

					}
				}
			}
		};
		communicate.start();
	}

	/** send a chat-message to the server
	 * @param message chat-message as Message-object*/
	public void writeToServer(Message message){
		out.println(message.toString());

	}
	/** is overridden in ClientController */
	public void displayLobbyChat(String message){
		System.out.println(message);
	}

	/** Send messages to the other players in the lobby */
	public void lobbyChat(String in){
		out.println(new Message(CommandList.LOBBY_CHAT,in));
	}

	/** tell the Server, that you want to change your name */
	public void requestNewName(String newName){
		out.println(new Message(CommandList.NEW_NAME,newName).toString());
	}

	/** ask the Server, to create a new GameServer */
	public void requestNewGameServer(String gameName){
		out.println(new Message(CommandList.CREATE_GAMESERVER,gameName));
		//System.out.println("C requesting Game Server");
	}

	/** tell the Server that you are leaving */
	public void requestLogout(){
		out.println(CommandList.LOGOUT);
	}

	/** Send all changes (newly placed Characters etc.) to the Server */
	public void sendGame(Message updates){
		out.println(updates);
	}

	/** request to enter a game */
	public void enterGameServer(String game,boolean asMexican){
		out.println(new Message(CommandList.JOIN_GAME,game+"="+asMexican));
	}

	/** tell the Server that you are ready to start the Game
	 * this function is called when clicking on Button "start Game" */
	public void startGame(boolean asMexican){
		out.println(new Message(CommandList.GAME_READY,String.valueOf(asMexican)));
	}

	/** this function is called when you are entering a game-lobby
	 * ask the Server to add you to this lobby */
	public void enterGameLobby(String gameName){
		out.println(new Message(CommandList.JOIN_LOBBY,String.valueOf(gameName)));
	}

	/** setup the PingPong instance
	 * Define what needs to happen when not receiving a PING during TIMEOUT */
	private void startPingPong(){
		pingPong = new PingPong(out) {
			@Override
			void noPong() {
				display(CommandList.MESSAGE,"Lost connection with Server");
				close();//close while loops in PingPong
				logoutWithoutExit();
				display(CommandList.MESSAGE, "reconnecting...");

				while(!connected){
					connected = connect(port);
					if(connected){
						display(CommandList.MESSAGE, "You are reconnected to the server");
						communicate(in,out);
						break; // We connected! Exit the loop.
					}else{
						try {
							TimeUnit.SECONDS.sleep(5);
						} catch(InterruptedException ie) {
							log.error("Thread Interrupted in startPingPong method");
						}
					}
				}
			}
		};
		pingPong.start();

	}

	/** overridden by annonymous class in ClientController
	 * define how to display Message-Objects */
	public void display(CommandList command, String msg){
		switch(command) {
			case WELCOME:
				//System.out.println("WELCOMED BY SERVER");
				break;
			case MESSAGE:
				//System.out.println("C: " + msg);
				break;
		}
	}

	/** this method is overridden in ClientController
	 * define what needs to happen when a Client closes the Game window before Game is finished */
	public void endGameByClient(String message){
		System.out.println(message);
	}

	/** display the available Games received from the Server
	 * overridden by ClientController */
	public void showAvailableGames(ArrayList<String> availableGames){
		System.out.println(availableGames);
	}

	/** overridden by the GUI
	 * update the Character position in the Game */
	public void sendCharacter(String name){
	}

	/**overridden by the GUI
	 * update the GUI with the new Resource value */
	public void setTacoCounter(int a){
	}

	/** overridden by the GUI
	 * open the Game window and start the Game */
	public void letsPlayGame(CommandList commandList){
		System.out.println("start a game");
	}

	/** Close connections, stop PingPong
	 * Needs to stay private. call requestLogout() from outside this class
	 * is triggered from incoming CommandList GOODBYE
	 * */
	private void logout(){
		try {
			pingPong.close();//close while-loops in PingPong
			in.close();
			out.close();
			mySocket.close();
			connected = false;
			log.info("Exit the application");
			System.exit(1);//closes ClientController-Window
		} catch (IOException e) {
			e.printStackTrace();
					}
	}

	private void logoutWithoutExit(){
		try {
			pingPong.close();//close while-loops in PingPong
			in.close();
			out.close();
			mySocket.close();
			connected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** This method is overriden by the ClientController
	 *  It updates the ListView in Chats with all available clients and their ids*/
	public void showAvailableClients(ArrayList<User> users){
		System.out.println("Show available Clients");
	}

	/**overridden by the GUI
	 * display that you won the game*/
	public void displayWinnerAlert(){
		System.out.println("Winner");
	}
	/**overridden by the GUI
	 * display that you lost the game*/
	public void displayLoserAlert(){
		System.out.println("Loser");
	}

	/** write highscore and other information (like how long the game was played) persistently in a file
	 * if the highscore-file does not exist, it will be created (in the same folder where the JAR is) */
	private void writeHighscore(String input){
		File statFile = new File(System.getProperty("user.dir")+"/stats.properties");
		if(!statFile.exists()){
			try {
				statFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				log.error("Something went wrong when trying to write highscore information");
			}
		}

		//File statFile;
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			//statFile = new File(statUrl.toURI());
			fw = new FileWriter(statFile,true);//true damit appenden
			bw = new BufferedWriter(fw);
			bw.write("\n"+input);

		} /*catch (URISyntaxException e) {
            e.printStackTrace();
        }*/ catch(IOException e){
			System.err.println("Error writing Statistics file");
			e.printStackTrace();
		}finally {
			try {
				if (bw != null){
					bw.close();
				}
				if (fw != null){
					fw.close();
				}
			} catch (IOException f) {
				f.printStackTrace();
			}
		}
	}
}