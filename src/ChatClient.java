
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.List;

// to handle user events implements EventHandler
public class ChatClient extends Application implements Runnable  {
    public DatagramSocket aSocket = null;
    public Button send;
    public TextField textarea;
    public ListView userList;
    public ListView chatView;
    public MenuItem about;
    public static int state = 0;
    @FXML
    Button button;
    Stage window;
    public static String serverIP;
    Scene scene1,scene2;
    String IPAddress = " ";
    public static String Name = "Chat Messenger";
    public static String sendToIP = " ";
    public static ObservableList<Users> users = FXCollections.observableArrayList();
    public static ObservableList<String> chat = FXCollections.observableArrayList();

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle(Name);

        Parent root = FXMLLoader.load(getClass().getResource("chat.fxml"));

        Scene chatpage = new Scene(root,650,500);
        Scene loginpage = new Scene(setGridLogin(chatpage),300,150);
        window.getIcons().add(new Image("FXML/icon.png"));
        window.setScene(loginpage);
        window.show();

    }

    static class Cell extends ListCell<Users>{
        HBox hbox = new HBox();
        Image picture = new Image("FXML/person-male.png");
        ImageView img = new ImageView(picture);
        Label name = new Label(" ");
        Label who = new Label("        ");
        Button block = new Button("Block");

        public Cell(){
            super();
            block.setOnMouseClicked(e->{
                blockChat();
            });

            block.setStyle("-fx-font: 10 arial; -fx-base: #b6e7c9;");

            hbox.getChildren().addAll(img,name,who,block);
            img.setFitHeight(30);
            img.setFitWidth(30);
        }

        public void updateItem(Users name,boolean empty){
            if(name != null  && !empty) {
                if(name.name.trim().equals(Name.trim()))
                who.setText(" (You)");
            }

            super.updateItem(name,empty);
            setText(null);
            setGraphic(null);

            if(name != null  && !empty){
                this.name.setText(name.name);
                setGraphic(hbox);
            }
        }
    }




    static class ChatBox extends ListCell<String>{
        BorderPane border = new BorderPane();
        HBox hbox = new HBox();
        Image picture = new Image("FXML/person-male.png");
        ImageView img = new ImageView(picture);

        TextArea message = new TextArea(" ");
        public ChatBox(String orient){
            super();

            message.setPrefWidth(200);
            message.setWrapText(true);
            img.setFitHeight(30);
            img.setFitWidth(30);
            hbox.getChildren().addAll(message,img);
            if(state == 0)
                border.setRight(hbox);
            else if(state ==1){
                border.setLeft(hbox);
            }
        }

        public void updateItem(String message,boolean empty){
            try {
                super.updateItem(message, empty);
                setText(null);
                setGraphic(null);

                if (message != null && !empty) {
                    this.message.setText(message);
                    setGraphic(border);
                }
            }catch (Exception e){
                System.out.println("Error here"+e);
            }
        }
    }



    public void setChatPage(){
        userList.setOnMouseClicked(e-> {
            Users person = (Users)userList.getSelectionModel().getSelectedItem();
            System.out.println("clicked on " + person.getIp());
            sendToIP = person.getIp();
        });

        send.setOnMouseClicked(e->{
            sendChat("3" +textarea.getText());
            textarea.clear();
        });

        getOnlineUsers("2online");
        userList.setCellFactory(param -> new Cell());
        userList.setItems(users);

        chatView.setCellFactory(param -> new ChatBox(null));
        chatView.setItems(chat);

        about.setOnAction(e->{
            new AboutPage().display("About Software");
        });
    }

//Login and register self as online
    public String joinChat(String username){

        try {
            aSocket = new DatagramSocket();
            byte [] name = username.getBytes();
            InetAddress aHost = InetAddress.getByName(serverIP);
            int serverPort = 6789;
            DatagramPacket request =
                    new DatagramPacket(name,  username.length(), aHost, serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.setSoTimeout(3000);
            aSocket.receive(reply);
            return new String(reply.getData());
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
        return "Error";
    }


//    Block a specific user from chatting with you
    public static void blockChat() {
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            // create socket at agreed port
            int serverPort = 6789;
//                    Send the message to the server with the ip concatenated
            String message = "4block:" + sendToIP;

            byte[] send = message.getBytes();
            DatagramPacket reply = new DatagramPacket(send,send.length,
                    InetAddress.getByName(serverIP),serverPort);
            aSocket.send(reply);

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) aSocket.close();
        }
    }
//Ask the server to send a list of people who are online
    public void getOnlineUsers(String check){
        (new Thread(new ChatClient())).start();
        DatagramSocket Socket = null;
        try {
            Socket = new DatagramSocket();
            byte [] online = check.getBytes();

            InetAddress aHost = InetAddress.getByName(serverIP);
            int serverPort = 6789;
            //            Ask server to check who online
            DatagramPacket request =
                    new DatagramPacket(online,  check.length(), aHost, serverPort);
            Socket.send(request);

            byte[] buffer = new byte[10000];
            DatagramPacket reply = new DatagramPacket(buffer,buffer.length);
            Socket.setSoTimeout(3000);
            Socket.receive(reply);
//            Server sends back List of users who online
            ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(reply.getData()));
            List<Users> names = (List<Users>) iStream.readObject();
            users = FXCollections.observableList(names);



        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());
        }catch (ClassNotFoundException e) {e.printStackTrace();}
        finally {if(Socket != null) Socket.close();}
    }


    public GridPane setGridLogin(Scene chatpage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label nameLabel = new Label("Username");
        GridPane.setConstraints(nameLabel, 0, 0);
        TextField nameInput = new TextField();
        nameInput.setPromptText("Name");
        GridPane.setConstraints(nameInput, 1, 0);


        try {
            IPAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
//        Server IP

        Label serverLabel = new Label("Server IP Address");
        GridPane.setConstraints(serverLabel, 0, 1);
        TextField serverInput = new TextField();
        serverInput.setText(IPAddress);
        GridPane.setConstraints(serverInput,1,1);


//        Your IP
        Label pwLabel = new Label("My IP Address");
        GridPane.setConstraints(pwLabel, 0, 2);
        TextField pwInput = new TextField();
        pwInput.setText(IPAddress);
//        put check if ip not found display message
//        pwInput.setDisable(true);
        GridPane.setConstraints(pwInput, 1, 2);

        Button login = new Button("Login");
        GridPane.setConstraints(login, 1, 3);
        grid.getChildren().addAll(nameInput, nameLabel, login, pwLabel, pwInput,serverInput,serverLabel);

        login.setOnAction(e -> {
            serverIP = serverInput.getText();
            serverIP = serverIP.replace("/","");
            Name = nameInput.getText();
            if (joinChat("1"+Name).trim().equals("Connected")) {
                window.setScene(chatpage);
            } else {
                AlertError.display("Cannot Connect", "Check IP address or Server Offline");
            }
        });
        return grid;
    }

    public void sendChat(String message) {
            state =0;
            DatagramSocket aSocket = null;
            try {
                aSocket = new DatagramSocket();
                // create socket at agreed port
                int serverPort = 6789;
//                    Send the message to the server with the ip concatenated
                    message += "TO:" + sendToIP;
                    byte[] send = message.getBytes();
                        DatagramPacket reply = new DatagramPacket(send,send.length,
                                InetAddress.getByName(serverIP),serverPort);
                message = message.split("TO:")[0];
                message = message.replaceFirst("3","");
                        chat.add(message);
                    aSocket.send(reply);

            } catch (SocketException e) {
                System.out.println("Socket: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            } finally {
                if (aSocket != null) aSocket.close();
            }
    }


    @Override
    public void run() {
        DatagramSocket aSocket = null;
        byte[] buffer;
        try{
            aSocket = new DatagramSocket(6500);
            // create socket at agreed port

            while(true){
                buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                state=1;
                String message = new String(request.getData());
                System.out.println("Receiving");
                try {
                    chat.add(message);
                }
                catch (Exception e){
                    System.out.println("THe mesg " + e);
                }

            }
        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {System.out.println("IO rec: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}

    }
    }
