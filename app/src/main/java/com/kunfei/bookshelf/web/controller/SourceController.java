package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

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
class SourceController {

    @ResponseBody
    @PostMapping(path = "/saveSource", consumes = MediaType.APPLICATION_JSON_VALUE)
    Object saveSource(@RequestBody BookSourceBean bookSourceBean) {
        ReturnData returnData = new ReturnData();
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            returnData.setSuccess(false);
            returnData.setErrorMsg("书源名称和URL不能为空");
            return returnData;
        }
        BookSourceManager.addBookSource(bookSourceBean);
        return "保存成功";
    }

}
