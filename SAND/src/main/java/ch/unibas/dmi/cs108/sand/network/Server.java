package ch.unibas.dmi.cs108.sand.network;
import ch.unibas.dmi.cs108.sand.logic.GameServer;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;

public class Server{
	private ServerSocket server = null;
	private ServerHandler serverHandler = null;

	static HashMap<Integer,String> clientNames = new HashMap<>();
	static HashMap<Integer,PrintWriter> broadcast = new HashMap<Integer,PrintWriter>();
	static HashSet<GameServer> games= new HashSet<GameServer>();
	static int gameCounter = 0;
	private int id=1;
	private boolean running=true;

	/** call other constructor with default values */
	public Server(){
		this(9999);
	}

	/** create ServerSocket
	 * listening-loop needs to be started separately by start()
	 * @param port what port to listen on*/
	protected Server(int port){
		try {
			server = new ServerSocket(port);
		} catch(IOException e){
			System.err.println("Couldn't open Server");
			e.printStackTrace();
		}
	}

	/** Start Server
	 * listen to Clients trying to connect to the Server
	 * (outside Constructor so that reference in ServerHandler works)
	 * create a new ServerHandler for every Client that connects with the Server
	 */
	public void start(){
		//(new Thread(){
		//public void run(){
		while(running){
			try {
				serverHandler = new ServerHandler(server.accept(),this,id);//listen to Clients trying to connect to the Server
				serverHandler.start();
				id++;
			} catch (IOException e) {
				System.err.println("Couldn't add Client to Server");
				e.printStackTrace();
			}
		}
		//}
		//}).start();
	}
	public static void main(String[] args) {
		new Server(Integer.parseInt(args[0]));
	}

	/** Overridden by the ServerController
	 * define how to Display messages*/
	public void display(String in){
		System.out.println(in);
	}

	public HashSet<GameServer> getOpenGames(){
		HashSet<GameServer> openGames = new HashSet<GameServer>();
		for(GameServer g:games) {
			if(!g.isFull()){
				openGames.add(g);
			}
		}
		return openGames;
	}
	public HashSet<GameServer> getOngoingGames(){
		HashSet<GameServer> ongoingGames = new HashSet<GameServer>();
		for(GameServer g:games) {
			if(g.isOngoing()){
				ongoingGames.add(g);
			}
		}
		return ongoingGames;
	}
	public HashSet<GameServer> getFinishedGames(){
		HashSet<GameServer> finishedGames = new HashSet<GameServer>();
		for(GameServer g:games) {
			if(g.isFinished()){
				finishedGames.add(g);
			}
		}
		return finishedGames;
	}


	/** Close Server
	 * Send a GOODBYE to every connected Client
	 * closes Server Streams etc. */
	public void logout(){
		try {
			//TODO: for now: every Client shuts down when server is manually closed. This will probably not be meaningful in the future.
			running = false;
			for (PrintWriter myClient : broadcast.values()) {
				//tell every Client to logout
				myClient.println(CommandList.GOODBYE);
			}
			broadcast.clear();
			if(serverHandler!=null){
				serverHandler.close();
			}
			if(server!=null){
				server.close();
			}
			//System.exit(1);
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

}