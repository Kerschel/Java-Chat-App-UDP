import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import jdk.nashorn.internal.ir.Block;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;

// to handle user events implements EventHandler
public class ChatServer extends Application {
    Stage window;
    TableView<Users>  table ;
    public static ObservableList<Users> users = FXCollections.observableArrayList();
    public static ObservableList<Users> chat = FXCollections.observableArrayList();
    public static ArrayList<BlockList> blocklist = new ArrayList<>();
    public static void main(String[] args) {
        new ServerThreads().start();
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Online Info");

        TableColumn<Users,String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(120);
        TableColumn<Users,String> ipColumn = new TableColumn<>("IP Address");
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        ipColumn.setPrefWidth(130);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);


        users.add(new Users("",""));
        table = new TableView<>();
        table.setItems(users);
        table.getColumns().addAll(nameColumn,ipColumn);
        grid.getChildren().addAll(table);
        Scene scene = new Scene(grid, 300, 300);
        window.getIcons().add(new Image("FXML/servericon.png"));
        window.setScene(scene);
        window.show();
    }


    static class ServerThreads extends Thread {
        public void run() {
            DatagramSocket aSocket = null;
            try {

                aSocket = new DatagramSocket(6789);
                // create socket at agreed port

                while (true) {
                    byte[] buffer = new byte[1000];
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    String info = new String(request.getData());
                    InetAddress ip = request.getAddress();
//                    1 for trying to login chat
                    if((users.indexOf(info)==-1) && info.charAt(0) == '1'){
                        users.add(new Users(info.split("1")[1],ip.toString()));
                        chat.add(new Users(info.split("1")[1],ip.toString()));
                        String con = "Connected";
                        byte[] buff = con.getBytes();
                        DatagramPacket reply = new DatagramPacket(buff, con.length(),
                                request.getAddress(), request.getPort());
                        aSocket.send(reply);
                    }
//                    2 for checking online users
                    else if(info.charAt(0) == '2' ){
                        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                        ObjectOutput oo = new ObjectOutputStream(bStream);
                        oo.writeObject(new ArrayList<Users>(chat));
                        oo.close();
                        byte[] serializedUsers = bStream.toByteArray();
                        DatagramPacket reply = new DatagramPacket(serializedUsers, serializedUsers.length,
                                request.getAddress(), request.getPort());
                        aSocket.send(reply);
                    }
//                  3  Get message and get ip address to send message to in chat
                    else if(info.charAt(0) == '3' ){
                        String message = info.split("TO:")[0];
                        message = message.replaceFirst("3","");
                        String sendToIP = info.split("TO:")[1];
                        sendToIP = sendToIP.replace("/","");
                        byte[] buff = message.getBytes();
                        int state =0;
//                        Block user from chatting with someone
                        for(int i=0;i<blocklist.size();i++){
                            System.out.println(blocklist.get(i).getBlockip() + " " + blocklist.get(i).getMyip());
                            System.out.println(blocklist.get(i).getBlockip().equals(ip.toString().replace("/","")) + " " + blocklist.get(i).getMyip());
                            if(blocklist.get(i).checkBlock(ip.toString().replace("/","").trim(),sendToIP.trim()) ==1){
                                state =1;
                            }
                        }
                        if(state == 0) {
                            DatagramPacket reply = new DatagramPacket(buff, message.length(),
                                    InetAddress.getByName((sendToIP)), 6500);
                            aSocket.send(reply);
                        }
                    }
//                    4 block a specific user from chatting with you
                    else if(info.charAt(0) == '4' ){
                        String blockip = info.split("block:")[1];
                        blockip = blockip.replace("/","");
                        System.out.println(blockip+" "+ip.toString());
                        blocklist.add(new BlockList(ip.toString().replace("/","").trim(),blockip.trim()));
                    }


                }
            } catch (SocketException e) {
                System.out.println("Socket: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e);
            } finally {
                if (aSocket != null) aSocket.close();
            }

        }

    }
}