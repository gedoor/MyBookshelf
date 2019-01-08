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
            String qrcode = URLEncoder.encode("tsx06677nwdk3javroq4ef0", "utf-8");
            final String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/" + qrcode;
            openUri(context, alipayqr);
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
