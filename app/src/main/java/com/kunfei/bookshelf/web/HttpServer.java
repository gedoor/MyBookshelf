package com.kunfei.bookshelf.web;

import com.kunfei.bookshelf.web.controller.SourceController;
import com.kunfei.bookshelf.web.utils.AssetsWeb;
import com.kunfei.bookshelf.web.utils.ReturnData;

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
                case "/saveSource":
                    return new SourceController().saveSource(session);
                case "/saveSources":
                    return new SourceController().saveSources(session);
                case "/getSource":
                    return new SourceController().getSource(session);
                case "/getSources":
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

    public static Response newResponse(Object object) {
        Response response;
        if (object instanceof ReturnData) {
            response = newFixedLengthResponse(((ReturnData) object).toJson());
        } else {
            ReturnData returnData = new ReturnData();
            returnData.setSuccess(true);
            returnData.setData(object);
            response = newFixedLengthResponse(returnData.toJson());
        }
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Max-Age", "" + 42 * 60 * 60);
        return response;
    }

}
