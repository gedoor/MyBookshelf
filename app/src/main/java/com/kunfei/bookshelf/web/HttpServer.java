package com.kunfei.bookshelf.web;

import com.google.gson.Gson;
import com.kunfei.bookshelf.web.controller.BookshelfController;
import com.kunfei.bookshelf.web.controller.SourceController;
import com.kunfei.bookshelf.web.utils.AssetsWeb;
import com.kunfei.bookshelf.web.utils.ReturnData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {
    private AssetsWeb assetsWeb = new AssetsWeb("web");

    public HttpServer(int port) {
        super(port);
    }


    @Override
    public Response serve(IHTTPSession session) {
        ReturnData returnData = null;
        String uri = session.getUri();

        try {
            switch (session.getMethod().name()) {
                case "OPTIONS":
                    Response response = newFixedLengthResponse("");
                    response.addHeader("Access-Control-Allow-Methods", "POST");
                    response.addHeader("Access-Control-Allow-Headers", "content-type");
                    response.addHeader("Access-Control-Allow-Origin", session.getHeaders().get("origin"));
                    //response.addHeader("Access-Control-Max-Age", "3600");
                    return response;

                case "POST":
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    String postData = files.get("postData");

                    switch (uri) {
                        case "/saveSource":
                            returnData = new SourceController().saveSource(postData);
                            break;
                        case "/saveSources":
                            returnData = new SourceController().saveSources(postData);
                            break;
                        case "/saveBook":
                            returnData = new BookshelfController().saveBook(postData);
                            break;
                        case "/deleteSources":
                            returnData = new SourceController().deleteSources(postData);
                    }
                    break;

                case "GET":
                    Map<String, List<String>> parameters = session.getParameters();

                    switch (uri) {
                        case "/getSource":
                            returnData = new SourceController().getSource(parameters);
                            break;
                        case "/getSources":
                            returnData = new SourceController().getSources();
                            break;
                        case "/getBookshelf":
                            returnData = new BookshelfController().getBookshelf();
                            break;
                        case "/getChapterList":
                            returnData = new BookshelfController().getChapterList(parameters);
                            break;
                        case "/getBookContent":
                            returnData = new BookshelfController().getBookContent(parameters);
                            break;
                    }
                    break;
            }

            if (returnData == null) {
                if (uri.endsWith("/")) {
                    uri = uri + "index.html";
                }
                return assetsWeb.getResponse(uri);
            }

            Response response = newFixedLengthResponse(new Gson().toJson(returnData));
            response.addHeader("Access-Control-Allow-Methods", "GET, POST");
            response.addHeader("Access-Control-Allow-Origin", session.getHeaders().get("origin"));
            return response;
        } catch (Exception e) {
            return newFixedLengthResponse(e.getMessage());
        }
    }

}
