package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.utils.GsonUtils;
import com.kunfei.bookshelf.web.utils.ReturnData;

import java.util.List;
import java.util.Map;

public class BookshelfController {

    public ReturnData getBookshelf() {
        List<BookShelfBean> shelfBeans = BookshelfHelp.getAllBook();
        ReturnData returnData = new ReturnData();
        if (shelfBeans.isEmpty()) {
            return returnData.setErrorMsg("还没有添加小说");
        }
        return returnData.setData(shelfBeans);
    }

    public ReturnData getChapterList(Map<String, List<String>> parameters) {
        List<String> strings = parameters.get("url");
        ReturnData returnData = new ReturnData();
        if (strings == null) {
            return returnData.setErrorMsg("参数url不能为空，请指定书籍地址");
        }
        List<BookChapterBean> chapterList = BookshelfHelp.getChapterList(strings.get(0));
        return returnData.setData(chapterList);
    }

    public ReturnData getBookContent(Map<String, List<String>> parameters) {
        List<String> strings = parameters.get("url");
        ReturnData returnData = new ReturnData();
        if (strings == null) {
            return returnData.setErrorMsg("参数url不能为空，请指定内容地址");
        }
        BookChapterBean chapter = DbHelper.getDaoSession().getBookChapterBeanDao().load(strings.get(0));
        if (chapter == null) {
            return returnData.setErrorMsg("未找到");
        }
        BookShelfBean bookShelfBean = BookshelfHelp.getBook(chapter.getNoteUrl());
        if (bookShelfBean == null) {
            return returnData.setErrorMsg("未找到");
        }
        String content = BookshelfHelp.getChapterCache(bookShelfBean, chapter);
        if (!TextUtils.isEmpty(content)) {
            return returnData.setData(content);
        }
        try {
            BookContentBean bookContentBean = WebBookModel.getInstance().getBookContent(bookShelfBean, chapter, null).blockingFirst();
            return returnData.setData(bookContentBean.getDurChapterContent());
        } catch (Exception e) {
            return returnData.setErrorMsg(e.getMessage());
        }
    }

    public ReturnData saveBook(String postData) {
        BookShelfBean bookShelfBean = GsonUtils.parseJObject(postData, BookShelfBean.class);
        ReturnData returnData = new ReturnData();
        if (bookShelfBean != null) {
            DbHelper.getDaoSession().getBookShelfBeanDao().insertOrReplace(bookShelfBean);
            return returnData.setData("");
        }
        return returnData.setErrorMsg("格式不对");
    }

}
