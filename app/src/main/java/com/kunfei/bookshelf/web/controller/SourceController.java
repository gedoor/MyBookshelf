package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.web.utils.Controller;
import com.kunfei.bookshelf.web.utils.ReturnData;

import java.util.List;

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
        List<BookSourceBean> bookSourceBeans = Controller.getPostData(session, new TypeToken<List<BookSourceBean>>() {
        }.getType());
        int count = 0;
        int allCount = bookSourceBeans.size();
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
                continue;
            }
            BookSourceManager.addBookSource(bookSourceBean);
            count++;
        }
        ReturnData returnData = new ReturnData();
        if (count == 0) {
            returnData.setSuccess(false);
            returnData.setErrorMsg("未添加书源，书源名称和URL不能为空");
            return NanoHTTPD.newFixedLengthResponse(returnData.toJson());
        }
        if (count == allCount) {
            returnData.setSuccess(true);
            returnData.setData("未添加书源，书源名称和URL不能为空");
            return NanoHTTPD.newFixedLengthResponse(returnData.toJson());
        }
        returnData.setSuccess(true);
        returnData.setData("一共" + allCount + "个书源，成功" + count + "个，失败" + (allCount - count) + "个");
        return NanoHTTPD.newFixedLengthResponse(returnData.toJson());
    }

    public NanoHTTPD.Response getSource(NanoHTTPD.IHTTPSession session) throws Exception {


        return NanoHTTPD.newFixedLengthResponse("");
    }

    public NanoHTTPD.Response getSources(NanoHTTPD.IHTTPSession session) throws Exception {
        return NanoHTTPD.newFixedLengthResponse(new ReturnData().setData(BookSourceManager.getAllBookSource()).toJson());
    }
}
