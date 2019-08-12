package com.kunfei.bookshelf.model.task;

import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.DownloadBookBean;
import com.kunfei.bookshelf.bean.DownloadChapterBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.model.impl.IDownloadTask;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class DownloadTaskImpl implements IDownloadTask {

    private int id;

    private boolean isDownloading = false;

    private DownloadBookBean downloadBook;
    private List<DownloadChapterBean> downloadChapters;

    private boolean isLocked = false;

    private CompositeDisposable disposables;

    protected DownloadTaskImpl(int id, DownloadBookBean downloadBook) {
        this.id = id;
        this.downloadBook = downloadBook;
        downloadChapters = new ArrayList<>();
        disposables = new CompositeDisposable();

        Observable.create((ObservableOnSubscribe<DownloadBookBean>) emitter -> {
            List<BookChapterBean> chapterList = BookshelfHelp.getChapterList(downloadBook.getNoteUrl());
            if (!chapterList.isEmpty()) {
                for (int i = downloadBook.getStart(); i <= downloadBook.getEnd(); i++) {
                    DownloadChapterBean chapter = new DownloadChapterBean();
                    chapter.setBookName(downloadBook.getName());
                    chapter.setDurChapterIndex(chapterList.get(i).getDurChapterIndex());
                    chapter.setDurChapterName(chapterList.get(i).getDurChapterName());
                    chapter.setDurChapterUrl(chapterList.get(i).getDurChapterUrl());
                    chapter.setNoteUrl(chapterList.get(i).getNoteUrl());
                    chapter.setTag(chapterList.get(i).getTag());
                    if (!BookshelfHelp.isChapterCached(chapter.getBookName(), chapter.getTag(), chapter, false)) {
                        downloadChapters.add(chapter);
                    }
                }
            }
            downloadBook.setDownloadCount(downloadChapters.size());
            emitter.onNext(downloadBook);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<DownloadBookBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(DownloadBookBean downloadBook) {
                        if (downloadBook.isValid()) {
                            onDownloadPrepared(downloadBook);
                            whenProgress(downloadChapters.get(0));
                        } else {
                            onDownloadComplete(downloadBook);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        downloadBook.setValid(false);
                        onDownloadError(downloadBook);
                    }
                });
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void startDownload(Scheduler scheduler) {
        if (isFinishing()) return;

        if (disposables.isDisposed()) {
            disposables = new CompositeDisposable();
        }

        isDownloading = true;

        toDownload(scheduler);
    }

    @Override
    public void stopDownload() {
        if (!disposables.isDisposed()) {
            disposables.dispose();
        }

        if (isDownloading) {
            isDownloading = false;
            onDownloadComplete(downloadBook);
        }

        if (!isFinishing()) {
            downloadChapters.clear();
        }

    }

    @Override
    public boolean isDownloading() {
        return isDownloading;
    }

    @Override
    public boolean isFinishing() {
        return downloadChapters.isEmpty();
    }

    @Override
    public DownloadBookBean getDownloadBook() {
        return downloadBook;
    }

    private synchronized void toDownload(Scheduler scheduler) {
        if (isFinishing()) {
            return;
        }

        if (!isLocked) {
            getDownloadingChapter()
                    .subscribe(new MyObserver<DownloadChapterBean>() {
                        @Override
                        public void onNext(DownloadChapterBean chapterBean) {
                            if (chapterBean != null) {
                                downloading(chapterBean, scheduler);
                            } else {
                                isLocked = true;
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            onDownloadError(downloadBook);
                        }
                    });
        }
    }

    /**
     * @return 章节下载信息
     */
    private Observable<DownloadChapterBean> getDownloadingChapter() {
        return Observable.create(emitter -> {
            DownloadChapterBean next = null;
            List<DownloadChapterBean> temp = new ArrayList<>(downloadChapters);
            for (DownloadChapterBean data : temp) {
                boolean cached = BookshelfHelp.isChapterCached(data.getBookName(), data.getTag(), data, false);
                if (cached) {
                    removeFromDownloadList(data);
                } else {
                    next = data;
                    break;
                }
            }
            emitter.onNext(next);
        });
    }

    /**
     * 下载
     */
    private synchronized void downloading(DownloadChapterBean chapter, Scheduler scheduler) {
        whenProgress(chapter);
        BookShelfBean bookShelfBean = BookshelfHelp.getBook(chapter.getNoteUrl());
        Observable.create((ObservableOnSubscribe<DownloadChapterBean>) e -> {
            if (!BookshelfHelp.isChapterCached(chapter.getBookName(), chapter.getTag(), chapter, false)) {
                e.onNext(chapter);
            } else {
                e.onError(new Exception("cached"));
            }
            e.onComplete();
        })
                .flatMap(result -> {
                    return WebBookModel.getInstance().getBookContent(bookShelfBean, chapter, null);
                })
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<BookContentBean>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(BookContentBean bookContentBean) {
                        RxBus.get().post(RxBusTag.CHAPTER_CHANGE, bookContentBean);
                        removeFromDownloadList(chapter);
                        whenNext(scheduler, true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        removeFromDownloadList(chapter);
                        if (TextUtils.equals(e.getMessage(), "cached")) {
                            whenNext(scheduler, false);
                        } else {
                            whenError(scheduler);
                        }
                    }
                });
    }

    /**
     * 从下载列表移除
     */
    private synchronized void removeFromDownloadList(DownloadChapterBean chapterBean) {
        downloadChapters.remove(chapterBean);
    }

    private void whenNext(Scheduler scheduler, boolean success) {
        if (!isDownloading) {
            return;
        }

        if (success) {
            downloadBook.successCountAdd();
        }
        if (isFinishing()) {
            stopDownload();
            onDownloadComplete(downloadBook);
        } else {
            onDownloadChange(downloadBook);
            toDownload(scheduler);
        }
    }

    private void whenError(Scheduler scheduler) {
        if (!isDownloading) {
            return;
        }

        if (isFinishing()) {
            stopDownload();
            if (downloadBook.getSuccessCount() == 0) {
                onDownloadError(downloadBook);
            } else {
                onDownloadComplete(downloadBook);
            }
        } else {
            toDownload(scheduler);
        }
    }

    private void whenProgress(DownloadChapterBean chapterBean) {
        if (!isDownloading) {
            return;
        }
        onDownloadProgress(chapterBean);
    }
}
