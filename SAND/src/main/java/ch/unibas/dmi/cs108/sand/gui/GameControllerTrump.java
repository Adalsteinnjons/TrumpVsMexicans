package ch.unibas.dmi.cs108.sand.gui;

import ch.unibas.dmi.cs108.sand.logic.Character;
import ch.unibas.dmi.cs108.sand.network.CommandList;
import ch.unibas.dmi.cs108.sand.network.Message;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;

/**
 * Created by Syarif Hidayatullah, Nils Hansen on 4/1/2017. This class is the main controller of the Game window
 * when playing as Trump. This class extends the more generic class 'Game Controller' that holds functions that are used
 * in both trump and mexican controller.
 */
public class GameControllerTrump extends GameController{
	private int characterCounter = 0;
	private CommandList command = CommandList.TRUMP;
	private Timeline tacos;
	private AudioClip clip;

	@FXML
	private Button bt1,bt2,bt3,bt4,bt5,bt6;
	@FXML
	private BorderPane trumpBtn;
	@FXML
	private BorderPane bullBtn;

	/** During the initialisation, the random running tacos is set,
	 * */
	public void initialize(){
		clip = playSound("sounds/background.mp3");
		clip.setVolume(0.2);
		clip.play();
		Text text = new Text("Chat with your enemies!  " + "\n");
		text.setStyle("-fx-font-size: 12; -fx-fill: #0b701c;");
		Platform.runLater(
				() -> {
					getChatMessages().getChildren().add(text);
				}
		);

		initStartTacos();
		tacos = setTacos();
	}

	/** This is the action handler of the button on the left hand side of the field in the first lane
	 * it calls the newTrumpOnLane method which create a Character object in the first lane. */
	public void buttonLeft11(){
		//ClientController.getClient().testing("trump,1");
		newTrumpOnLane(1);
	}

	/** This is the action handler of the button on the left hand side of the field in the second lane
	 * it calls the newTrumpOnLane method which create a Character object in the second lane. */
	public void buttonLeft12(){
		//ClientController.getClient().testing("trump,2");
		newTrumpOnLane(2);
	}
	/** This is the action handler of the button on the left hand side of the field in the first lane
	 * it calls the newTrumpOnLane method which create a Character object in the third lane. */
	public void buttonLeft13(){
		//ClientController.getClient().testing("trump,3");
		newTrumpOnLane(3);
	}
	/** This is the action handler of the button on the left hand side of the field in the first lane
	 * it calls the newTrumpOnLane method which create a Character object in the fourth lane. */
	public void buttonLeft14(){
		//ClientController.getClient().testing("trump,4");
		newTrumpOnLane(4);
	}
	/** This is the action handler of the button on the left hand side of the field in the first lane
	 * it calls the newTrumpOnLane method which create a Character object in the fifth lane. */
	public void buttonLeft15(){
		//ClientController.getClient().testing("trump,5");
		newTrumpOnLane(5);
	}
	/** This is the action handler of the button on the left hand side of the field in the first lane
	 * it calls the newTrumpOnLane method which create a Character object in the sixth lane. */
	public void buttonLeft16(){
		//ClientController.getClient().testing("trump,6");
		newTrumpOnLane(6);
	}

	/** this method will set the Commandlist to BULL. If the user pressed any button on any lanes,
	 * Bull object is created and a new Bull image is rendered
 	 */
	public void bull(){
		command = CommandList.BULL;
		bullBtn.setStyle("-fx-background-color: burlywood");
		trumpBtn.setStyle("-fx-background-color: blanchedalmond");
	}

	/** this method will set the Commandlist to Trump. If the user pressed any button on any lanes,
	 *  Trump object is created and a new trump image is rendered
	 */
	public void trump(){
		command = CommandList.TRUMP;
		trumpBtn.setStyle("-fx-background-color: burlywood");
		bullBtn.setStyle("-fx-background-color: blanchedalmond");
	}

	private void newTrumpOnLane(int lane){
		characterCounter++;
		Character c  = new Character(characterCounter, command,lane);
		String newTrump = c.toString();
		//String newTrump = "id:"+characterCounter+",type:"+CommandList.TRUMP+",xPos:200,lane:"+lane+",health:10,damage:10;";
		Message send = new Message(CommandList.CHARACTER,newTrump);
		ClientController.getClient().sendGame(send);
		//System.out.println("c: sending "+newTrump);
	}

	/** This method is called if one of the trump objects reach the other ends of the field.
	 * All buttons are disabled and the winner dialog is shown */
	void displayWinnerAlert(){
		disabledButton();
		showWinnerDialog(CommandList.WINNER);
	}

	/** This method is called if one of the mexican objects reach the other ends of the field.
	 * All buttons are disabled and the loser dialog is shown */
	void displayLoserAlert(){
		disabledButton();
		showWinnerDialog(CommandList.LOSER);

	}

	/** disabled all buttons on the game field*/
	private void disabledButton(){
		bt1.setDisable(true);
		bt2.setDisable(true);
		bt3.setDisable(true);
		bt4.setDisable(true);
		bt5.setDisable(true);
		bt6.setDisable(true);
	}

	public AudioClip getClip() {
		return clip;
	}

	/** @return Timeline object that controls the random running tacos */
	public Timeline getTacos() {
		return tacos;
	}
}
