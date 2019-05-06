package com.kunfei.bookshelf.presenter;

import android.graphics.Color;

import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.bean.TxtChapterRuleBean;
import com.kunfei.bookshelf.help.DocumentHelper;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.model.TxtChapterRuleManager;
import com.kunfei.bookshelf.presenter.contract.TxtChapterRuleContract;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

public class TxtChapterRulePresenter extends BasePresenterImpl<TxtChapterRuleContract.View> implements TxtChapterRuleContract.Presenter {

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void saveData(List<TxtChapterRuleBean> txtChapterRuleBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int i = 0;
            for (TxtChapterRuleBean ruleBean : txtChapterRuleBeans) {
                i++;
                ruleBean.setSerialNumber(i + 1);
            }
            DbHelper.getDaoSession().getTxtChapterRuleBeanDao().insertOrReplaceInTx(txtChapterRuleBeans);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void delData(TxtChapterRuleBean txtChapterRuleBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            TxtChapterRuleManager.del(txtChapterRuleBean);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean replaceRuleBeans) {
                        mView.refresh();
                        mView.getSnackBar(txtChapterRuleBean.getName() + "已删除", Snackbar.LENGTH_LONG)
                                .setAction("恢复", view -> restoreData(txtChapterRuleBean))
                                .setActionTextColor(Color.WHITE)
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void delData(List<TxtChapterRuleBean> txtChapterRuleBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            TxtChapterRuleManager.del(txtChapterRuleBeans);
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.toast("删除成功");
                        mView.refresh();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("删除失败");
                    }
                });
    }

    private void restoreData(TxtChapterRuleBean txtChapterRuleBean) {
        TxtChapterRuleManager.save(txtChapterRuleBean);
        mView.refresh();
    }

    @Override
    public void importDataSLocal(String path) {
        String json;
        DocumentFile file = DocumentFile.fromFile(new File(path));
        json = DocumentHelper.readString(file);
        if (!isEmpty(json)) {
            importDataS(json);
        } else {
            mView.toast("文件读取失败");
        }
    }

    @Override
    public void importDataS(String text) {
        Observable<Boolean> observable = ReplaceRuleManager.importReplaceRule(text);
        if (observable != null) {
            observable.subscribe(new MyObserver<Boolean>() {
                @Override
                public void onNext(Boolean aBoolean) {
                    mView.refresh();
                    mView.toast("导入成功");
                }

                @Override
                public void onError(Throwable e) {
                    mView.toast("格式不对");
                }
            });
        } else {
            mView.toast("导入失败");
        }
    }
}
