package ch.unibas.dmi.cs108.sand.logic;

import ch.unibas.dmi.cs108.sand.network.CommandList;
import ch.unibas.dmi.cs108.sand.network.Message;
import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/**
 * Created by Adalsteinn on 4/1/2017.
 */
public class Character implements Comparable<Character>{

    private int id;
    private CommandList type;
    /** either TRUMP or MEXICAN
     * DONKEY belongs to side MEXICAN,
     * BULL belongs to side TRUMP
     * */
    private CommandList side;
	private String name;
    private int health;
    private int damage;
    private int cost;
    private double xPosition;
    private double yPosition;
    private int lane;
    private ImageView imageView;
    private TranslateTransition transition;
    private int resourceValue;
    private int speed;
    private boolean attacking = false;

    /**Default Constructor*/
    public Character(){

    }

    /** depending on type: set different values for health/damage/xPosition etc. */
    public Character(int id, CommandList type, int lane){
        this.id = id;
        this.type = type;
        this.lane = lane;
        setDefaultValues(type);
    }

    /** Enables sorting a List (Map) of Characters. Sorting by xPosition */
    @Override
    public int compareTo(Character a){
        return this.xPosition > a.xPosition ? 1 : (this.xPosition < a.xPosition ? -1 : 0);
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public CommandList getSide() {
        return side;
    }
    public void setSide(CommandList side) {
        this.side = side;
    }

    public CommandList getType(){
        return type;
    }
    private void setType(CommandList type){
        this.type = type;
    }

    public int getHealth() {
        return health;
    }
    private void setHealth(int health) {
        this.health = health;
    }

    int getDamage() {
        return damage;
    }
    private void setDamage(int damage) {
        this.damage = damage;
    }

    /** check if Character is in attacking mode */
    public boolean isAttacking() {
        return attacking;
    }
    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    int getSpeed() {
        return speed;
    }

    public int getCost() {
        return cost;
    }
    public void setCost(int cost) {
        this.cost = cost;
    }

    public double getXPos() {
        return xPosition;
    }
    private void setXPos(double xPosition) {
        this.xPosition = xPosition;
    }

    private double getYPos() {
        return yPosition;
    }
    public void setYPosition(double yPosition) {
        this.yPosition = yPosition;
    }

    /** Surrounding Rectangle to detect collision */
    public Rectangle getBounds(){
        return new Rectangle(getXPos(), getYPos(),75,75);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public int getLane() {
        return lane;
    }
    private void setLane(int lane){
        this.lane = lane;
    }

    public int getResourceValue() {
        return resourceValue;
    }
    public void setResourceValue(int resourceValue){
    	this.resourceValue = resourceValue;
	}

    public TranslateTransition getTransition(){
        return transition;
    }


    /** set the default values (e.g. default speed) for each type (MEXICAN/TRUMP/BULL etc.) */
    private void setDefaultValues(CommandList type){
        switch (type){
            case MEXICAN:
                this.xPosition = 1050;
                this.health = 55;
                this.damage = 5;
                this.cost = 10;
                this.speed = 4;
                this.side = CommandList.MEXICAN;
                break;
            case DONKEY:
                this.xPosition = 1050;
                this.health = 80;
                this.damage = 1;
                this.cost = 20;
                this.speed = 8;
                this.side = CommandList.MEXICAN;
                break;

            case TRUMP:
                this.xPosition = 350;
                this.health = 60;
                this.damage = 5;
                this.cost = 10;
                this.speed = 4;
                this.side = CommandList.TRUMP;
                break;
            case BULL:
                this.xPosition = 350;
                this.health = 75;
                this.damage = 2;
                this.cost = 20;
                this.speed = 8;
                this.side = CommandList.TRUMP;
                break;
            default:
                throw new IllegalArgumentException("Character type needs to be one of the following: MEXICAN, TRUMP, DONKEY, BULL");
        }
    }

    /** Serialize a Character to a String
     * @return serialized Character (without the "CHARACTER%"-part)
     * */
    public String toString(){
        //String out = "{";
        String out = "id:"+id+",";
        out += "type:"+type+",";
        out += "xPos:"+xPosition+",";
        out += "lane:"+lane+",";
        out += "health:"+health+",";
        out += "damage:"+damage+",";
        out += "attacking:"+attacking;
        return out;
    }

    /** move Character forward and return Character as Message */
    Message forward(){
        attacking = false;
        if(this.side==CommandList.TRUMP){
            setXPos(getXPos()+speed);
        }
        else if(this.side==CommandList.MEXICAN){
            setXPos(getXPos()-speed);
        }
        return new Message(CommandList.CHARACTER,toString());
    }

    /** Character is hit and looses life
     * @param attack strength of the attacking Character (his damage-value) */
    Message hit(int attack){
        attacking=true;
        health = health-attack;
        if(health<0){
            health=0;
        }
        return new Message(CommandList.CHARACTER,toString());
    }

    /** parse a String to a Character.
     * @return Exported Character
     */
    public static Character parse(String in){
        if(in=="" || in==null){
            return null;
        }
        in = in.replaceAll("\\s","");//remove whitespace
        String data[] = in.split(",");
        Character c = new Character();
        for (String i : data) {
            String j[] = i.split(":");
            String key = j[0];
            String val = j[1];
            switch (key) {
                case "id":
                    c.setId(Integer.parseInt(val));
                    break;
                case "type":
                    try{
                        CommandList t =CommandList.valueOf(val);
                        c.setType(t);
                        c.setDefaultValues(t);
                    } catch(IllegalArgumentException e){
                        System.err.println("Couldn't parse type: "+val);
                        System.err.println("from: "+in);
                        e.printStackTrace();
                    }
                    break;
                case "xPos":
                    c.setXPos(Double.parseDouble(val));
                    break;
                case "lane":
                    c.setLane(Integer.parseInt(val));
                    break;
                case "health":
                    c.setHealth(Integer.parseInt(val));
                    break;
                case "damage":
                    c.setDamage(Integer.parseInt(val));
                    break;
                case "attacking":
                    c.setAttacking(Boolean.parseBoolean(val));
                    break;
                default:
                    throw new IllegalArgumentException("couldn't parse to Character: "+in);
            }
        }
        return c;
    }
}