package com.kunfei.bookshelf.utils.web_dav.http;

import okhttp3.OkHttpClient;

public class OkHttp {

    private OkHttpClient okHttpClient;

    private OkHttp() {
    }

    public static OkHttp getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public OkHttpClient client() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder().build();
        }
        return okHttpClient;
    }

    private static class SingletonHelper {
        private final static OkHttp INSTANCE = new OkHttp();
    }

}
