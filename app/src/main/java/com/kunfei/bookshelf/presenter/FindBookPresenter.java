//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.presenter;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.FindKindBean;
import com.kunfei.bookshelf.bean.FindKindGroupBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.presenter.contract.FindBookContract;
import com.kunfei.bookshelf.utils.ACache;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.ArrayList;
import java.util.List;

import javax.script.SimpleBindings;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

import static com.kunfei.bookshelf.constant.AppConstant.SCRIPT_ENGINE;

public class FindBookPresenter extends BasePresenterImpl<FindBookContract.View> implements FindBookContract.Presenter {
    private Disposable disposable;
    private AnalyzeRule analyzeRule;
    private String findError = "发现规则语法错误";

    @SuppressWarnings("unchecked")
    @Override
    public void initData() {
        if (disposable != null) return;
        ACache aCache = ACache.get(mView.getContext(), "findCache");
        Single.create((SingleOnSubscribe<List<RecyclerViewData>>) e -> {
            List<RecyclerViewData> group = new ArrayList<>();
            boolean showAllFind = MApplication.getConfigPreferences().getBoolean("showAllFind", true);
            List<BookSourceBean> sourceBeans = new ArrayList<>(showAllFind ? BookSourceManager.getAllBookSourceBySerialNumber() : BookSourceManager.getSelectedBookSourceBySerialNumber());
            for (BookSourceBean sourceBean : sourceBeans) {
                try {
                    String[] kindA;
                    String findRule;
                    if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl()) && !sourceBean.containsGroup(findError)) {
                        boolean isJsAndCache = sourceBean.getRuleFindUrl().startsWith("<js>");
                        if (isJsAndCache) {
                            findRule = aCache.getAsString(sourceBean.getBookSourceUrl());
                            if (TextUtils.isEmpty(findRule)) {
                                String jsStr = sourceBean.getRuleFindUrl().substring(4, sourceBean.getRuleFindUrl().lastIndexOf("<"));
                                findRule = evalJS(jsStr, sourceBean.getBookSourceUrl()).toString();
                            } else {
                                isJsAndCache = false;
                            }
                        } else {
                            findRule = sourceBean.getRuleFindUrl();
                        }
                        kindA = findRule.split("(&&|\n)+");
                        List<FindKindBean> children = new ArrayList<>();
                        for (String kindB : kindA) {
                            if (kindB.trim().isEmpty()) continue;
                            String[] kind = kindB.split("::");
                            FindKindBean findKindBean = new FindKindBean();
                            findKindBean.setGroup(sourceBean.getBookSourceName());
                            findKindBean.setTag(sourceBean.getBookSourceUrl());
                            findKindBean.setKindName(kind[0]);
                            findKindBean.setKindUrl(kind[1]);
                            children.add(findKindBean);
                        }
                        FindKindGroupBean groupBean = new FindKindGroupBean();
                        groupBean.setGroupName(sourceBean.getBookSourceName());
                        groupBean.setGroupTag(sourceBean.getBookSourceUrl());
                        group.add(new RecyclerViewData(groupBean, children, false));
                        if (isJsAndCache) {
                            aCache.put(sourceBean.getBookSourceUrl(), findRule);
                        }
                    }
                } catch (Exception exception) {
                    sourceBean.addGroup(findError);
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
                        mView.upData(recyclerViewData);
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

    /**
     * 执行JS
     */
    private Object evalJS(String jsStr, String baseUrl) throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", getAnalyzeRule());
        bindings.put("baseUrl", baseUrl);
        return SCRIPT_ENGINE.eval(jsStr, bindings);
    }

    private AnalyzeRule getAnalyzeRule() {
        if (analyzeRule == null) {
            analyzeRule = new AnalyzeRule(null);
        }
        return analyzeRule;
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
    }

    @Override
    public void detachView() {

    }

}