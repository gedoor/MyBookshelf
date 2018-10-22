package com.monke.monkeybook.presenter;

import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.help.DocumentHelper;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.presenter.contract.ReplaceRuleContract;

import java.io.File;
import java.net.URL;
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

public class ReplaceRulePresenterImpl extends BasePresenterImpl<ReplaceRuleContract.View> implements ReplaceRuleContract.Presenter {
    private BookSourceBean delBookSource;

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void saveData(List<ReplaceRuleBean> replaceRuleBeans) {
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            int i = 0;
            for (ReplaceRuleBean replaceRuleBean : replaceRuleBeans) {
                i++;
                replaceRuleBean.setSerialNumber(i + 1);
            }
            ReplaceRuleManage.addDataS(replaceRuleBeans);
            e.onNext(ReplaceRuleManage.getAll());
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void delData(ReplaceRuleBean replaceRuleBean) {
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            ReplaceRuleManage.delData(replaceRuleBean);
            e.onNext(ReplaceRuleManage.getAll());
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        mView.refresh();
                        mView.getSnackBar(replaceRuleBean.getReplaceSummary() + "已删除", Snackbar.LENGTH_LONG)
                                .setAction("恢复", view -> {
                                    restoreData(replaceRuleBean);
                                })
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
            ReplaceRuleManage.delDataS(replaceRuleBeans);
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
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
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            ReplaceRuleManage.saveData(replaceRuleBean);
            e.onNext(ReplaceRuleManage.getAll());
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        mView.refresh();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void importDataSLocal(String path) {
        String json;
        DocumentFile file = DocumentFile.fromFile(new File(path));
        json = DocumentHelper.readString(file);
        if (!isEmpty(json)) {
            try {
                List<ReplaceRuleBean> dataS = new Gson().fromJson(json, new TypeToken<List<ReplaceRuleBean>>() {
                }.getType());
                ReplaceRuleManage.addDataS(dataS);
                mView.refresh();
                mView.toast("导入成功");
            } catch (Exception e) {
                mView.toast("格式不对");
            }
        } else {
            mView.toast("文件读取失败");
        }
    }

    @Override
    public void importDataS(String sourceUrl) {
        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (Exception e) {
            e.printStackTrace();
            mView.toast("URL格式不对");
            return;
        }
        ReplaceRuleManage.importReplaceRuleFromWww(url)
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            mView.refresh();
                            mView.toast("导入成功");
                        } else {
                            mView.toast("格式不对");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast(e.getMessage());
                    }
                });
    }

}
