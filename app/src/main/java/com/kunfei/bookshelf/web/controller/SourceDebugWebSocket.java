package com.kunfei.bookshelf.web.controller;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.kunfei.bookshelf.constant.RxBusTag;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

public class SourceDebugWebSocket extends NanoWSD.WebSocket {

    public SourceDebugWebSocket(NanoHTTPD.IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

    @Override
    protected void onOpen() {
        RxBus.get().register(this);
    }

    @Override
    protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        RxBus.get().unregister(this);
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {

    }

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {

    }

    @Override
    protected void onException(IOException exception) {

    }

    @Subscribe(tags = {@Tag(RxBusTag.PRINT_DEBUG_LOG)})
    public void printDebugLog(String msg) {

    }

}
