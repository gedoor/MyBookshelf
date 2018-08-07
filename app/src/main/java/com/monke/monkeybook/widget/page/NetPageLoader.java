package com.monke.monkeybook.widget.page;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by newbiechen on 17-5-29.
 * 网络页面加载器
 */

public class NetPageLoader extends PageLoader {
    private static final String TAG = "PageFactory";

    public NetPageLoader(PageView pageView, BookShelfBean collBook) {
        super(pageView, collBook);
    }

    @Override
    public void refreshChapterList() {
        if (mCollBook.getChapterList() == null) return;

        mChapterList = mCollBook.getChapterList();
        isChapterListPrepare = true;

        // 目录加载完成，执行回调操作。
        if (mPageChangeListener != null) {
            mPageChangeListener.onCategoryFinish(mChapterList);
        }

        // 如果章节未打开
        if (!isChapterOpen()) {
            // 打开章节
            openChapter(mCollBook.getDurChapterPage());
        }
    }

    @Override
    protected BufferedReader getChapterReader(ChapterListBean chapter) throws Exception {
        File file = new File(Constant.BOOK_CACHE_PATH + mCollBook.getBookInfoBean().getName()
                + File.separator + chapter.getDurChapterName() + FileUtils.SUFFIX_NB);
        if (!file.exists()) return null;

        Reader reader = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);
        return br;
    }

    @Override
    protected boolean hasChapterData(ChapterListBean chapter) {
        return BookshelfHelp.isChapterCached(mCollBook.getBookInfoBean().getName(), chapter.getDurChapterName());
    }

    // 装载上一章节的内容
    @Override
    boolean parsePrevChapter() {
        boolean isRight = super.parsePrevChapter();

        if (mPageChangeListener != null && mCurChapterPos >= 1) {
            mPageChangeListener.requestChapters(mCurChapterPos - 1);
        }
        return isRight;
    }

    // 装载当前章内容。
    @Override
    boolean parseCurChapter() {
        boolean isRight = super.parseCurChapter();

        if (mPageChangeListener != null) {
            for (int i=mCurChapterPos; i < mCurChapterPos + 5; i++) {
                if (i < mChapterList.size()) {
                    mPageChangeListener.requestChapters(i);
                }
            }
        }

        return isRight;
    }

    // 装载下一章节的内容
    @Override
    boolean parseNextChapter() {
        boolean isRight = super.parseNextChapter();

        if (mPageChangeListener != null) {
            for (int i=mCurChapterPos + 1; i < mCurChapterPos + 6; i++) {
                if (i < mChapterList.size()) {
                    mPageChangeListener.requestChapters(i);
                }
            }
        }

        return isRight;
    }

}

