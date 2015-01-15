package vkclient;

import java.util.Map;

/**
 * Created by timur560 on 15.01.15.
 */
abstract class VKObject {
    protected Map<String, String> values;

    public VKObject(Map<String, String> values) {
        this.values = values;
    }

    public String get(String index) {
        return values.get(index);
    }

    public abstract String getVisibleText();
}