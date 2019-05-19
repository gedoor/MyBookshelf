package com.kunfei.bookshelf.model.task;

import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.utils.RxUtils;

import io.reactivex.Single;

public class DownloadContentTask {
    private BookInfoBean infoBean;
    private int chapterIndex;
    private Callback callback;

    public DownloadContentTask(BookInfoBean infoBean, int chapterIndex) {
        this.infoBean = infoBean;
        this.chapterIndex = chapterIndex;
    }

    public DownloadContentTask setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void startDownload() {
        Single.create(emitter -> {

        }).compose(RxUtils::toSimpleSingle)
                .subscribe();
    }


    public interface Callback {
        void finish(int chapterIndex, String content);

        void error(String errorMsg);
    }

}

