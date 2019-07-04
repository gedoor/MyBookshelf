package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.utils.GsonUtils;
import com.kunfei.bookshelf.web.utils.ReturnData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SourceController {

    public ReturnData saveSource(String postData) {
        BookSourceBean bookSourceBean = GsonUtils.parseJObject(postData, BookSourceBean.class);
        ReturnData returnData = new ReturnData();
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            return returnData.setErrorMsg("书源名称和URL不能为空");
        }
        BookSourceManager.addBookSource(bookSourceBean);
        return returnData.setData("");
    }

    public ReturnData saveSources(String postData) {
        List<BookSourceBean> bookSourceBeans = GsonUtils.parseJArray(postData, BookSourceBean.class);
        List<BookSourceBean> okSources = new ArrayList<>();
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
                continue;
            }
            BookSourceManager.addBookSource(bookSourceBean);
            okSources.add(bookSourceBean);
        }
        return (new ReturnData()).setData(okSources);
    }

    public ReturnData getSource(Map<String, List<String>> parameters) {
        List<String> strings = parameters.get("url");
        ReturnData returnData = new ReturnData();
        if (strings == null) {
            return returnData.setErrorMsg("参数url不能为空，请指定书源地址");
        }
        BookSourceBean bookSourceBean = BookSourceManager.getBookSourceByUrl(strings.get(0));
        if (bookSourceBean == null) {
            return returnData.setErrorMsg("未找到书源，请检查书源地址");
        }
        return returnData.setData(bookSourceBean);
    }

    public ReturnData getSources() {
        List<BookSourceBean> bookSourceBeans = BookSourceManager.getAllBookSource();
        ReturnData returnData = new ReturnData();
        if (bookSourceBeans.size() == 0) {
            return returnData.setErrorMsg("设备书源列表为空");
        }
        return returnData.setData(BookSourceManager.getAllBookSource());
    }

    public ReturnData deleteSources(String postData) {
        List<BookSourceBean> bookSourceBeans = GsonUtils.parseJArray(postData, BookSourceBean.class);
        /*List<BookSourceBean> okSources= new ArrayList<>();*/
        for (BookSourceBean bookSourceBean : bookSourceBeans) {
            /*if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
                continue;
            }*/
            BookSourceManager.removeBookSource(bookSourceBean);
            /*if(BookSourceManager.getBookSourceByUrl(bookSourceBean.getBookSourceUrl()) == null){
                okSources.add(bookSourceBean);
            }*/
        }
        return (new ReturnData()).setData("已执行"/*okSources*/);
    }
}
