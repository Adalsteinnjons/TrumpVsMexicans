package ch.unibas.dmi.cs108.sand.logic;

import ch.unibas.dmi.cs108.sand.network.CommandList;
import ch.unibas.dmi.cs108.sand.network.Message;
import ch.unibas.dmi.cs108.sand.network.ServerHandler;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer extends Thread{

	private ServerHandler chatServer;
	private final int TIMEOUT = 100;
	private String gameName;
	private boolean isFull = false;
	private boolean ongoing = false;
	private boolean finished = false;
	private Thread moveAllThread;
	private PrintWriter mexican = null;
	private PrintWriter trump = null;
	private String mexName="";
	private String trumpName="";
	private PrintWriter player1;
	private PrintWriter player2;
	private String player1Name;
	private String player2Name;
	private int player1Id;
	private int player2Id;
	private long startTime;
	private long endTime;//how long game has been played at end
	private int resourceCountTrump = 20;
	private int resourceCountMexican = 20;
	static ConcurrentHashMap<Integer,Lane> lanes = new ConcurrentHashMap<>();
	private final int NUMBER_OF_LANES = 6;


	/** GameServer needs the "in" and "out" from the ChatServer
	 * create 6 instances of Lane() when starting the GameServer */
	public GameServer(ServerHandler chatServer, String gameName){
		this.chatServer = chatServer;
		this.gameName = gameName;
		startTime = System.currentTimeMillis();
		//create 6 new lanes
		for(int i=1;i<=NUMBER_OF_LANES;i++){
			Lane lane = new Lane(i);
			lanes.put(i,lane);
		}
	}

	/** run the moveAll() method as a Thread */
	public void run() {
		moveAllThread = new Thread(this::moveAll);
		moveAllThread.start();
	}


	public long getStartTime(){
		return startTime;
	}

	public String getGameName(){
		return gameName;
	}
	/** returns if all the slots of the GameServer are already taken or if there is room for another player */
	public boolean isFull(){
		return isFull;
	}
	/** if ongoing is true, the game is being played and not finished yet */
	public boolean isOngoing(){
		return ongoing;
	}
	/** check if the game has been finished yet */
	public boolean isFinished(){
		return finished;
	}

	/** a client can join a Game here
	 * If I want to play as Mexican, but the other player already joined as Mexican, I will play as Trump
	 * @param playAs what side you want to play, either CommandList.MEXICAN or CommandList.TRUMP
	 * */
	public CommandList join(PrintWriter out, String name, CommandList playAs){
		CommandList ret = null;
		if(playAs == CommandList.MEXICAN){
			if(mexican==null){
				mexican = out;
				mexName = name;
				ret = CommandList.MEXICAN;
				System.out.println("Client joined as mex");
			} else{
				trump = out;
				trumpName = name;
				ret = CommandList.TRUMP;
				System.out.println("Client joined as trump");
			}

		} else if(playAs == CommandList.TRUMP){
			if(trump==null){
				trump = out;
				trumpName = name;
				ret = CommandList.TRUMP;
				System.out.println("Client joined as trump");
			} else{
				mexican = out;
				mexName = name;
				ret = CommandList.MEXICAN;
				System.out.println("Client joined as mex");
			}
		}
		if(mexican !=null && trump!=null){
			mexican.println(new Message(CommandList.GAME_READY,CommandList.MEXICAN.toString()));
			trump.println(new Message(CommandList.GAME_READY,CommandList.TRUMP.toString()));
			isFull = true;
			ongoing = true;
			run();//call the run method to start the moveAll()-Thread
		}
		return ret;
	}

	/** Enter Game Lobby
	 * @param player whoever enters the game lobby first, will be the player1 */
	public void enterGameLobby(PrintWriter player,String name, int id){
		if(player1 == null && player2 == null){
			player1 = player;
			player1Name = name;
			player1Id = id;
			//player 2 is not here yet
			Thread writeToLobby = new Thread(){

				public void run() {
					try {
						sleep(900);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					player1.println(new Message(CommandList.LOBBY_CHAT, "Waiting for other user to join the lobby..."));
				}
			};
			writeToLobby.start();
		}else if(player1 != null){
			player2 = player;
			player2Name = name;
			player2Id = id;
			if(player1!=null){
				player1.println(new Message(CommandList.LOBBY_CHAT,name+" has joined the Lobby"));
			}
			isFull = true;//Lobby is full

			Thread writeToLobby = new Thread(){

				public void run() {
					try {
						sleep(900);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					player2.println(new Message(CommandList.LOBBY_CHAT,player1Name+" is in the Lobby"));
				}
			};
			writeToLobby.start();

		}
	}

	/** leave the lobby (when closing the lobbby-window) */
	public void leaveLobby(int id){
		if(id == player1Id){
			if (player2 != null) {
				player2.println(new Message(CommandList.LOBBY_CHAT,player1Name+" left the lobby"));
				player1 = null;
				player1Name = null;
				player1Id = 0;
				isFull = false;
			} else{
				chatServer.removeGameServer();
			}
		} else if(id == player2Id){
			if (player1 != null) {
				player1.println(new Message(CommandList.LOBBY_CHAT,player2Name+" left the lobby"));
				player2 = null;
				player2Name = null;
				player2Id = 0;
				isFull = false;
			} else{
				chatServer.removeGameServer();
			}
		}
	}

	/** depending on TIMEOUT, every x seconds the Characters on every lane are moved
	 * (or fight if they are in attacking mode)
	 * all values of all Characters are updated and broadcasted to clients */
	private void moveAll(){
		while(ongoing){
			for(int i=1;i<=NUMBER_OF_LANES;i++){
				ArrayList<Message> msgs = lanes.get(i).move();
				if(msgs.size()!=0){
					broadcastUpdates(msgs);
				}
				checkGameStatus(lanes.get(i));
			}

			try {
				sleep(TIMEOUT);
			} catch (InterruptedException e) {
				System.err.println("Error: GameServer-Thread was interrupted");
				e.printStackTrace();
			}
		}
	}

	/** accepts the updates on the game from a Client (e.g. a newly set Character or a collected Resource) and updates the Server,
	 * sends back the new resource values to the Client */
	public void update(Message in,CommandList playingAs){
		if(finished){
			return;
		}
		else if(in.getCommand()==CommandList.RESOURCE){
			if(playingAs == CommandList.MEXICAN){
				resourceCountMexican += 20;
				mexican.println(new Message(CommandList.RESOURCE,resourceCountMexican));
			} else if(playingAs == CommandList.TRUMP){
				resourceCountTrump += 20;
				trump.println(new Message(CommandList.RESOURCE,resourceCountTrump));
			}
			return;
		}else{//will be a character
			if(in.getMessage()==null || in.getMessage().equals("")){
				//ignore empty Characters
				System.err.println("trying to update with character==null");
				return;
			}
		}

		Character newCharacter = Character.parse(in.getMessage());
		if(newCharacter==null){
			return;
		}
		//else:

		int lane = newCharacter.getLane();
		if(newCharacter.getSide()== CommandList.MEXICAN){
			Integer a = lanes.get(lane).addCharacter(newCharacter,resourceCountMexican);
			if(a!=null){
				resourceCountMexican = a;
				mexican.println(new Message(CommandList.RESOURCE,resourceCountMexican));
			}
		}
		else if(newCharacter.getSide()==CommandList.TRUMP){
			Integer a = lanes.get(lane).addCharacter(newCharacter,resourceCountTrump);
			if(a!=null){
				resourceCountTrump = a;
				trump.println(new Message(CommandList.RESOURCE,resourceCountTrump));
			}//else: Character was not put on lane because not enough money
		}
	}

	/** send updated game to the players
	 * @param update contains all the Message-Objects to be sent (Characters with new values) */
	private void broadcastUpdates(ArrayList<Message> update){
		if(mexican!=null){
			for(Message m:update){
				mexican.println(m);
			}
		}

		if(trump!=null){
			for(Message m:update){
				trump.println(m);
			}
		}
	}

	/** check if Game has been finished
	 * if a Character walked over the border, the Game is finished and the Clients are informed if they either won or lost */
	private void checkGameStatus(Lane l){
		if(l.wonGame()==null){
			return;
		}

		PrintWriter winner;
		PrintWriter loser;
		//CommandList winner=null;
		String winnerName = "";
		String highscoreMessage="";

		if(l.wonGame() == CommandList.TRUMP){
			winner = trump;
			loser = mexican;
			winnerName = trumpName;
		} else{ //else if(l.wonGame() == CommandList.MEXICAN){
			winner = mexican;
			loser = trump;
			winnerName =mexName;
		}

		endTime = System.currentTimeMillis()-startTime;
		highscoreMessage = winnerName+"%"+endTime+"%"+System.currentTimeMillis();
		trump.println(new Message(CommandList.HIGHSCORE,highscoreMessage));
		mexican.println(new Message(CommandList.HIGHSCORE,highscoreMessage));
		winner.println(new Message(CommandList.WINNER));
		loser.println(new Message(CommandList.LOSER));

		ongoing= false;
		finished = true;

		System.out.println(winnerName + " won game");
		chatServer.removeGameServer();
	}

	/** Whisper-Chat just within the GameServer */
	public void lobbyChat(String clientName, Message in){
		if(player1!=null){
			player1.println(new Message(CommandList.LOBBY_CHAT,clientName+": "+in.getMessage()));
		}
		if(player2!=null){
			player2.println(new Message(CommandList.LOBBY_CHAT,clientName+": "+in.getMessage()));
		}
	}

	/** close a Game, before someone won (by closing window while game is ongoing) */
	public void endGameByClient(String name){
		System.out.println(name+" closed game before end");
		ongoing= false;
		finished = true;
		if(player1!=null){
			player1.println(new Message(CommandList.END_GAME,name+" has left the game"));
		}
		if(player2!=null){
			player2.println(new Message(CommandList.END_GAME,name+" has left the game"));
		}
	}
}