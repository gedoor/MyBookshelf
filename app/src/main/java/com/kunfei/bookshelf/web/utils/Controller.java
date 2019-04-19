package com.kunfei.bookshelf.web.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class Controller {

    public static <T> T getPostData(NanoHTTPD.IHTTPSession session, Type type) throws Exception {
        String postData = getPostData(session);
        return new Gson().fromJson(postData, type);
    }

    public static String getPostData(NanoHTTPD.IHTTPSession session) throws Exception {
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        return files.get("postData");
    }
}
