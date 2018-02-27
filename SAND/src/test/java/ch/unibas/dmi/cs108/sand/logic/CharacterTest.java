package ch.unibas.dmi.cs108.sand.logic;

import ch.unibas.dmi.cs108.sand.network.CommandList;
import org.junit.Test;

import static org.junit.Assert.*;

public class CharacterTest {
	@Test
	public void toStringTypeDonkey() throws Exception {
		Character c = new Character(1,CommandList.DONKEY,2);
		String toString = c.toString();
		assertEquals(toString, "id:1,type:DONKEY,xPos:1050.0,lane:2,health:80,damage:1,attacking:false");
	}

	@Test
	public void toStringEmpty() {
		Character c = new Character();
		assertEquals(c.toString(), "id:0,type:null,xPos:0.0,lane:0,health:0,damage:0,attacking:false");
	}

	@Test
	public void parseTrump() throws Exception {
		String toParse = "id:1,type:TRUMP,xPos:3.3,lane:2,health:10,damage:10";
		Character c = Character.parse(toParse);
		assertEquals(c.getId(), 1);
		assertEquals(c.getType(), CommandList.TRUMP);
		assertEquals(c.getXPos(), 3.3, 0);
		assertEquals(c.getLane(), 2);
		assertEquals(c.getHealth(), 10);
		assertEquals(c.getDamage(), 10);
	}

	@Test
	public void parseEmpty() {
		String toParse = "";
		Character c = Character.parse(toParse);
		assertNull(c);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseNonsense() {
		String toParse = "asfd4erg:t4reg:34treg";
		Character c = Character.parse(toParse);
	}

	@Test
	public void parseBullWithSpaces() {
		String toParse = "id:1 , type:BULL , xPos:3.3 , lane:2 , health:10 , damage:10";
		Character c = Character.parse(toParse);
		assertEquals(c.getId(), 1);
		assertEquals(c.getType(), CommandList.BULL);
		assertEquals(c.getXPos(), 3.3, 0);
		assertEquals(c.getLane(), 2);
		assertEquals(c.getHealth(), 10);
		assertEquals(c.getDamage(), 10);
		assertEquals(c.getSide(),CommandList.TRUMP);
	}

	@Test
	public void getSide(){
		Character c = new Character(3,CommandList.BULL,5);
		assertEquals(c.getSide(),CommandList.TRUMP);
	}
}