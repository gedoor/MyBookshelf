package com.kunfei.bookshelf.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

public class NetWorkStateReceiver extends BroadcastReceiver {

    private static class InstanceHolder {
        private static final NetWorkStateReceiver INSTANCE = new NetWorkStateReceiver();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WebService.stopThis(context);
        ShareService.stopThis(context);
    }

    /**
     * 注册网络监听
     */
    public static NetWorkStateReceiver registerReceiver(@NonNull Context context) {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        NetWorkStateReceiver netWorkStateReceiver = new NetWorkStateReceiver();
        context.registerReceiver(netWorkStateReceiver, intentFilter);
        return netWorkStateReceiver;
    }

    /**
     * 取消网络监听
     */
    public static void unregisterReceiver(@NonNull Context context, NetWorkStateReceiver netWorkStateReceiver) {
        context.unregisterReceiver(netWorkStateReceiver);
    }
}
