package com.kunfei.bookshelf.web.controller;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.model.content.Debug;
import com.kunfei.bookshelf.utils.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.kunfei.bookshelf.constant.AppConstant.MAP_STRING;

public class SourceDebugWebSocket extends NanoWSD.WebSocket {
    private CompositeDisposable compositeDisposable;

    public SourceDebugWebSocket(NanoHTTPD.IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

    @Override
    protected void onOpen() {
        RxBus.get().register(this);
        compositeDisposable = new CompositeDisposable();
        Observable.interval(10, 10, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribe(new MyObserver<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        try {
                            ping(new byte[]{aLong.byteValue()});
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        RxBus.get().unregister(this);
        compositeDisposable.dispose();
        Debug.SOURCE_DEBUG_TAG = null;
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        if (!StringUtils.isJsonType(message.getTextPayload())) return;
        Map<String, String> debugBean = new Gson().fromJson(message.getTextPayload(), MAP_STRING);
        String tag = debugBean.get("tag");
        String key = debugBean.get("key");
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(key)) {
            try {
                send(MApplication.getInstance().getString(R.string.cannot_empty));
                close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false);
            } catch (IOException ignored) {
            }
            return;
        }
        Debug.newDebug(tag, key, compositeDisposable);
    }

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException exception) {
        Debug.SOURCE_DEBUG_TAG = null;
    }

    @Subscribe(thread = EventThread.EXECUTOR, tags = {@Tag(RxBusTag.PRINT_DEBUG_LOG)})
    public void printDebugLog(String msg) {
        try {
            send(msg);
            if (msg.equals("finish")) {
                close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false);
                Debug.SOURCE_DEBUG_TAG = null;
            }
        } catch (IOException e) {
            Debug.SOURCE_DEBUG_TAG = null;
        }

    }

}
