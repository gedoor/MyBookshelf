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
        if(session.getMethod().name().equals("OPTIONS")){
            Response response = newFixedLengthResponse("");
            response.addHeader("Access-Control-Allow-Methods", "POST");
            response.addHeader("Access-Control-Allow-Headers", "content-type");
            response.addHeader("Access-Control-Allow-Origin", session.getHeaders().get("origin"));
            response.addHeader("Access-Control-Max-Age", "3600");
            return response;
        }
        String uri = session.getUri();
        ReturnData returnData = null;
        try {
            if (uri.endsWith("/")) {
                uri = uri + "index.html";
            } else switch (uri) {
                case "/saveSource":
                    returnData = new SourceController().saveSource(session);
                    break;
                case "/saveSources":
                    returnData =  new SourceController().saveSources(session);
                    break;
                case "/getSource":
                    returnData =  new SourceController().getSource(session);
                    break;
                case "/getSources":
                    returnData =  new SourceController().getSources(session);
                    break;
            }
            if(returnData == null){
                return assetsWeb.getResponse(uri);
            }

            Response response = newFixedLengthResponse(returnData.toJson());
            response.addHeader("Access-Control-Allow-Methods", "GET, POST");
            response.addHeader("Access-Control-Allow-Origin", session.getHeaders().get("origin"));
            return response;
        } catch (Exception e) {
            return newFixedLengthResponse(e.getMessage());
        }
    }

}
