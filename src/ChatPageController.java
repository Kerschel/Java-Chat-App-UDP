package FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatPageController implements Initializable{

    public Button button;
    public ListView userList;

    public void clicker(){
        System.out.println("Data");
        userList.getItems().addAll("S","DSDS","SDS");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
