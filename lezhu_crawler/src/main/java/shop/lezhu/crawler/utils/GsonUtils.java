package shop.lezhu.crawler.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
    private static Gson GSON = null;


    public static Gson getGson() {
        if (GSON == null) {
            GsonBuilder gb = new GsonBuilder();
            GSON = gb.create();
        }
        return GSON;
    }
}
