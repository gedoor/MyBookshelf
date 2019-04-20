package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.web.utils.ReturnData;

import java.util.List;
import java.util.Map;

public class SourceController {

    public ReturnData saveSource(String postData) {
        BookSourceBean bookSourceBean = new Gson().fromJson(postData, new TypeToken<BookSourceBean>() {
        }.getType());
        ReturnData returnData = new ReturnData();
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            return returnData.setErrorMsg("书源名称和URL不能为空");
        }
        BookSourceManager.addBookSource(bookSourceBean);
        return returnData.setData("书源[" + bookSourceBean.getBookSourceName() + "]保存成功");
    }

    public ReturnData saveSources(String postData) {
        List<BookSourceBean> bookSourceBeans = new Gson().fromJson(postData, new TypeToken<List<BookSourceBean>>() {
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
            return returnData.setErrorMsg("未添加书源，书源名称和URL不能为空");
        }
        if (count == allCount) {
            return returnData.setData("成功添加"+count+"个书源");
        }
        return returnData.setData("一共" + allCount + "个书源，成功" + count + "个，失败" + (allCount - count) + "个");
    }

    public ReturnData getSource(Map<String,List<String>> parameters) {
        List<String> strings = parameters.get("url");
        ReturnData returnData = new ReturnData();
        if(strings == null){
            return returnData.setErrorMsg("参数url不能为空，请指定书源地址");
        }
        BookSourceBean bookSourceBean = BookSourceManager.getBookSourceByUrl(strings.get(0));
        if(bookSourceBean == null){
            return returnData.setErrorMsg("未找到书源，请检查书源地址");
        }
        return returnData.setData(bookSourceBean);
    }

    public ReturnData getSources() {
        List<BookSourceBean> bookSourceBeans = BookSourceManager.getAllBookSource();
        ReturnData returnData = new ReturnData();
        if(bookSourceBeans.size() == 0){
            return returnData.setErrorMsg("设备书源列表为空");
        }
        return returnData.setData(BookSourceManager.getAllBookSource());
    }
}
