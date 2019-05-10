package com.kunfei.bookshelf.web.controller;

import android.os.AsyncTask;
import android.os.Handler;

import com.google.gson.Gson;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.model.content.Debug;
import com.kunfei.bookshelf.utils.StringUtils;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import io.reactivex.disposables.CompositeDisposable;

import static com.kunfei.bookshelf.constant.AppConstant.MAP_STRING;

public class SourceDebugWebSocket extends NanoWSD.WebSocket {
    private CompositeDisposable compositeDisposable;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                send(new byte[]{});
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.postDelayed(this, 10000);
        }
    };

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
        if (!StringUtils.isJsonType(message.getTextPayload())) return;
        Map<String, String> debugBean = new Gson().fromJson(message.getTextPayload(), MAP_STRING);
        String tag = debugBean.get("tag");
        String key = debugBean.get("key");
        Debug.newDebug(tag, key, compositeDisposable, new Debug.Callback() {
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
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false);
                    } catch (IOException ignored) {
                    }
                    Debug.SOURCE_DEBUG_TAG = null;
                });
            }

            @Override
            public void finish() {
                AsyncTask.execute(() -> {
                    try {
                        send("finish");
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false);
                    } catch (IOException ignored) {
                    }
                    Debug.SOURCE_DEBUG_TAG = null;
                });
            }
        });
        handler.postDelayed(runnable, 10000);
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
