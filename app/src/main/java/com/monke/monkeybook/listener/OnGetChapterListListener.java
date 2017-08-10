package com.monke.monkeybook.listener;

import com.monke.monkeybook.bean.BookShelfBean;

public interface OnGetChapterListListener {
    public void success(BookShelfBean bookShelfBean);
    public void error();
}
