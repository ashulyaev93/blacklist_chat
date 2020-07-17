package Lesson_6.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MiniStage extends Stage {
    public MiniStage() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("sample.fxml"));
            setTitle("chat 2k19");
            Scene scene = new Scene(root, 350, 375);
            setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
