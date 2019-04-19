package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.web.utils.ReturnData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class SourceController {

    public NanoHTTPD.Response saveSource(NanoHTTPD.IHTTPSession session) throws IOException, NanoHTTPD.ResponseException {
        Map<String, String> files = new HashMap<String, String>();
        session.parseBody(files);

        BookSourceBean bookSourceBean = new BookSourceBean();
        ReturnData returnData = new ReturnData();
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            returnData.setSuccess(false);
            returnData.setErrorMsg("书源名称和URL不能为空");
        }
        BookSourceManager.addBookSource(bookSourceBean);
        returnData.setSuccess(true);
        returnData.setData("书源[" + bookSourceBean.getBookSourceName() + "]保存成功");
        return NanoHTTPD.newFixedLengthResponse(returnData.toJson());
    }

    public NanoHTTPD.Response saveSources(NanoHTTPD.IHTTPSession session) {


        return NanoHTTPD.newFixedLengthResponse("");
    }

    public NanoHTTPD.Response getSource(NanoHTTPD.IHTTPSession session) {


        return NanoHTTPD.newFixedLengthResponse("");
    }

    public NanoHTTPD.Response getSources(NanoHTTPD.IHTTPSession session) {


        return NanoHTTPD.newFixedLengthResponse("");
    }
}
