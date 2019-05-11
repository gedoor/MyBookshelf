package com.kunfei.bookshelf.presenter;

import android.graphics.Color;

import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.snackbar.Snackbar;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.base.observer.MySingleObserver;
import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.help.DocumentHelper;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.presenter.contract.ReplaceRuleContract;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

/**
 * Created by GKF on 2017/12/18.
 * 书源管理
 */

public class ReplaceRulePresenter extends BasePresenterImpl<ReplaceRuleContract.View> implements ReplaceRuleContract.Presenter {

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void saveData(List<ReplaceRuleBean> replaceRuleBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            int i = 0;
            for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
                i++;
                replaceRuleBean.setSerialNumber(i + 1);
            }
            ReplaceRuleManager.addDataS(replaceRuleBeans);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void delData(ReplaceRuleBean replaceRuleBean) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            ReplaceRuleManager.delData(replaceRuleBean);
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean replaceRuleBeans) {
                        mView.refresh();
                        mView.getSnackBar(replaceRuleBean.getReplaceSummary() + "已删除", Snackbar.LENGTH_LONG)
                                .setAction("恢复", view -> restoreData(replaceRuleBean))
                                .setActionTextColor(Color.WHITE)
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void delData(List<ReplaceRuleBean> replaceRuleBeans) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            ReplaceRuleManager.delDataS(replaceRuleBeans);
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

    private void restoreData(ReplaceRuleBean replaceRuleBean) {
        ReplaceRuleManager.saveData(replaceRuleBean)
                .subscribe(new MySingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        mView.refresh();
                    }
                });
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
