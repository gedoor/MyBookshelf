package com.kunfei.bookshelf.help;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.net.URLEncoder;

/**
 * Created by GKF on 2017/12/18.
 * 捐赠
 */

public class Donate {

    public static void aliDonate(Context context) {
        try {
            String qrCode = URLEncoder.encode("https://qr.alipay.com/tsx06677nwdk3javroq4ef0?_s=web-other", "utf-8");
            String aliPayQr = "alipayqr://platformapi/startapp?saId=10000007&qrcode=" + qrCode + "&_t=" + System.currentTimeMillis();
            openUri(context, aliPayQr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送一个intent
     */
    private static void openUri(Context context, String s) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
        context.startActivity(intent);
    }
}
