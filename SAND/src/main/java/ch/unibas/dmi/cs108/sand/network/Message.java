package ch.unibas.dmi.cs108.sand.network;

import java.io.Serializable;

/** An object that contains a command (from enum CommandList) and optionally a corresponding message*/
public class Message implements Serializable {
    private String message;
    private CommandList command;
    private final char SEPARATOR = '%';

    /*Constructors*/
    /**
	 * creates Message-Object of type MESSAGE
	 * @param message Text-Message to be sent
	 * */
    public Message(String message) {
        this.message = message;
        command = CommandList.MESSAGE;//message as default
    }
	/**
	 * creates Message-Object of type MESSAGE
	 * @param command what type the MESSAGE-Object should be, e.g. END_GAME%John has left the Game
	 * @param message Text-Message to be sent
	 * */
    public Message(CommandList command, String message){
    	this.command = command;
    	this.message = message;
	}
	/**
	 * creates Message-Object of type MESSAGE
	 * @param command what type the MESSAGE-Object should be, e.g. END_GAME%John has left the Game
	 * @param message Text-Message to be sent, here of type int, so that you don't need to parse it to a String first
	 * */
	public Message(CommandList command, int message){
		this.command = command;
		this.message = Integer.toString(message);
	}
	/**@param command simple CommandList without a message, e.g. PING or PONG
	 * creates Message-Object with only Command and no additional String-Message */
	public Message(CommandList command){
		this.command = command;
		message = null;
	}

    public String getMessage() {
        return message;
    }
    public CommandList getCommand(){
    	return command;
	}

    public void setMessage(String message) {
        this.message =message;
    }
	public void setMessage(CommandList command, String message) {
		this.message = message;
		this.command = command;
	}

	/** return Message as String with correct Separator */
	public String toString() {
    	if(message!=null && !message.equals("")){
			return command.toString()+SEPARATOR+message;
		} else{
			return command.toString();
		}
	}

	/** parse Strings from Stream into Message object
	 * the String is split at the first occurrence of a %, the first part is the command and the second part is the message
	 * if the String cannot be split, it was a simple CommandList without a message*/
	static Message parse(String in){
		if(in==null || in.equals("")){
			return null;
		}
		String pattern = "(?<!\\\\)%";
		String res[] = in.split(pattern,2);//split only on first occurrence
		if(res.length==1){
			CommandList c = CommandList.valueOf(res[0]);
			return new Message(c);
		}else{
			CommandList c = CommandList.valueOf(res[0]);
			String msg = res[1];
			return new Message(c,msg);
		}
	}
}