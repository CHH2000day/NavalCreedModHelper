package com.chh2000day.navalcreed.modhelper;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class FormatHelperFactory {
    private static HashMap<Uri, AudioFormatHelper> audiohelpers;

    public static AudioFormatHelper getAudioFormatHelper(Uri file, Context ctx) {
        AudioFormatHelper afh = null;
        if (audiohelpers == null) {
            audiohelpers = new HashMap<>();
        }
        afh = audiohelpers.get(file);
        if (afh == null) {
            afh = new AudioFormatHelper(file, ctx);
            audiohelpers.put(file, afh);
        }
        return afh;
    }

    public synchronized static void refreshCache(File file) {
        if (audiohelpers == null) {
            return;
        }
        Collection c = audiohelpers.values();
        for (Object o : c) {
            AudioFormatHelper afh = (AudioFormatHelper) o;
            afh.invalidCache(file);
        }
    }

    public synchronized static void denyAllCaches() {
        if (audiohelpers == null) {
            return;
        }
        Collection c = audiohelpers.values();
        for (Object o : c) {
            AudioFormatHelper afh = (AudioFormatHelper) o;
            afh.invalidCache(null, afh.MODE_DENY_ALL_CACHE);
        }
    }

}
