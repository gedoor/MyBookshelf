package com.kunfei.bookshelf.web;

import com.kunfei.bookshelf.web.controller.SourceController;
import com.kunfei.bookshelf.web.utils.AssetsWeb;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {
    private AssetsWeb assetsWeb = new AssetsWeb("web");

    public HttpServer(int port) {
        super(port);
    }


    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        try {
            switch (uri) {
                case "saveSource":
                    return new SourceController().saveSource(session);
                case "saveSources":
                    return new SourceController().saveSources(session);
                case "getSource":
                    return new SourceController().getSource(session);
                case "getSources":
                    return new SourceController().getSources(session);
                default:
                    if (uri.endsWith("/")) {
                        uri = uri + "index.html";
                    }
            }
            return assetsWeb.getResponse(uri);
        } catch (Exception e) {
            return newFixedLengthResponse(e.getMessage());
        }
    }


}
