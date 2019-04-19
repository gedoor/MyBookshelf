package com.kunfei.bookshelf.web;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {

    public HttpServer(int port) {
        super(port);
    }


    @Override
    public Response serve(IHTTPSession session) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("Sorry, Can't Found the page!");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(builder.toString());
    }

}
