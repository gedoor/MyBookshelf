//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.impl.ILibraryPresenter;
import com.monke.monkeybook.view.impl.ILibraryView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LibraryPresenterImpl extends BasePresenterImpl<ILibraryView> implements ILibraryPresenter {

    private final List<FindKindBean> kinds = new ArrayList<>();

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Override
    public void initData() {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            for (BookSourceBean sourceBean : BookSourceManage.getSelectedBookSource()) {
                if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl())) {
                    String kindA[] = sourceBean.getRuleFindUrl().split("&&");
                    for (String kindB : kindA) {
                        String kind[] = kindB.split("::");
                        FindKindBean findKindBean = new FindKindBean(sourceBean.getBookSourceUrl(), kind[0], kind[1]);
                        kinds.add(findKindBean);
                    }
                }
            }
            e.onNext(true);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {
                        //执行刷新界面
                        mView.updateUI(kinds);

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

}