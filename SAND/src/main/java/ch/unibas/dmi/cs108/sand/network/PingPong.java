package ch.unibas.dmi.cs108.sand.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;

/** Both, Server and Client initialize PingPong to keep checking that the connection wasn't lost */
public abstract class PingPong extends Thread{
	private PrintWriter out;
	private boolean waitingForPong = false;
	private final int TIMEOUT = 2000;
	private boolean running=true;
	private static final Logger log = LogManager.getLogger(PingPong.class);

	PingPong(PrintWriter out){
		this.out = out;
	}

	/** start the pingPong method */
	public void run() {
		Thread a = new Thread(this::pingPong);
		a.start();
	}

	/** check every 2 sec (TIMEOUT) if I have to either send a PING or if I should have received a PONG */
	private void pingPong(){
		try {
			log.info("PingPong started");
			while (running) {
				if (!waitingForPong) {
					out.println(new Message(CommandList.PING).toString());
					//System.out.println("PING");
					waitingForPong = true;
				} else if (waitingForPong) {
					noPong();
					log.warn("lost connection");
				}
				sleep(TIMEOUT);
			}
		} catch (InterruptedException e){
			System.err.println("PingPong interrupted");
			log.error("Something went wrong while pingponging");
			noPong();
		}
	}

	/** When the Server/Clients receives a PONG, he needs to call this function */
	void pong(){
		(new Thread() {
			public void run() {
				waitingForPong = false;
				//System.out.println("PONG");
			}
		}).start();
	}

	/** define (in anonymous class) what happens when connection is lost */
	abstract void noPong();

	/** to stop the while-loops => stopping PingPong when Client leaves */
	void close(){
		running = false;
	}
}