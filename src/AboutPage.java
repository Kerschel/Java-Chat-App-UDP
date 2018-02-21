import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.*;
public class AboutPage {
    public static void display(String title){
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setWidth(530);

        Label l1 =  new Label("This Application was created using JavaFX by Kerschel James version 1.0");
        Label l2 =  new Label("Created on 21/02/2018");


        Button close = new Button("Close");
        close.setOnAction(e->window.close());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(l1,l2,close);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

    }

}
