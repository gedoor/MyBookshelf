package com.monke.monkeybook.presenter;

import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.FileHelper;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.presenter.impl.IReplaceRulePresenter;
import com.monke.monkeybook.view.impl.IReplaceRuleView;

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

public class ReplaceRulePresenterImpl extends BasePresenterImpl<IReplaceRuleView> implements IReplaceRulePresenter {
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
                        Snackbar.make(mView.getView(), replaceRuleBean.getReplaceSummary() + "已删除", Snackbar.LENGTH_LONG)
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
                        Toast.makeText(mView.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                        mView.refresh();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), "删除失败", Toast.LENGTH_SHORT).show();
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
    public void importDataS(Uri uri) {
        String json;
        if (uri.toString().startsWith("content://")) {
            json = FileHelper.readString(uri);
        } else {
            String path = uri.getPath();
            DocumentFile file = DocumentFile.fromFile(new File(path));
            json = FileHelper.readString(file);
        }
        if (!isEmpty(json)) {
            try {
                List<ReplaceRuleBean> dataS = new Gson().fromJson(json, new TypeToken<List<ReplaceRuleBean>>() {
                }.getType());
                ReplaceRuleManage.addDataS(dataS);
                mView.refresh();
                Toast.makeText(mView.getContext(), "导入成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(mView.getContext(), "格式不对", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mView.getContext(), "文件读取失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void importDataS(String sourceUrl) {
        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mView.getContext(), "URL格式不对", Toast.LENGTH_SHORT).show();
            return;
        }
        ReplaceRuleManage.importReplaceRuleFromWww(url)
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            mView.refresh();
                            Toast.makeText(mView.getContext(), "导入成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mView.getContext(), "格式不对", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
