package com.kunfei.bookshelf.model.task;

import com.kunfei.bookshelf.bean.BookSourceBean;

import io.reactivex.Scheduler;

public class CheckSourceTask {

    BookSourceBean sourceBean;
    private Scheduler scheduler;

    public CheckSourceTask(BookSourceBean sourceBean, Scheduler scheduler) {
        this.sourceBean = sourceBean;
        this.scheduler = scheduler;
    }
/*
    private void startCheck() {
        if (!TextUtils.isEmpty(sourceBean.getRuleSearchUrl())) {
            WebBookModel.getInstance().searchBook("我的", 1, sourceBean.getBookSourceUrl())
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(60, TimeUnit.SECONDS)
                    .subscribe(getObserver());
        } else if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl())) {
            Observable.create((ObservableOnSubscribe<String>) emitter -> {
                String kindA[];
                if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl())) {
                    if (sourceBean.getRuleFindUrl().startsWith("<js>")) {
                        String jsStr = sourceBean.getRuleFindUrl().substring(4, sourceBean.getRuleFindUrl().lastIndexOf("<"));
                        Object object = evalJS(jsStr, sourceBean.getBookSourceUrl());
                        kindA = object.toString().split("(&&|\n)+");
                    } else {
                        kindA = sourceBean.getRuleFindUrl().split("(&&|\n)+");
                    }
                    emitter.onNext(kindA[0].split("::")[1]);
                    emitter.onComplete();
                }
            }).flatMap(url -> WebBookModel.getInstance().findBook(url, 1, sourceBean.getBookSourceUrl()))
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(60, TimeUnit.SECONDS)
                    .subscribe(getObserver());
        } else {
            sourceBean.addGroup("失效");
            sourceBean.setSerialNumber(10000 + checkIndex);
            BookSourceManager.addBookSource(sourceBean);
            nextCheck();
        }
    }

    private Observer<List<SearchBookBean>> getObserver() {
        return new Observer<List<SearchBookBean>>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(List<SearchBookBean> value) {
                if (value.isEmpty()) {
                    sourceBean.addGroup("失效");
                    sourceBean.setSerialNumber(10000 + checkIndex);
                    BookSourceManager.addBookSource(sourceBean);
                } else {
                    if (sourceBean.containsGroup("失效")) {
                        sourceBean.removeGroup("失效");
                        BookSourceManager.addBookSource(sourceBean);
                    }
                }
                nextCheck();
            }

            @Override
            public void onError(Throwable e) {
                sourceBean.addGroup("失效");
                sourceBean.setSerialNumber(10000 + checkIndex);
                BookSourceManager.addBookSource(sourceBean);
                nextCheck();
            }

            @Override
            public void onComplete() {
                checkSource = null;
            }
        };
    }

    *//**
     * 执行JS
     *//*
    private Object evalJS(String jsStr, String baseUrl) throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", new AnalyzeRule(null));
        bindings.put("baseUrl", baseUrl);
        return SCRIPT_ENGINE.eval(jsStr, bindings);
    }*/
}
