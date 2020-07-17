package Lesson_6.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller {
    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    Button btn1;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    @FXML
    ListView<String> clientList;

    @FXML
    VBox VboxChat;

    private boolean isAuthorized;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADRESS = "localhost";
    final int PORT = 8189;

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if(!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);

            VboxChat = new VBox();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {

        if(socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {

        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/authok")) {
                                setAuthorized(true);
                                break;
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if(str.startsWith("/")) {
                                if (str.equals("/serverclosed"))  {
                                    // closeApp();
                                    break;
                                }
                                if (str.startsWith("/clientlist ")) {
                                    String[] tokens = str.split(" ");
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
//                                            MiniStage miniStage = new MiniStage();
//                                            miniStage.show();

                                            clientList.getItems().clear();
                                            for (int i = 1; i < tokens.length; i++) {
                                                clientList.getItems().add(tokens[i]);
                                            }
                                        }
                                    });
                                }
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int index = 0;

    public void sendMsg(ActionEvent actionEvent) {
        Label label = new Label(textField.getText() + "\n");
        VBox vBox = new VBox();

        if (index % 2 == 0){
            vBox.setAlignment(Pos.TOP_RIGHT);
        }else {
            vBox.setAlignment(Pos.TOP_LEFT);
        }

        vBox.getChildren().add(label);

        if (VboxChat != null)
            VboxChat.getChildren().add(vBox);

        textField.clear();
        textField.requestFocus();
        index++;

//        try {
//            out.writeUTF(textField.getText());
//            textField.clear();
//            textField.requestFocus();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//    @FXML
//    public void handleCloseButtonAction(ActionEvent event) {
//        Stage stage = (Stage) closeButton.getScene().getWindow();
//        stage.close();
//    }



//    public void closeApp() {
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.exit(0);
//    }
}
