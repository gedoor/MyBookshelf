//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.help.RxBusTag;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.contract.FindBookContract;
import com.monke.monkeybook.utils.RxUtils;
import com.monke.monkeybook.widget.refreshview.expandablerecyclerview.bean.RecyclerViewData;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

public class FindBookPresenter extends BasePresenterImpl<FindBookContract.View> implements FindBookContract.Presenter {
    private Disposable disposable;

    @Override
    public void initData() {
        if (disposable != null) return;
        Single.create((SingleOnSubscribe<List<RecyclerViewData>>) e -> {
            List<RecyclerViewData> group = new ArrayList<>();
            boolean showAllFind = MApplication.getInstance().getConfigPreferences().getBoolean("showAllFind", true);
            List<BookSourceBean> sourceBeans = new ArrayList<>(showAllFind ? BookSourceManager.getAllBookSource() : BookSourceManager.getSelectedBookSource());
            for (BookSourceBean sourceBean : sourceBeans) {
                try {
                    if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl())) {
                        String kindA[] = sourceBean.getRuleFindUrl().split("(&&|\n)+");
                        List<FindKindBean> children = new ArrayList<>();
                        for (String kindB : kindA) {
                            if (kindB.trim().isEmpty()) continue;
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
                } catch (Exception exception) {
                    sourceBean.addGroup("发现规则语法错误");
                    BookSourceManager.addBookSource(sourceBean);
                }
            }
            e.onSuccess(group);
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<RecyclerViewData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(List<RecyclerViewData> recyclerViewData) {
                        mView.updateUI(recyclerViewData);
                        disposable.dispose();
                        disposable = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        disposable.dispose();
                        disposable = null;
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