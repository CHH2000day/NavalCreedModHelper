package com.chh2000day.navalcreed.modhelper;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class FormatHelperFactory {
    private static HashMap<Uri, AudioFormatHelper> audioHelperMap;

    public static AudioFormatHelper getAudioFormatHelper(Uri file, Context ctx) {
        AudioFormatHelper afh = null;

        if (audioHelperMap == null) {
            audioHelperMap = new HashMap<>();
        }
        afh = audioHelperMap.get(file);
        if (afh == null) {
            afh = new AudioFormatHelper(file, ctx);
            audioHelperMap.put(file, afh);
        }
        return afh;
    }

    public synchronized static void refreshCache(File file) {
        if (audioHelperMap == null) {
            return;
        }
        Collection c = audioHelperMap.values();
        for (Object o : c) {
            AudioFormatHelper afh = (AudioFormatHelper) o;
            afh.invalidCache(file);
        }
    }

    public synchronized static void denyAllCaches() {
        if (audioHelperMap == null) {
            return;
        }
        Collection c = audioHelperMap.values();
        for (Object o : c) {
            AudioFormatHelper afh = (AudioFormatHelper) o;
            afh.invalidCache(null, AudioFormatHelper.MODE_DENY_ALL_CACHE);
        }
    }
}
