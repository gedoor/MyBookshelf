package com.kunfei.bookshelf.help;

import android.text.TextUtils;
import android.webkit.CookieManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HttpInterceptor implements Interceptor {
    private int maxRetry;//最大重试次数
    private int retryNum = 0;//假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）
    private CookieManager cookieManager = CookieManager.getInstance();

    public HttpInterceptor(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        while (!response.isSuccessful() && retryNum < maxRetry) {
            retryNum++;
            response = chain.proceed(request);
        }
        if (!request.headers("Set-Cookie").isEmpty()) {
            for (String s : request.headers("Set-Cookie")) {
                String[] x = s.split(";");
                for (String y : x) {
                    if (!TextUtils.isEmpty(y)) {
                        cookieManager.setCookie(request.url().toString(), y);
                    }
                }
            }
        }
        return response;
    }
}
