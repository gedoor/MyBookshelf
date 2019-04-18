package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.http.RequestBody;

import java.io.IOException;

@RestController
class SourceController {

    @PostMapping(path = "/saveSource", consumes = "application/json")
    String saveSource(RequestBody body) {
        try {
            String json = body.string();
            BookSourceBean bookSourceBean = new Gson().fromJson(json, new TypeToken<BookSourceBean>() {
            }.getType());
            if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl()))
                return "书源名称和URL不能为空";
            BookSourceManager.addBookSource(bookSourceBean);
        } catch (IOException e) {
            return e.getMessage();
        }
        return "保存成功";
    }

}
