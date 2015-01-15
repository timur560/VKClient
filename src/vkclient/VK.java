package vkclient;


import com.sun.deploy.util.StringUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.stringtree.json.JSONReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author timur
 */
class VK {

    // id  : 4716034
    // key : Ig86Qn0W4tB8uWp62sqd

    public static final String LOGIN_URL = "https://oauth.vk.com/authorize?" +
            "client_id=4716034&" +
            "scope=audio,video,friends,photos,messages&" + // http://vk.com/dev/permissions
            "redirect_uri=https://oauth.vk.com/blank.html&" +
            "display=popup&" + // page, popup, mobile
            // "v=5.27&" +
            "response_type=token";

    private static final String[] USER_INFO_FIELDS_ALL = {
            "sex", "bdate", "city", "country", "photo_50", "photo_100",
            "photo_200_orig", "photo_200", "photo_400_orig", "photo_max", "photo_max_orig",
            "photo_id", "online", "online_mobile", "domain" , "has_mobile", "contacts",
            "connections", "site", "education", "universities", "schools", "can_post",
            "can_see_all_posts", "can_see_audio", "can_write_private_message", "status", "last_seen",
            "common_count", "relation", "relatives", "counters", "screen_name", "maiden_name", "timezone",
            "occupation", "activities", "interests", "music", "movies", "tv", "books",
            "games", "about", "quotes", "personal"
    };

    private static final String[] FRIENDS_FIELDS_ALL = {
            "nickname", "domain", "sex", "bdate", "city", "country", "timezone", "photo_50",
            "photo_100", "photo_200_orig", "has_mobile", "contacts", "education", "online",
            "relation", "last_seen", "status", "can_write_private_message", "can_see_all_posts",
            "can_post", "universities"
    };

    public static final String API_URL = "https://api.vk.com/method/";

    private static VK instance = null;

    private String accessToken = "";
    private String login = "";
    private String password = "";
    private String userId = "";

    public static VK getInstance() {
        if (instance == null) {
            instance = new VK();
        }
        return instance;
    }

    public Optional<Pair<String, String>> showLoginDialog() {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Login VK");
        dialog.setGraphic(new ImageView(VK.class.getResource("/images/vk.png").toString()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> username.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public void loginVK() {
        Optional<Pair<String, String>> result = showLoginDialog();

        login = result.get().getKey();
        password = result.get().getValue();

        try {

            HttpClient client = HttpClientBuilder.create().build();

            HttpPost post;
            HttpResponse response;

            post = new HttpPost(LOGIN_URL);
            response = client.execute(post);
            post.abort();

            String ipH, to;
            Document doc;

            if (response.getFirstHeader("location") != null) {
                String HeaderLocation = response.getFirstHeader("location").getValue();
                URI RedirectUri = new URI(HeaderLocation);
                ipH= RedirectUri.getQuery().split("&")[2].split("=")[1];
                to=RedirectUri.getQuery().split("&")[4].split("=")[1];
            } else {
                doc = Jsoup.connect(LOGIN_URL).get();

                ipH = doc.select("input[name=\"ip_h\"]").get(0).attr("value");
                to = doc.select("input[name=\"to\"]").get(0).attr("value");
            }

            // Auth

            String loginUrl = "https://login.vk.com/?act=login&soft=1" +
                "&_origin=http://oauth.vk.com" +
                "&q=1" +
                "&ip_h=" + ipH +
                "&from_host=oauth.vk.com" +
                "&to=" + to +
                "&expire=0" +
                "&email=" + login +
                "&pass=" + password;

            post = new HttpPost(loginUrl);

            response = client.execute(post);
            post.abort();

            // Permissions
            String HeaderLocation = response.getFirstHeader("location").getValue();

            if (response.getFirstHeader("location") == null) {
                doc = Jsoup.connect(HeaderLocation).get();
                HeaderLocation = doc.select("form").get(0).attr("action");
            } else {
                HeaderLocation = response.getFirstHeader("location").getValue();
            }

            post = new HttpPost(HeaderLocation);
            response = client.execute(post);
            post.abort();

            // Last redirect for getting token
            if (response.getFirstHeader("location") == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("VK Login Fail");
                alert.setHeaderText("Cannot login VK");
                alert.setContentText("Wrong e-mail or password");

                alert.showAndWait();
                return;
//                doc = Jsoup.connect(HeaderLocation).get();
//                System.out.println(doc);
//                System.exit(0);
            } else {

                HeaderLocation = response.getFirstHeader("location").getValue();
            }

            post = new HttpPost(HeaderLocation);
            response = client.execute(post);
            post.abort();

            // get token
            if (response.getFirstHeader("location") != null) {
                HeaderLocation = response.getFirstHeader("location").getValue();
            } else {
                doc = Jsoup.connect(HeaderLocation).get();
                System.out.println(doc);
                System.exit(0);
            }

            accessToken = HeaderLocation.split("#")[1].split("&")[0].split("=")[1];
            userId = HeaderLocation.split("#")[1].split("&")[2].split("=")[1];
        } catch (Exception ex) {
            Logger.getLogger(VK.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String vkApiRequest(String method, Map<String, String> params) {
        if (accessToken.isEmpty()) {
            return null;
        }

        List<String> paramsArray = params.keySet().stream().map(paramKey ->
                paramKey + "=" + params.get(paramKey)).collect(Collectors.toList());

        String paramsString = StringUtils.join(paramsArray, "&");

        String url = API_URL + method + "?" +
                paramsString +
                "&access_token=" + accessToken;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line = reader.readLine();
            reader.close();

            return line;

        } catch (Exception e) {
            return null;
        }

    }

    public List<Map<String, String>> getUsersInfo(String[] uids, String[] fields) {
        if (fields == null || fields.length == 0) {
            fields = USER_INFO_FIELDS_ALL;
        }

        Map<String, String> params = new HashMap<>();

        params.put("uids", String.join(",", uids));
        params.put("fields", String.join(",", fields));

        JSONReader jsonReader = new JSONReader();

        Object result = jsonReader.read(vkApiRequest("users.get", params));

        return (List) ((Map) result).get("response");
    }

    public Map<String, String> getCurrentUserInfo() {
        return getUsersInfo(new String[]{userId}, USER_INFO_FIELDS_ALL).get(0);
    }

    public List<FriendVKObject> getFriends(String uid, String[] fields) {
        if (fields == null) {
            fields = FRIENDS_FIELDS_ALL;
        }

        Map<String, String> params = new HashMap<>();

        params.put("user_id", uid);
        params.put("fields", String.join(",", fields));

        JSONReader jsonReader = new JSONReader();

        Object resultJson = jsonReader.read(vkApiRequest("friends.get", params));

        List<FriendVKObject> result = new ArrayList<>();

        for (Map friendData : (List<Map>) ((Map) resultJson).get("response")) {
            result.add(new FriendVKObject(friendData));
        }

        return result;
    }

    public List<FriendVKObject> getCurrentUserFriends(String[] fields) {
        return getFriends(userId, fields);
    }

    public List<FriendVKObject> getCurrentUserFriends() {
        return getFriends(userId, FRIENDS_FIELDS_ALL);
    }

    public List<AudioVKObject> getAudio(String ownerId) {
        Map<String, String> params = new HashMap<>();

        params.put("owner_id", ownerId);

        JSONReader jsonReader = new JSONReader();

        Object resultJson = jsonReader.read(vkApiRequest("audio.get", params));

        List resultList = (List) ((Map) resultJson).get("response");

        resultList.remove(0);

        List<AudioVKObject> result = new ArrayList<>();

        for (Map audioData : (List<Map>) resultList) {
            result.add(new AudioVKObject(audioData));
        }

        return result;
    }

    public List<AudioVKObject> getCurrentUserAudio() {
        return getAudio(userId);
    }

}
