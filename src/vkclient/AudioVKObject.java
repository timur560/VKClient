package vkclient;

import java.util.Map;

/**
 * Created by timur560 on 15.01.15.
 */
public class AudioVKObject extends VKObject {

    public AudioVKObject(Map<String, String> values) {
        super(values);
    }

    @Override
    public String getVisibleText() {
        return values.get("artist") + " - " + values.get("title");
    }
}
