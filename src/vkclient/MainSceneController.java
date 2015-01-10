package vkclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import java.io.File;

import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.PlugInManager;
import javax.media.format.AudioFormat;

public class MainSceneController implements Initializable {
    public Label infoText;
    public ImageView avatarImage;
    public ListView<String> audioList, friendsList;

    private Map<String, String> audioItems = new HashMap<>();

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

    Player mp3Player;

    public void playSelectedAudio(ActionEvent actionEvent) {
        if (mp3Player != null) {
            mp3Player.stop();
            // mp3Player.close();
        }

        String mp3Url = audioItems.get(audioList.getSelectionModel().getSelectedItem());
        System.out.println(mp3Url);

        Format input1 = new AudioFormat(AudioFormat.MPEGLAYER3);
        Format input2 = new AudioFormat(AudioFormat.MPEG);
        Format output = new AudioFormat(AudioFormat.LINEAR);

        PlugInManager.addPlugIn(
                "com.sun.media.codec.audio.mp3.JavaDecoder",
                new Format[]{input1, input2},
                new Format[]{output},
                PlugInManager.CODEC
        );

        try {
            if (mp3Player == null) {
                mp3Player = Manager.createRealizedPlayer(new MediaLocator(new URL(mp3Url)));
            } else {
                // mp3Player. new MediaLocator(new URL(mp3Url)));
            }

            mp3Player.start();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    public void stopPlayAudio(ActionEvent actionEvent) {
        mp3Player.stop();
        // mp3Player.close();
    }
}
