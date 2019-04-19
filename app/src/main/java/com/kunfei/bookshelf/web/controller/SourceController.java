package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.web.utils.Controller;
import com.kunfei.bookshelf.web.utils.ReturnData;

import fi.iki.elonen.NanoHTTPD;

public class SourceController {

    public NanoHTTPD.Response saveSource(NanoHTTPD.IHTTPSession session) throws Exception {
        BookSourceBean bookSourceBean = Controller.getPostData(session, new TypeToken<BookSourceBean>() {
        }.getType());
        ReturnData returnData = new ReturnData();
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            returnData.setSuccess(false);
            returnData.setErrorMsg("书源名称和URL不能为空");
            return NanoHTTPD.newFixedLengthResponse(returnData.toJson());
        }
        BookSourceManager.addBookSource(bookSourceBean);
        returnData.setSuccess(true);
        returnData.setData("书源[" + bookSourceBean.getBookSourceName() + "]保存成功");
        return NanoHTTPD.newFixedLengthResponse(returnData.toJson());
    }

    public NanoHTTPD.Response saveSources(NanoHTTPD.IHTTPSession session) throws Exception {


        return NanoHTTPD.newFixedLengthResponse("");
    }

    public NanoHTTPD.Response getSource(NanoHTTPD.IHTTPSession session) throws Exception {


        return NanoHTTPD.newFixedLengthResponse("");
    }

    public NanoHTTPD.Response getSources(NanoHTTPD.IHTTPSession session) throws Exception {


        return NanoHTTPD.newFixedLengthResponse("");
    }
}
