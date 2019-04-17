package com.kunfei.bookshelf.model;

import android.util.Log;

import com.kunfei.bookshelf.utils.NetworkUtil;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.util.concurrent.TimeUnit;

public class WebServerManager {
    private Server server;

    /**
     * Create server.
     */
    public WebServerManager(Server.ServerListener listener) {
        server = AndServer.serverBuilder()
                .inetAddress(NetworkUtil.getLocalIPAddress())
                .port(1122)
                .timeout(10, TimeUnit.SECONDS)
                .listener(listener)
                .build();
    }

    public Server getServer() {
        return server;
    }

    /**
     * Start server.
     */
    public void startServer() {
        if (server.isRunning()) {
            // TODO The server is already up.
        } else {
            server.startup();
        }
    }

    /**
     * Stop server.
     */
    public void stopServer() {
        if (server.isRunning()) {
            server.shutdown();
        } else {
            Log.w("AndServer", "The server has not started yet.");
        }
    }

}
