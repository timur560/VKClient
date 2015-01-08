package vkclient;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {
    public Label infoText;
    public ImageView avatarImage;
    public ListView audioList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VK.getInstance().loginVK();

        try {
            Map<String, String> userInfo = VK.getInstance().getCurrentUserInfo();
            infoText.setText(userInfo.get("first_name") + " " + userInfo.get("last_name"));
            
            
        } catch (Exception e) {
            ///
        }
    }
}
