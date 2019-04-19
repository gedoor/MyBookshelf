package com.kunfei.bookshelf.web;

import com.kunfei.bookshelf.MApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {

    AssetsWeb assetsWeb = new AssetsWeb();

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
            body = assetsWeb.readFile(uri);
        } catch (IOException e) {
            body = e.getMessage();
        }

        return newFixedLengthResponse(body);
    }

}
