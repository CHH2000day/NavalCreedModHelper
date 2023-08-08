package com.chh2000day.navalcreed.modhelper;

import com.orhanobut.logger.Logger;

import java.net.UnknownHostException;

import okhttp3.OkHttpClient;

public class OKHttpHelper {
	private static OkHttpClient client;

	public synchronized static OkHttpClient getClient() {
		try {
			if (client == null) init();
		} catch (UnknownHostException e) {
			Logger.e(e, "Failed to init Okhttp");
		}
		return client;
	}

	private static void init() throws UnknownHostException {
		client = new OkHttpClient.Builder()
				.build();
	}
}
