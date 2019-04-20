package com.kunfei.bookshelf.web;

import com.kunfei.bookshelf.web.controller.SourceDebugWebSocket;

import fi.iki.elonen.NanoWSD;

public class WebSocketServer extends NanoWSD {

    public WebSocketServer(int port) {
        super(port);
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new SourceDebugWebSocket(handshake);
    }
}
