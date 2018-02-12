//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.presenter;

import android.os.Handler;

import com.monke.basemvplib.BasePresenterImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.LibraryBean;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.model.content.GxwztvBookModelImpl;
import com.monke.monkeybook.presenter.impl.ILibraryPresenter;
import com.monke.monkeybook.view.impl.ILibraryView;

import java.util.LinkedHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LibraryPresenterImpl extends BasePresenterImpl<ILibraryView> implements ILibraryPresenter {
    public final static String LIBRARY_CACHE_KEY = "cache_library";
    private ACache mCache;
    private Boolean isFirst = true;

    private final LinkedHashMap<String,String> kinds = new LinkedHashMap<>();

    public LibraryPresenterImpl() {
        kinds.put("东方玄幻","http://www.gxwztv.com/xuanhuanxiaoshuo/");
        kinds.put("西方奇幻","http://www.gxwztv.com/qihuanxiaoshuo/");
        kinds.put("热血修真","http://www.gxwztv.com/xiuzhenxiaoshuo/");
        kinds.put("武侠仙侠","http://www.gxwztv.com/wuxiaxiaoshuo/");
        kinds.put("都市爽文","http://www.gxwztv.com/dushixiaoshuo/");
        kinds.put("言情暧昧","http://www.gxwztv.com/yanqingxiaoshuo/");
        kinds.put("灵异悬疑","http://www.gxwztv.com/lingyixiaoshuo/");
        kinds.put("运动竞技","http://www.gxwztv.com/jingjixiaoshuo/");
        kinds.put("历史架空","http://www.gxwztv.com/lishixiaoshuo/");
        kinds.put("审美","http://www.gxwztv.com/danmeixiaoshuo/");
        kinds.put("科幻迷航","http://www.gxwztv.com/kehuanxiaoshuo/");
        kinds.put("游戏人生","http://www.gxwztv.com/youxixiaoshuo/");
        kinds.put("军事斗争","http://www.gxwztv.com/junshixiaoshuo/");
        kinds.put("商战人生","http://www.gxwztv.com/shangzhanxiaoshuo/");
        kinds.put("校园爱情","http://www.gxwztv.com/xiaoyuanxiaoshuo/");
        kinds.put("官场仕途","http://www.gxwztv.com/guanchangxiaoshuo/");
        kinds.put("娱乐明星","http://www.gxwztv.com/zhichangxiaoshuo/");
        kinds.put("其他","http://www.gxwztv.com/qitaxiaoshuo/");

        mCache = ACache.get(MApplication.getInstance());
    }

    @Override
    public void detachView() {

    }

    @Override
    public void getLibraryData() {
        if (isFirst) {
            isFirst = false;
            Observable.create((ObservableOnSubscribe<String>) e -> {
                String cache = mCache.getAsString(LIBRARY_CACHE_KEY);
                e.onNext(cache);
                e.onComplete();
            }).flatMap(s -> GxwztvBookModelImpl.getInstance().analyzeLibraryData(s))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<LibraryBean>() {
                        @Override
                        public void onNext(LibraryBean value) {
                            //执行刷新界面
                            mView.updateUI(value);
                            getLibraryNewData();
                        }

                        @Override
                        public void onError(Throwable e) {
                            getLibraryNewData();
                        }
                    });
        }else{
            getLibraryNewData();
        }
    }

    private void getLibraryNewData() {
        GxwztvBookModelImpl.getInstance().getLibraryData(mCache).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<LibraryBean>() {
                    @Override
                    public void onNext(final LibraryBean value) {
                        new Handler().postDelayed(() -> {
                            mView.updateUI(value);
                            mView.finishRefresh();
                        },1000);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.finishRefresh();
                    }
                });
    }

    @Override
    public LinkedHashMap<String, String> getKinds() {
        return kinds;
    }
}