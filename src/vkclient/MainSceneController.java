package vkclient;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.URLDataSource;

public class MainSceneController implements Initializable {
    public ImageView avatarImage;
    public ListView<VKObject> audioList, friendsList;
    public VBox playerControls;
    public Button playButton;
    public Label currentAudioTitle;

    public Label infoName;
    public Label infoNickname;

    private Player player;
    private String currentTrackUrl = "";
    DataSource dataSource;

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
            infoName.setText(userInfo.get("first_name") + " " + userInfo.get("last_name"));
            infoNickname.setText(userInfo.get("nickname"));
            avatarImage.setImage(new Image(userInfo.get("photo_200_orig")));

            initAudioListView();

            initFriendsListView();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initFriendsListView() {
        List<FriendVKObject> friendItems = VK.getInstance().getCurrentUserFriends(); //new String[]{"first_name", "last_name"});

        ObservableList<VKObject> friendsItemsList = FXCollections.observableArrayList(friendItems);

        friendsList.setItems(friendsItemsList);

        friendsList.setCellFactory(new Callback<ListView<VKObject>, ListCell<VKObject>>() {
            @Override
            public ListCell<VKObject> call(ListView<VKObject> param) {
                ListCell<VKObject> cell = new ListCell<VKObject>(){
                    @Override
                    protected void updateItem(VKObject vkItem, boolean bln) {
                        super.updateItem(vkItem, bln);
                        if (vkItem != null) {
                            setText(vkItem.getVisibleText());
                        }
                    }

                };

                return cell;
            }
        });
    }

    public void initAudioListView() {
        List<AudioVKObject> audioItems = VK.getInstance().getCurrentUserAudio();
        ObservableList<VKObject> audioItemsList = FXCollections.observableArrayList(audioItems);
        audioList.setItems(audioItemsList);

        audioList.setCellFactory(new Callback<ListView<VKObject>, ListCell<VKObject>>() {
            @Override
            public ListCell<VKObject> call(ListView<VKObject> param) {
                ListCell<VKObject> cell = new ListCell<VKObject>(){
                    @Override
                    protected void updateItem(VKObject audioItem, boolean bln) {
                        super.updateItem(audioItem, bln);
                        if (audioItem != null) {
                            setText(audioItem.getVisibleText());
                        }
                    }

                };

                return cell;
            }
        });

        audioList.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<VKObject>() {
                    @Override
                    public void changed(ObservableValue<? extends VKObject> observable,
                                        VKObject oldValue, VKObject newValue) {
                        if (player == null || player.getState() != Controller.Started) {
                            currentAudioTitle.setText(newValue.getVisibleText());
                        }

                    }
                }
        );

        audioList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        String mp3Url = audioList.getSelectionModel().getSelectedItem().get("url");

                        try {

                            if (player != null) {
                                player.stop();
                                player.close();
                            }

                            if (dataSource == null) {
                                dataSource = new URLDataSource(new URL(mp3Url));
                            } else {
                                dataSource.stop();
                                dataSource.disconnect();
                                dataSource = new URLDataSource(new URL(mp3Url));
                                // dataSource.setLocator(new MediaLocator(new URL(mp3Url)));
                            }

                            dataSource.connect();
                            System.out.println(dataSource.getContentType());

                            if (player == null) {
                                player = Manager.createRealizedPlayer(dataSource.getLocator());
                                // = Manager.createRealizedPlayer(new MediaLocator(new URL(mp3Url)));
                            } else {
                                // player.setSource(dataSource);
                                player.deallocate();
                                player = Manager.createRealizedPlayer(dataSource.getLocator());

                            }

                            currentAudioTitle.setText(audioList.getSelectionModel().getSelectedItem().getVisibleText());
                            currentTrackUrl = mp3Url;
                            player.start();
                            playButton.setText("Pause");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });

    }

    public void playSelectedAudio(ActionEvent actionEvent) {
        String mp3Url = audioItems.get(audioList.getSelectionModel().getSelectedItem());

        try {
            if (player == null) {
                if (dataSource == null) {
                    dataSource = new URLDataSource(new URL(mp3Url));
                } else {
                    dataSource.setLocator(new MediaLocator(new URL(mp3Url)));
                }

                player = Manager.createRealizedPlayer(dataSource);
                // = Manager.createRealizedPlayer(new MediaLocator(new URL(mp3Url)));

                currentAudioTitle.setText(audioList.getSelectionModel().getSelectedItem().getVisibleText());
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

                    if (dataSource == null) {
                        dataSource = new URLDataSource(new URL(mp3Url));
                    } else {
                        dataSource.setLocator(new MediaLocator(new URL(mp3Url)));
                    }

                    player.setSource(dataSource);

                    currentAudioTitle.setText(audioList.getSelectionModel().getSelectedItem().getVisibleText());
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
