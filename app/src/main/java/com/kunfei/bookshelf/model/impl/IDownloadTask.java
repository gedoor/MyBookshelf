package com.kunfei.bookshelf.model.impl;

import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.bean.DownloadBookBean;
import com.kunfei.bookshelf.bean.DownloadChapterBean;

import io.reactivex.Scheduler;

public interface IDownloadTask {

    int getId();

    void startDownload(Scheduler scheduler, int threadsNum);

    void stopDownload();

    boolean isDownloading();

    boolean isFinishing();

    DownloadBookBean getDownloadBook();

    void onDownloadPrepared(DownloadBookBean downloadBook);

    void onDownloadProgress(DownloadChapterBean chapterBean);

    void onDownloadChange(DownloadBookBean downloadBook);

    void onDownloadError(DownloadBookBean downloadBook);

    void onDownloadComplete(DownloadBookBean downloadBook);
}
