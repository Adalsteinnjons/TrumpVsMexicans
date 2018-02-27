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
 * when playing as Mexican. This class extends the more generic class 'Game Controller' that holds functions that are used
 * in both trump and mexican controller.
 */
public class GameControllerMexican extends GameController{

	private int characterCounter = 0;
	private CommandList command = CommandList.MEXICAN;
	private Timeline tacos;
	private AudioClip clip;

	@FXML
	private Button bt1,bt2,bt3,bt4,bt5,bt6;
	@FXML
	private BorderPane donkeyBtn;
	@FXML
	private BorderPane mexBtn;

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


	public void buttonRight11(){
		newMexicanOnLane(1);
	}
	public void buttonRight12(){
		newMexicanOnLane(2);
	}
	public void buttonRight13(){
		newMexicanOnLane(3);
	}
	public void buttonRight14(){
		newMexicanOnLane(4);
	}
	public void buttonRight15(){
		newMexicanOnLane(5);
	}
	public void buttonRight16(){
		newMexicanOnLane(6);
	}


	public void mexican(){
		command = CommandList.MEXICAN;
		mexBtn.setStyle("-fx-background-color: burlywood");
		donkeyBtn.setStyle("-fx-background-color: blanchedalmond");

	}

	public void donkey(){
		command = CommandList.DONKEY;
		donkeyBtn.setStyle("-fx-background-color: burlywood");
		mexBtn.setStyle("-fx-background-color: blanchedalmond");


	}

	private void newMexicanOnLane(int lane){
		characterCounter++;
		Character c = new Character(characterCounter, command,lane);
		String newMexican = c.toString();
		Message send = new Message(CommandList.CHARACTER,newMexican);
		ClientController.getClient().sendGame(send);
	}

	void displayWinnerAlert(){
		disabledButton();

		showWinnerDialog(CommandList.WINNER);
	}

	void displayLoserAlert() {
		disabledButton();

	showWinnerDialog(CommandList.LOSER);
	}

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

	public Timeline getTacos() {
		return tacos;
	}
}
