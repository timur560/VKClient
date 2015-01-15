package vkclient;

import java.util.Map;

/**
 * Created by timur on 15.01.15.
 */
public class FriendVKObject extends VKObject {

    public FriendVKObject(Map<String, String> values) {
        super(values);
    }

    @Override
    public String getVisibleText() {
        return values.get("first_name") + " " + values.get("last_name");
    }
}
