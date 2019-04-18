package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.web.model.ReturnData;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.util.MediaType;

@RestController
class SourceController {

    @ResponseBody
    @PostMapping(path = "/saveSource", consumes = MediaType.APPLICATION_JSON_VALUE)
    ReturnData saveSource(@RequestBody BookSourceBean bookSourceBean) {
        ReturnData returnData = new ReturnData();
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            returnData.setSuccess(false);
            returnData.setData("书源名称和URL不能为空");
            return returnData;
        }
        BookSourceManager.addBookSource(bookSourceBean);
        returnData.setSuccess(true);
        returnData.setData("保存成功");
        return returnData;
    }

}
