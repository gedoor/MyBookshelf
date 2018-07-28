package com.monke.monkeybook.utils;

import com.example.newbiechen.ireader.model.bean.CommentBean;
import com.example.newbiechen.ireader.model.bean.DetailBean;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by newbiechen on 17-4-29.
 */

public class RxUtils {

    public static <T> SingleSource<T> toSimpleSingle(Single<T> upstream){
        return upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> ObservableSource<T> toSimpleSingle(Observable<T> upstream){
        return upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T,R> TwoTuple<T,R> twoTuple(T first,R second){
        return new TwoTuple<T, R>(first, second);
    }

    public static <T> Single<DetailBean<T>> toCommentDetail(Single<T> detailSingle,
                                                            Single<List<CommentBean>> bestCommentsSingle,
                                                            Single<List<CommentBean>> commentsSingle){
        return Single.zip(detailSingle, bestCommentsSingle, commentsSingle,
                new Function3<T, List<CommentBean>, List<CommentBean>, DetailBean<T>>() {
                    @Override
                    public DetailBean<T> apply(T t, List<CommentBean> commentBeen,
                                               List<CommentBean> commentBeen2) throws Exception {
                        return new DetailBean<T>(t,commentBeen,commentBeen2);
                    }
                });
    }

    public static class TwoTuple<A, B> {
        public final A first;
        public final B second;

        public TwoTuple(A a, B b) {
            this.first = a;
            this.second = b;
        }
    }
}
