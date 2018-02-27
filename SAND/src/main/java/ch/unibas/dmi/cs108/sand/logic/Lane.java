package ch.unibas.dmi.cs108.sand.logic;

import ch.unibas.dmi.cs108.sand.network.CommandList;
import ch.unibas.dmi.cs108.sand.network.Message;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

class Lane {
	private int index = -1;

	private ConcurrentSkipListMap<Integer,Character> mexicans = new ConcurrentSkipListMap<Integer,Character>();
	private ConcurrentSkipListMap<Integer,Character> trumps = new ConcurrentSkipListMap<Integer,Character>();

	private int mexCounter = 0;
	private int trumpCounter = 0;

	/** create a new lane (identified by index)
	 * @param index needs to be between 1 and 6 */
	Lane(int index){
		this.index = index;
	}

	/** add a Character to this lane
	 * check if you got enough resources.
	 * @return if you got enough resources: return new amount of resources, if not: return null
	 * */
	Integer addCharacter(Character c,int resource){
		if(c==null){
			throw new IllegalArgumentException("trying to add null to this lane");
		}else if(c.getLane()!=index){
			throw new IllegalArgumentException("trying to add Character with lane "+c.getLane()+" to lane with index "+index);
		}

		if(c.getSide() == CommandList.MEXICAN){
			if(resource>=c.getCost()){
				if(mexicans.size()>0){
					double a = mexicans.lastEntry().getValue().getXPos();
					if(a<1050-80){
						mexCounter++;
						mexicans.put(mexCounter,c);
						return resource-c.getCost();
					}
				} else{
					mexCounter++;
					mexicans.put(mexCounter,c);
					return resource-c.getCost();
				}
			}

		} else if(c.getSide() == CommandList.TRUMP){
			if(resource>=c.getCost()){
				if(trumps.size()>0){
					double a = trumps.lastEntry().getValue().getXPos();
					if(a>350 + 80){
						trumpCounter++;
						trumps.put(trumpCounter,c);
						return resource-c.getCost();
					}
				} else{
					trumpCounter++;
					trumps.put(trumpCounter,c);
					return resource-c.getCost();
				}
			}
		}
		//else
		return null;
	}

	/** return the Trump-Character on a lane that is in the front
	 * this is the only Trump-Character on this lane that could be in attacking mode */
	Character getFirstTrump(){
		if(trumps.size()>0){
			return trumps.firstEntry().getValue();
		} else{
			return null;
		}
	}
	/** return the Mexican-Character on a lane that is in the front
	 * this is the only Mexican-Character on this lane that could be in attacking mode */
	Character getFirstMex(){
		if(mexicans.size()>0){
			return mexicans.firstEntry().getValue();
		} else{
			return null;
		}
	}

	/** every Character on this lane is moved forward (if possible)
	 * check if there is a collision
	 * the Server calls this function every x seconds
	 * SPACE: space between following Characters on lane */
	ArrayList<Message> move(){
		final int SPACE = 80;//space between following Characters on lane
		ArrayList<Message> updated = new ArrayList<Message>();

		Character temp;
		if(mexicans.size()>0){
			temp = getFirstMex();
			for(Map.Entry<Integer,Character> entry : mexicans.entrySet()) {
				Character c = entry.getValue();
				//ignore first entry, will be handled in collision
				if(entry != mexicans.firstEntry()){
					if(Math.abs(temp.getXPos()-c.getXPos())>SPACE) {
						updated.add(c.forward());
					}
				}
				temp=c;
			}
		}
		if(trumps.size()>0){
			temp = getFirstTrump();
			for(Map.Entry<Integer,Character> entry : trumps.entrySet()) {
				Character c = entry.getValue();
				//ignore first entry
				if(entry != trumps.firstEntry()){
					if(Math.abs(temp.getXPos()-c.getXPos())>SPACE) {
						updated.add(c.forward());
					}
				}
				temp=c;
			}
		}

		updated.addAll(collision());

		return updated;
	}

	/** detect collision by comparing the first Mexican and Trump on this lane.
	 * Subtract health if there is a collision
	 * @return a Message-list of the updated Characters (the fighting ones) */
	private ArrayList<Message> collision(){
		ArrayList<Message> updated = new ArrayList<Message>();
		//Collision
		if(getFirstTrump()!=null && getFirstMex()!=null){
			if(Math.abs(getFirstMex().getXPos()-getFirstTrump().getXPos())<100){
				updated.add(getFirstTrump().hit(getFirstMex().getDamage()));
				updated.add(getFirstMex().hit(getFirstTrump().getDamage()));
				if(getFirstTrump().getHealth()<=0){
					trumps.remove(trumps.firstEntry().getKey());
					//System.out.println("trump died");
				}
				if(getFirstMex().getHealth()<=0){
					//System.out.println(mexicans);
					mexicans.remove(mexicans.firstEntry().getKey());
					//System.out.println(mexicans);
					//System.out.println("mex died");
				}
			} else{
				updated.add(getFirstTrump().forward());
				updated.add(getFirstMex().forward());
			}
		} else{//there is either only mexicans or only trumps on the lane
			if(getFirstMex() != null){
				updated.add(getFirstMex().forward());
			}
			if(getFirstTrump() != null){
				updated.add(getFirstTrump().forward());
			}
		}

		return updated;
	}

	/** determine, if Game is finished an who won
	 * @return CommandList.MEXICAN or CommandList.TRUMP or null (if no one won)*/
	CommandList wonGame(){
		if(mexicans.size()>0){
			if(mexicans.firstEntry().getValue().getXPos()<290+30){
				return CommandList.MEXICAN;
			}
		}
		if(trumps.size()>0){
			if(trumps.firstEntry().getValue().getXPos()>1100-20){
				return CommandList.TRUMP;
			}
		}
		//if Game not finished yet:
		return null;
	}

}
