package com.kunfei.bookshelf.web.controller;

import android.os.AsyncTask;
import android.webkit.CookieSyncManager;

import com.google.gson.Gson;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.model.content.Debug;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import io.reactivex.disposables.CompositeDisposable;

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
    }

    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        RxBus.get().unregister(this);
        compositeDisposable.dispose();
        Debug.SOURCE_DEBUG_TAG = null;
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        Map<String, String> debugBean = new Gson().fromJson(message.getTextPayload(), MAP_STRING);
        String tag = debugBean.get("tag");
        String key = debugBean.get("key");
        Debug.newDebug(tag, key, compositeDisposable, new Debug.CallBack() {
            @Override
            public void printLog(String msg) {
                AsyncTask.execute(() -> {
                    try {
                        send(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void printError(String msg) {
                AsyncTask.execute(() -> {
                    try {
                        send(msg);
                    } catch (IOException ignored) {
                    }
                    Debug.SOURCE_DEBUG_TAG = null;
                });
            }

            @Override
            public void finish() {
                AsyncTask.execute(() -> {
                    try {
                        close(NanoWSD.WebSocketFrame.CloseCode.valueOf("finish"), "finish", true);
                    } catch (IOException ignored) {
                    }
                    Debug.SOURCE_DEBUG_TAG = null;
                });
            }
        });
    }

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException exception) {
        Debug.SOURCE_DEBUG_TAG = null;
    }

    @Subscribe(thread = EventThread.IO, tags = {@Tag(RxBusTag.PRINT_DEBUG_LOG)})
    public void printDebugLog(String msg) {
        try {
            send(msg);
        } catch (IOException e) {
            Debug.SOURCE_DEBUG_TAG = null;
        }
    }

}
