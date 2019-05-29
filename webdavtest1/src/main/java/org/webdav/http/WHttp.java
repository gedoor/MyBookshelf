package org.webdav.http;

import okhttp3.OkHttpClient;

public class WHttp {

    private OkHttpClient okHttpClient;

    private WHttp() {
    }

    public static WHttp getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public OkHttpClient client() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder().build();
        }
        return okHttpClient;
    }

    private static class SingletonHelper {
        private final static WHttp INSTANCE = new WHttp();
    }

}
