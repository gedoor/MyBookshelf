package com.kunfei.bookshelf.web;

import com.kunfei.bookshelf.web.utils.AssetsWeb;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {

    private AssetsWeb assetsWeb = new AssetsWeb("web");

    public HttpServer(int port) {
        super(port);
    }


    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if (uri.endsWith("/")) {
            uri = uri + "index.html";
        }

        String body;
        try {
            return assetsWeb.getResponse(uri);
        } catch (IOException e) {
            body = e.getMessage();
        }

        return newFixedLengthResponse(body);
    }

}
