package com.kunfei.bookshelf.web.utils;

import android.content.res.AssetManager;
import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;


public class AssetsWeb {
    private AssetManager assetManager;
    private String rootPath = "web";

    public AssetsWeb(String rootPath) {
        if (!TextUtils.isEmpty(rootPath)) {
            this.rootPath = rootPath;
        }
        assetManager = MApplication.getInstance().getAssets();
    }

    public NanoHTTPD.Response getResponse(String path) throws IOException {
        path = (rootPath + path).replaceAll("/+", File.separator);
        InputStream inputStream = assetManager.open(path);
        return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK,
                getMimeType(path),
                inputStream);
    }

    private String getMimeType(String path) {
        String mimeType = "text/html";
        if (StringUtils.endWithIgnoreCase(path, ".html") || StringUtils.endWithIgnoreCase(path, ".htm")) {
            mimeType = "text/html";
        } else if (StringUtils.endWithIgnoreCase(path, ".js")) {
            mimeType = "text/javascript";
        } else if (StringUtils.endWithIgnoreCase(path, ".css")) {
            mimeType = "text/css";
        }
        return mimeType;
    }
}
