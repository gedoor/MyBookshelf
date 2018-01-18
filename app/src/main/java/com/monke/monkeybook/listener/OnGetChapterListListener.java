//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.listener;

import com.monke.monkeybook.bean.BookShelfBean;

public interface OnGetChapterListListener {
    void doOnNext(BookShelfBean bookShelfBean);

    void success(BookShelfBean bookShelfBean);

    void error();
}
