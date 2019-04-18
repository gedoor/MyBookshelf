package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.web.model.ReturnData;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.ResponseBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.util.MediaType;

import java.util.List;

@RestController
class SourceController {

    @ResponseBody
    @PostMapping(path = "/saveSource", consumes = MediaType.APPLICATION_JSON_VALUE)
    Object saveSource(@RequestBody BookSourceBean bookSourceBean) {
        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
            ReturnData returnData = new ReturnData();
            returnData.setSuccess(false);
            returnData.setErrorMsg("书源名称和URL不能为空");
            return returnData;
        }
        BookSourceManager.addBookSource(bookSourceBean);
        return "书源["+bookSourceBean.getBookSourceName()+"]保存成功";
    }
    @ResponseBody
    @PostMapping(path = "/saveSources", consumes = MediaType.APPLICATION_JSON_VALUE)
    Object saveSources(@RequestBody List<BookSourceBean> bookSourceBeans) {
        int count = 0;
        int allCount = bookSourceBeans.size();
        for (BookSourceBean bookSourceBean: bookSourceBeans) {
            if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl())) {
                continue;
            }
            BookSourceManager.addBookSource(bookSourceBean);
            count++;
        }
        if(count == 0){
            ReturnData returnData = new ReturnData();
            returnData.setSuccess(false);
            returnData.setErrorMsg("未添加书源，书源名称和URL不能为空");
            return returnData;
        }
        if(count == allCount){
            return "成功导入"+allCount+"个书源";
        }
        return "一共"+allCount+"个书源，成功"+count+"个，失败"+(allCount-count)+"个";
    }

    @ResponseBody
    @GetMapping(path = "/getSources")
    Object getSources() {
        return BookSourceManager.getAllBookSource();
    }
}
