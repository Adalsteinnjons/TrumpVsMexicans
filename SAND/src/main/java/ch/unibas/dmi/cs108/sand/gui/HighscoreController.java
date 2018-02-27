package ch.unibas.dmi.cs108.sand.gui;

import ch.unibas.dmi.cs108.sand.network.Statistic;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Syarif Hidayatullah on 5/4/2017. The highscore information is taken from the stats.properties
 * and is displayed on a TableView in a separated dialog
 */
public class HighscoreController {
    @FXML
    private TableView<Statistic> statisticTableView;
    @FXML
    private TableColumn<Statistic,String> name;
    @FXML
    private TableColumn<Statistic,String> time;
    @FXML
    private TableColumn<Statistic,String> timeStamp;

    private ArrayList<Statistic> statistics = new ArrayList<>();

    /** Each time the highscore menu is pressed, the highscore window will be opened
     *  During the initialisation the stats.properties is read using the bufferedReader
     *  Statistic object is created and added to the Arraylist, which is sorted using the compare method of comparable interface
     *  The sorted list is displayed on a TableView*/
    public void initialize(){
        File statFile = new File(System.getProperty("user.dir")+"/stats.properties");

        BufferedReader br;
        String line;

        try {
            br = new BufferedReader(new FileReader(statFile.getPath()));
            while ((line=br.readLine()) != null){
                if(line.equals("")){
                    continue;
                }
                String[] parsed = line.split("%");
                Statistic statistic = new Statistic(parsed[0],Long.parseLong(parsed[1]),Long.parseLong(parsed[2]));
                statistics.add(statistic);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(statistics, new Comparator<Statistic>() {
            @Override
            public int compare(Statistic stat1, Statistic stat2){
                return  stat1.compareTo(stat2);
            }
        });

        ObservableList<Statistic> observableList = FXCollections.observableArrayList(statistics);
        name.setCellValueFactory(new PropertyValueFactory<Statistic,String>("userName"));
        time.setCellValueFactory(new PropertyValueFactory<Statistic,String>("timeInWord"));
        timeStamp.setCellValueFactory(new PropertyValueFactory<Statistic,String>("timeStamp"));
        statisticTableView.setItems(observableList);

    }
}