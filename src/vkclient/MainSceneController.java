package vkclient;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {
    public Label infoText;
    public ImageView avatarImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VK.getInstance().loginVK();

        try {
            infoText.setText(VK.getInstance().getUserInfo());
        } catch (Exception e) {
            ///
        }
    }
}
