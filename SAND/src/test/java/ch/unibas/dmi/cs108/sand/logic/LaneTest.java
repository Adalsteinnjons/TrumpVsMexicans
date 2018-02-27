package ch.unibas.dmi.cs108.sand.logic;

import ch.unibas.dmi.cs108.sand.network.CommandList;
import org.junit.Test;

import static org.junit.Assert.*;

public class LaneTest {

	/** Add mexican correctly */
	@Test
	public void addMexican(){
		Lane l = new Lane(1);
		Character c = new Character(1,CommandList.MEXICAN,1);
		l.addCharacter(c,20);
	}
	/** add Mexican on wrong lane */
	@Test(expected = IllegalArgumentException.class)
	public void addOnWrongLane(){
		Lane l = new Lane(1);
		Character c = new Character(1,CommandList.MEXICAN,2);
		l.addCharacter(c,20);
	}
	/** add null to lane */
	@Test(expected = IllegalArgumentException.class)
	public void addNullCharacter(){
		Lane l = new Lane(1);
		l.addCharacter(null,20);
	}

	@Test
	public void getFirstTrump() throws Exception {
		Lane lane = new Lane(1);
		Character a = new Character(1, CommandList.TRUMP,1);
		Character b = new Character(2, CommandList.TRUMP,1);
		lane.addCharacter(a,20);
		lane.addCharacter(b,20);
		Character c = lane.getFirstTrump();
		assertEquals(c.getId(),1);
	}
	/** get First Trump on empty lane */
	@Test
	public void getFirstTrumpOnEmptyLane(){
		Lane lane = new Lane(1);
		assertNull(lane.getFirstTrump());
	}

	@Test
	public void getFirstMex() throws Exception {
		Lane lane = new Lane(1);
		Character a = new Character(1, CommandList.MEXICAN,1);
		Character b = new Character(2, CommandList.MEXICAN,1);
		lane.addCharacter(a,20);
		lane.addCharacter(b,20);
		Character c = lane.getFirstMex();
		assertEquals(c.getId(),1);
	}
	/** get first Mex on empty lane */
	@Test
	public void getFirstMexOnEmptyLane(){
		Lane lane = new Lane(1);
		assertNull(lane.getFirstMex());
	}

	/** move Character forward */
	@Test
	public void move(){
		Lane l = new Lane(1);
		Character c = new Character(1,CommandList.MEXICAN,1);
		l.addCharacter(c,20);
		double expected = 0+c.getXPos();
		l.move();
		expected = expected-c.getXPos();
		expected = Math.abs(expected);
		assertEquals(expected,c.getSpeed(),0);
	}

}