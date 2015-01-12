package vkclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;

public class MainSceneController implements Initializable {
    public Label infoText;
    public ImageView avatarImage;
    public ListView<String> audioList, friendsList;
    public VBox playerControls;
    public Button playButton;

    private Map<String, String> audioItems = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PlugInManager.addPlugIn(
                "com.sun.media.codec.audio.mp3.JavaDecoder",
                new Format[]{new AudioFormat(AudioFormat.MPEGLAYER3), new AudioFormat(AudioFormat.MPEG)},
                new Format[]{new AudioFormat(AudioFormat.LINEAR)},
                PlugInManager.CODEC
        );

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

            for (Map<String, String> audioItem : audio) {
                audioItems.put(audioItem.get("artist") + " - " + audioItem.get("title"), audioItem.get("url"));
            }

            ObservableList<String> audioItemsList = FXCollections.observableArrayList();
            audioItemsList.addAll(audio.stream().map(audioItem ->
                    audioItem.get("artist") + " - " + audioItem.get("title")).collect(Collectors.toList()));

            audioList.setItems(audioItemsList);
        } catch (Exception e) {
            ///
        }
    }

    private Player player;
    private String currentTrackUrl = "";

    public void playSelectedAudio(ActionEvent actionEvent) {
        String mp3Url = audioItems.get(audioList.getSelectionModel().getSelectedItem());

        try {
            if (player == null) {
                player = Manager.createRealizedPlayer(new MediaLocator(new URL(mp3Url)));
                currentTrackUrl = mp3Url;
                player.start();
                playButton.setText("Pause");
            } else if (player.getState() == Controller.Started) {
                player.stop();
                playButton.setText("Play");
            } else {
                if (!currentTrackUrl.equals(mp3Url)) {
                    player.stop();
                    player.close();
                    player = Manager.createRealizedPlayer(new MediaLocator(new URL(mp3Url)));
                    currentTrackUrl = mp3Url;
                }
                player.start();
                playButton.setText("Pause");
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    public void stopPlayAudio(ActionEvent actionEvent) {
        player.stop();
        player.close();
        playButton.setText("Play");
    }
}
