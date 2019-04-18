package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.util.MediaType;

@RestController
class SourceController {

    @ResponseBody
    @PostMapping(path = "/saveSource", consumes = MediaType.APPLICATION_JSON_VALUE)
    String saveSource(@RequestBody BookSourceBean bookSourceBean) {
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl()))
            return "书源名称和URL不能为空";
        BookSourceManager.addBookSource(bookSourceBean);
        return "保存成功";
    }

}
