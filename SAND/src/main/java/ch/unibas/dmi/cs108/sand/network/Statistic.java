package ch.unibas.dmi.cs108.sand.network;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adallstein on 5/5/2017.
 */
public class Statistic implements Comparable<Statistic>{
    private String userName;
    private long time;
    private String timestamp;



    private String timeInWord;

    /** create a new Statistic Object
     * @param millis how long the game was being played
     * @param dateTime when the Highscore was achieved*/
    public Statistic(String userName, long millis, long dateTime) {
        this.userName = userName;
        this.time = millis;
        timeInWord = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        Timestamp t = new Timestamp(dateTime);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy HH:mm:ss");
        timestamp = sdf.format(t);
    }

    @Override
    public int compareTo(Statistic o) {

        if(o.getTime() > this.getTime()){
            return -1;
        }else {
            return 1;
        }
    }

    public String getTimeInWord() {
        return timeInWord;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String unserName) {
        this.userName = userName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTimeStamp(){
        return timestamp;
    }
}
