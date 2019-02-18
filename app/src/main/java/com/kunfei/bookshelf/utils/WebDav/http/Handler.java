package com.kunfei.bookshelf.utils.WebDav.http;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

    public static final URLStreamHandler HANDLER = new Handler();

    protected int getDefaultPort() {
        return 80;
    }

    public URLConnection openConnection(URL u) throws IOException {
        return null;
    }

    @Override
    protected void parseURL(URL url, String spec, int start, int end) {
        super.parseURL(url, spec, start, end);
    }
}
