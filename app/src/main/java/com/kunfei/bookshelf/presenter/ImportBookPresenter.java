//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.presenter;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.BaseActivity;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.LocBookShelfBean;
import com.kunfei.bookshelf.help.RxBusTag;
import com.kunfei.bookshelf.model.ImportBookModel;
import com.kunfei.bookshelf.presenter.contract.ImportBookContract;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ImportBookPresenter extends BasePresenterImpl<ImportBookContract.View> implements ImportBookContract.Presenter {

    @Override
    public void importBooks(List<File> books) {
        Observable.fromIterable(books)
                .flatMap(file -> ImportBookModel.getInstance().importBook(file))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((BaseActivity) mView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new SimpleObserver<LocBookShelfBean>() {
                    @Override
                    public void onNext(LocBookShelfBean value) {
                        if (value.getNew()) {
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, value.getBookShelfBean());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.addError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mView.addSuccess();
                    }
                });
    }


    @Override
    public void detachView() {

    }
}
