package com.chh2000day.navalcreed.modhelper;

import java.net.MalformedURLException;
import java.net.URL;

public class ServerActions {
    public static final String VALUE_LEGACY = "legacy";
    public static final String ACTION = "action";
    public static final String ACTION_CHECKTEST = "checkTest";
    public static final String VALUE_KEY = "key";
    public static final String VALUE_SSAID = "ssaid";
    public static final String VALUE_DEVICE = "device";
    public static final String ACTION_CHECKUPDATE = "checkUpdate";
    public static final String ACTION_GET_ANNOUNCEMENT = "getAnnouncement";
    public static final String VALUE_BUILD_TYPE = "buildType";
    public static final String BUILD_TYPE_RELEASE = "release";
    public static final String BUILD_TYPE_ALPHA = "alpha";
    public static URL REQUEST_URL;

    static {
        try {
            REQUEST_URL = new URL("https://ncapi.chh2000day.com/ncapi.do");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
