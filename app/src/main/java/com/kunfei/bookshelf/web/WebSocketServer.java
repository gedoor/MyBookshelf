package com.kunfei.bookshelf.web;

import com.kunfei.bookshelf.web.controller.SourceDebugWebSocket;

import java.io.IOException;

import fi.iki.elonen.NanoWSD;

public class WebSocketServer extends NanoWSD {

    private SourceDebugWebSocket debugWebSocket;

    public WebSocketServer(int port) {
        super(port);
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        if (handshake.getUri().equals("/sourceDebug")) {
            if (debugWebSocket != null) {
                try {
                    debugWebSocket.close(WebSocketFrame.CloseCode.valueOf("finish"), "finish", true);
                } catch (IOException ignored) {
                }
            }
            debugWebSocket = new SourceDebugWebSocket(handshake);
            return debugWebSocket;
        }
        return null;
    }
}
