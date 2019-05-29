package org.webdav.http;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class WHandler extends URLStreamHandler {

    public static final URLStreamHandler HANDLER = new WHandler();

    protected int getDefaultPort() {
        return 80;
    }

    public URLConnection openConnection(URL u) {
        return null;
    }

    @Override
    protected void parseURL(URL url, String spec, int start, int end) {
        super.parseURL(url, spec, start, end);
    }
}
