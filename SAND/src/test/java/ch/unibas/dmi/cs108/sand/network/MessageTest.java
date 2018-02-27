package ch.unibas.dmi.cs108.sand.network;

import static org.junit.Assert.*;
import org.junit.Test;

public class MessageTest {
	@Test
	public void toStringMessage() {
		Message m = new Message(CommandList.MESSAGE,"hallo");
		String parsed = m.toString();
		assertEquals("MESSAGE%hallo",parsed);
	}

	@Test
	public void toStringGameReady() {
		Message m = new Message(CommandList.GAME_READY,CommandList.MEXICAN.toString());
		String parsed = m.toString();
		assertEquals("GAME_READY%MEXICAN",parsed);
	}
	@Test
	public void toStringGoodbye(){
		Message m = new Message(CommandList.GOODBYE);
		String parsed = m.toString();
		assertEquals(parsed,"GOODBYE");
	}

	@Test
	public void parseMessage() throws Exception {
		String toParse = "MESSAGE%hallo";
		Message m = Message.parse(toParse);
		assertEquals(toParse,m.toString());
	}
	@Test
	public void parseGameReady() throws Exception {
		String toParse = "GAME_READY%MEXICAN";
		Message m = Message.parse(toParse);
		assertEquals(toParse,m.toString());
	}
	@Test
	public void parseMessageWithCommand(){
		//String toParse = "MESSAGE%GOODBYE";
		String toParse = "MESSAGE%GOODBYE";
		Message m = Message.parse(toParse);
		assertEquals(m.getCommand(),CommandList.MESSAGE);
		assertEquals(m.getMessage(),"GOODBYE");
	}
	@Test(expected = IllegalArgumentException.class)
	public void parseNonsense(){
		String toParse = "asdf";
		Message m = Message.parse(toParse);
	}
	@Test
	public void parseEmpty(){
		//String toParse = "MESSAGE%GOODBYE";
		String toParse = "";
		Message m = Message.parse(toParse);
		assertNull(m);
	}

	@Test
	public void highscores(){
		Message m = new Message(CommandList.HIGHSCORE,"nils%1234%4321");
		String mm = m.toString();
		Message m2 = Message.parse(mm);
		assertEquals(m2.getMessage(),"nils%1234%4321");
	}
}