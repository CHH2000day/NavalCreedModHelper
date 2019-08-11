package com.CHH2000day.navalcreed.modhelper;

import com.google.gson.Gson;

public final class GsonHelper {
    private static Gson mGson;

    public synchronized static Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }
}
