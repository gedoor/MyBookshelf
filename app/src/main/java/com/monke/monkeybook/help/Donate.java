package com.monke.monkeybook.help;

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
        openAlipayPayPage(context, "tsx06677nwdk3javroq4ef0");
    }

    public static boolean openAlipayPayPage(Context context, String qrcode) {
        try {
            qrcode = URLEncoder.encode(qrcode, "utf-8");
        } catch (Exception e) {
        }
        try {
            final String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/" + qrcode;
            openUri(context, alipayqr);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 发送一个intent
     */
    private static void openUri(Context context, String s) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
        context.startActivity(intent);
    }
}
