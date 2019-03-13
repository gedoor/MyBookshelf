package com.kunfei.bookshelf.presenter;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.DocumentHelper;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.contract.BookSourceContract;
import com.kunfei.bookshelf.service.CheckSourceService;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public class BookSourcePresenter extends BasePresenterImpl<BookSourceContract.View> implements BookSourceContract.Presenter {
    private BookSourceBean delBookSource;
    private Snackbar progressSnackBar;

    @Override
    public void saveData(BookSourceBean bookSourceBean) {
        AsyncTask.execute(() -> {
            DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
            BookSourceManager.refreshBookSource();
        });
    }

    @Override
    public void saveData(List<BookSourceBean> bookSourceBeans) {
        AsyncTask.execute(() -> {
            if (MApplication.getConfigPreferences().getInt("SourceSort", 0) == 0) {
                for (int i = 1; i <= bookSourceBeans.size(); i++) {
                    bookSourceBeans.get(i - 1).setSerialNumber(i);
                }
            }
            DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplaceInTx(bookSourceBeans);
            BookSourceManager.refreshBookSource();
        });
    }

    @Override
    public void delData(BookSourceBean bookSourceBean) {
        this.delBookSource = bookSourceBean;
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DbHelper.getDaoSession().getBookSourceBeanDao().delete(bookSourceBean);
            BookSourceManager.refreshBookSource();
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.getSnackBar(delBookSource.getBookSourceName() + "已删除", Snackbar.LENGTH_LONG)
                                .setAction("恢复", view -> restoreBookSource(delBookSource))
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("删除失败");
                        mView.refreshBookSource();
                    }
                });
    }

    @Override
    public void delData(List<BookSourceBean> bookSourceBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            for (BookSourceBean sourceBean : bookSourceBeans) {
                DbHelper.getDaoSession().getBookSourceBeanDao().delete(sourceBean);
            }
            BookSourceManager.refreshBookSource();
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.toast("删除成功");
                        mView.refreshBookSource();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("删除失败");
                    }
                });
    }

    private void restoreBookSource(BookSourceBean bookSourceBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            BookSourceManager.addBookSource(bookSourceBean);
            BookSourceManager.refreshBookSource();
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.refreshBookSource();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void importBookSource(String text) {
        mView.showSnackBar("正在导入书源", Snackbar.LENGTH_INDEFINITE);
        Observable<List<BookSourceBean>> observable = BookSourceManager.importSource(text);
        if (observable != null) {
            observable.subscribe(getImportObserver());
        } else {
            mView.showSnackBar("格式不对", Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    public void importBookSourceLocal(String path) {
        if (TextUtils.isEmpty(path)) {
            mView.toast(R.string.read_file_error);
            return;
        }
        String json;
        DocumentFile file;
        try {
            file = DocumentFile.fromFile(new File(path));
        } catch (Exception e) {
            mView.toast(path + "无法打开！");
            return;
        }
        json = DocumentHelper.readString(file);
        if (!isEmpty(json)) {
            mView.showSnackBar("正在导入书源", Snackbar.LENGTH_INDEFINITE);
            importBookSource(json);
        } else {
            mView.toast(R.string.read_file_error);
        }
    }

    private SimpleObserver<List<BookSourceBean>> getImportObserver() {
        return new SimpleObserver<List<BookSourceBean>>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onNext(List<BookSourceBean> bookSourceBeans) {
                if (bookSourceBeans.size() > 0) {
                    mView.refreshBookSource();
                    mView.showSnackBar(String.format("导入成功%d个书源", bookSourceBeans.size()), Snackbar.LENGTH_SHORT);
                } else {
                    mView.showSnackBar("格式不对", Snackbar.LENGTH_SHORT);
                }
            }

            @Override
            public void onError(Throwable e) {
                mView.showSnackBar(e.getMessage(), Snackbar.LENGTH_SHORT);
            }
        };
    }

    private String getProgressStr(int state) {
        return String.format(mView.getContext().getString(R.string.check_book_source) + mView.getContext().getString(R.string.progress_show),
                state, BookSourceManager.getAllBookSource().size());
    }

    @Override
    public void checkBookSource() {
        CheckSourceService.start(mView.getContext());
    }

    /////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    /////////////////////RxBus////////////////////////

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.CHECK_SOURCE_STATE)})
    public void upCheckSourceState(Integer state) {
        mView.refreshBookSource();

        if (state == -1) {
            mView.showSnackBar("校验完成", Snackbar.LENGTH_SHORT);
        } else {
            if (progressSnackBar == null) {
                progressSnackBar = mView.getSnackBar(getProgressStr(state), Snackbar.LENGTH_INDEFINITE);
                progressSnackBar.setAction(mView.getContext().getString(R.string.cancel), view -> CheckSourceService.stop(mView.getContext()));
            } else {
                progressSnackBar.setText(getProgressStr(state));
            }
            if (!progressSnackBar.isShown()) {
                progressSnackBar.show();
            }
        }
    }
}
