package vkclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainSceneController implements Initializable {
    public Label infoText;
    public ImageView avatarImage;
    public ListView<String> audioList, friendsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VK.getInstance().loginVK();

        try {
            Map<String, String> userInfo = VK.getInstance().getCurrentUserInfo();
            infoText.setText(userInfo.get("first_name") + " " + userInfo.get("last_name"));

            // friends list
            List<Map<String, String>> friends = VK.getInstance().getCurrentUserFriends(new String[]{"first_name", "last_name"});
            ObservableList<String> friendsItems = FXCollections.observableArrayList();
            friendsItems.addAll(friends.stream().map(friend ->
                    friend.get("first_name") + " " + friend.get("last_name")).collect(Collectors.toList()));
            friendsList.setItems(friendsItems);

            // audios list
            List<Map<String, String>> audio = VK.getInstance().getCurrentUserAudio();
            ObservableList<String> auidoItems = FXCollections.observableArrayList();
            auidoItems.addAll(audio.stream().map(audioItem ->
                    audioItem.get("artist") + " - " + audioItem.get("title")).collect(Collectors.toList()));
            audioList.setItems(auidoItems);
        } catch (Exception e) {
            ///
        }
    }
}
