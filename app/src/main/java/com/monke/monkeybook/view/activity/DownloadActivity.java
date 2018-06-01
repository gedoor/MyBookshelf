package com.monke.monkeybook.view.activity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.bean.DownloadChapterBean;
import com.monke.monkeybook.dao.BookShelfBeanDao;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.dao.DownloadChapterBeanDao;
import com.monke.monkeybook.help.MyItemTouchHelpCallback;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.service.DownloadService;
import com.monke.monkeybook.view.adapter.BookSourceAdapter;
import com.monke.monkeybook.view.adapter.DownloadAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DownloadActivity extends MBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.ll_content)
    LinearLayout llContent;

    private DownloadAdapter adapter;
    private boolean downloadPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_recycler_vew);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        downloadPause = !DownloadService.isStartDownload;
        initRecyclerView();
        RxBus.get().register(this);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadAdapter(this);
        recyclerView.setAdapter(adapter);
        downloadUp();
    }

    public void delDownload(String noteUrl) {
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder()
                    .where(DownloadChapterBeanDao.Properties.NoteUrl.eq(noteUrl))
                    .orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).list();
            DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteInTx(downloadChapterList);
            downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder().list();
            emitter.onNext(downloadChapterList == null ? 0 : downloadChapterList.size());
            emitter.onComplete();
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<Integer>() {
                    @Override
                    public void onNext(Integer size) {
                        if (downloadPause & size == 0) {
                            RxBus.get().post(RxBusTag.CANCEL_DOWNLOAD, downloadPause);
                        } else {
                            downloadUp();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.download_offline);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem pauseMenu = menu.getItem(0);
        if (downloadPause) {
            pauseMenu.setIcon(R.drawable.ic_play1);
            pauseMenu.setTitle(R.string.start);
        } else {
            pauseMenu.setIcon(R.drawable.ic_pause1);
            pauseMenu.setTitle(R.string.pause);
        }
        pauseMenu.getIcon().mutate();
        pauseMenu.getIcon().setColorFilter(getResources().getColor(R.color.menu_color_default), PorterDuff.Mode.SRC_ATOP);
        return super.onPrepareOptionsMenu(menu);
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_cancel:
                RxBus.get().post(RxBusTag.CANCEL_DOWNLOAD, downloadPause);
                break;
            case R.id.action_pause_resume:
                if (downloadPause) {
                    RxBus.get().post(RxBusTag.START_DOWNLOAD, downloadPause);
                } else {
                    RxBus.get().post(RxBusTag.PAUSE_DOWNLOAD, downloadPause);
                }
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadPause() {
        downloadPause = true;
        supportInvalidateOptionsMenu();
    }

    private void downloadUp() {
        Observable.create((ObservableOnSubscribe<List<DownloadBookBean>>) emitter -> {
            List<DownloadBookBean> downloadBookBeanList = new ArrayList<>();
            List<BookShelfBean> bookShelfBeanList = DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().queryBuilder()
                    .where(BookShelfBeanDao.Properties.Tag.notEq(BookShelfBean.LOCAL_TAG))
                    .orderDesc(BookShelfBeanDao.Properties.FinalDate).list();
            if (bookShelfBeanList != null && bookShelfBeanList.size() > 0) {
                for (BookShelfBean bookItem : bookShelfBeanList) {
                    List<DownloadChapterBean> downloadChapterList = DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().queryBuilder()
                            .where(DownloadChapterBeanDao.Properties.NoteUrl.eq(bookItem.getNoteUrl()))
                            .orderAsc(DownloadChapterBeanDao.Properties.DurChapterIndex).list();
                    if (downloadChapterList != null && downloadChapterList.size() > 0) {
                        DownloadBookBean downloadBookBean = new DownloadBookBean();
                        downloadBookBean.setName(bookItem.getBookInfoBean().getName());
                        downloadBookBean.setNoteUrl(bookItem.getNoteUrl());
                        downloadBookBean.setCoverUrl(bookItem.getBookInfoBean().getCoverUrl());
                        downloadBookBean.setDownload(downloadChapterList.size());
                        downloadBookBeanList.add(downloadBookBean);
                    }
                }
                emitter.onNext(downloadBookBeanList);
            } else {
                DbHelper.getInstance().getmDaoSession().getDownloadChapterBeanDao().deleteAll();
                emitter.onNext(new ArrayList<>());
            }
            emitter.onComplete();
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<List<DownloadBookBean>>() {
                    @Override
                    public void onNext(List<DownloadBookBean> downloadBookBeans) {
                        adapter.upDataS(downloadBookBeans);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }


    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.PAUSE_DOWNLOAD_LISTENER)})
    public void pauseTask(Object o) {
        downloadPause();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.FINISH_DOWNLOAD_LISTENER)})
    public void finishTask(Object o) {
        downloadUp();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.PROGRESS_DOWNLOAD_LISTENER)})
    public void progressTask(DownloadChapterBean downloadChapterBean) {
        downloadPause = false;
        supportInvalidateOptionsMenu();
        downloadUp();
    }
}
