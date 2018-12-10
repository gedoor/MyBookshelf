//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf.base.observer;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class SimpleObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onComplete() {

    }
}
