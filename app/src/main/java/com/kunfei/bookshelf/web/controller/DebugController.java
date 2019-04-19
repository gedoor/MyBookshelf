package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.utils.TimeUtils;
import com.kunfei.bookshelf.web.model.ReturnData;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.util.MediaType;

import static com.kunfei.bookshelf.model.content.Debug.DEBUG_TIME_FORMAT;

@RestController
public class DebugController {
    @ResponseBody
    @PostMapping(path = "/debug", consumes = MediaType.APPLICATION_JSON_VALUE)
    Object debug(@RequestBody BookSourceBean rules) {
        ReturnData retData = new ReturnData();
        retData.setSuccess(false);
        if (TextUtils.isEmpty(rules.getBookSourceName()))
        {
            retData.setErrorMsg("书源名称不能为空!");
            return retData;
        }
        if (TextUtils.isEmpty(rules.getBookSourceUrl()))
        {
            retData.setErrorMsg("书源域名不能为空!");
            return retData;
        }
        if (TextUtils.isEmpty(rules.getRuleSearchUrl()))
        {
            retData.setErrorMsg("搜索地址为空,调试中止!");
            return retData;
        }

        return String.format("%s %s", TimeUtils.getNowString(DEBUG_TIME_FORMAT), "≡开始搜索关键字\"我的\"");
    }
}
