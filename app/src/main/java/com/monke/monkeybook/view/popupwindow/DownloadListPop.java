//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.DownloadChapterBean;
import com.monke.monkeybook.common.RxBusTag;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.DownloadChapterBeanDao;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DownloadListPop extends PopupWindow {
    private Context mContext;
    private View view;

    private TextView tvNone;
    private LinearLayout llDownload;

    private ImageView ivCover;
    private TextView tvName;
    private TextView tvChapterName;
    private TextView tvCancel;
    private TextView tvDownload;

    public DownloadListPop(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        view = LayoutInflater.from(mContext).inflate(R.layout.view_pop_downloadlist, null);
        this.setContentView(view);
        bindView();
        bindEvent();
        initWait();
        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_checkaddshelf);
        RxBus.get().register(DownloadListPop.this);
    }

    private void bindEvent() {
        tvCancel.setOnClickListener(v -> {
            RxBus.get().post(RxBusTag.CANCEL_DOWNLOAD, new Object());
            tvNone.setVisibility(View.VISIBLE);
        });
        tvDownload.setOnClickListener(v -> {
            if (tvDownload.getText().equals("开始下载")) {
                RxBus.get().post(RxBusTag.START_DOWNLOAD, new Object());
            } else {
                RxBus.get().post(RxBusTag.PAUSE_DOWNLOAD, new Object());
            }
        });
    }

    private void bindView() {
        tvNone = (TextView) view.findViewById(R.id.tv_none);
        llDownload = (LinearLayout) view.findViewById(R.id.ll_download);
        ivCover = (ImageView) view.findViewById(R.id.iv_cover);
        tvName = (TextView) view.findViewById(R.id.tv_name);
        tvChapterName = (TextView) view.findViewById(R.id.tv_chapter_name);
        tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        tvDownload = (TextView) view.findViewById(R.id.tv_download);
    }

    private void initWait() {
        Observable.create((ObservableOnSubscribe<DownloadChapterBean>) e -> {
            List<BookShelfBean> bookShelfBeanList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder().orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
            if (bookShelfBeanList != null && bookShelfBeanList.size() > 0) {
                for (BookShelfBean bookItem : bookShelfBeanList) {
                    if (!bookItem.getTag().equals(BookShelfBean.LOCAL_TAG)) {
                        List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder().where(DownloadChapterBeanDao.Properties.NoteUrl.eq(bookItem.getNoteUrl())).orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).limit(1).list();
                        if (downloadChapterList != null && downloadChapterList.size() > 0) {
                            e.onNext(downloadChapterList.get(0));
                            e.onComplete();
                            return;
                        }
                    }
                }
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                e.onNext(new DownloadChapterBean());
            } else {
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                e.onNext(new DownloadChapterBean());
            }
            e.onComplete();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new SimpleObserver<DownloadChapterBean>() {
                    @Override
                    public void onNext(DownloadChapterBean value) {
                        if (value.getNoteUrl() != null && value.getNoteUrl().length() > 0) {
                            llDownload.setVisibility(View.GONE);
                            tvNone.setVisibility(View.GONE);
                            tvDownload.setText("开始下载");
                        } else {
                            tvNone.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        tvNone.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void onDestroy() {
        RxBus.get().unregister(DownloadListPop.this);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.PAUSE_DOWNLOAD_LISTENER)
            }
    )
    public void pauseTask(Object o) {
        tvNone.setVisibility(View.GONE);
        llDownload.setVisibility(View.GONE);
        tvDownload.setText("开始下载");
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.FINISH_DOWNLOAD_LISTENER)
            }
    )
    public void finishTask(Object o) {
        tvNone.setVisibility(View.VISIBLE);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag(RxBusTag.PROGRESS_DOWNLOAD_LISTENER)
            }
    )
    public void progressTask(DownloadChapterBean downloadChapterBean) {
        tvNone.setVisibility(View.GONE);
        llDownload.setVisibility(View.VISIBLE);
        tvDownload.setText("暂停下载");
        Glide.with(mContext).load(downloadChapterBean.getCoverUrl()).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).centerCrop().placeholder(R.drawable.img_cover_default).into(ivCover);
        tvName.setText(downloadChapterBean.getBookName());
        tvChapterName.setText(downloadChapterBean.getDurChapterName());
    }

}
