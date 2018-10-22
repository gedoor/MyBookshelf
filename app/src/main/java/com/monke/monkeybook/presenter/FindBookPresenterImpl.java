//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.contract.FindBookContract;
import com.monke.monkeybook.widget.refreshview.expandablerecyclerview.bean.RecyclerViewData;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FindBookPresenterImpl extends BasePresenterImpl<FindBookContract.View> implements FindBookContract.Presenter {

    @Override
    public void initData() {
        Observable.create((ObservableOnSubscribe<List<RecyclerViewData>>) e -> {
            List<RecyclerViewData> group = new ArrayList<>();
            boolean showAllFind = MApplication.getInstance().getConfigPreferences().getBoolean("showAllFind", true);
            List<BookSourceBean> sourceBeans = showAllFind ? BookSourceManage.getAllBookSource() : BookSourceManage.getSelectedBookSource();
            for (BookSourceBean sourceBean : sourceBeans) {
                if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl())) {
                    String kindA[] = sourceBean.getRuleFindUrl().split("&&");
                    List<FindKindBean> children = new ArrayList<>();
                    for (String kindB : kindA) {
                        String kind[] = kindB.split("::");
                        FindKindBean findKindBean = new FindKindBean();
                        findKindBean.setGroup(sourceBean.getBookSourceName());
                        findKindBean.setTag(sourceBean.getBookSourceUrl());
                        findKindBean.setKindName(kind[0]);
                        findKindBean.setKindUrl(kind[1]);
                        children.add(findKindBean);
                    }
                    FindKindGroupBean groupBean = new FindKindGroupBean();
                    groupBean.setGroupName(sourceBean.getBookSourceName());
                    groupBean.setChildrenCount(kindA.length);
                    group.add(new RecyclerViewData(groupBean, children, false));
                }
            }
            e.onNext(group);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<RecyclerViewData>>() {
                    @Override
                    public void onNext(List<RecyclerViewData> value) {
                        //执行刷新界面
                        mView.updateUI(value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD,
            tags = {@Tag(RxBusTag.UPDATE_BOOK_SOURCE)})
    public void hadAddOrRemoveBook(Object object) {
        initData();
    }
}