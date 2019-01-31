package com.kunfei.bookshelf.help;

import android.text.TextUtils;
import android.webkit.CookieManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private int maxRetry;//最大重试次数
    private int retryNum = 0;//假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）

    public RetryInterceptor(int maxRetry) {
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
            final StringBuilder cookieBuffer = new StringBuilder();
            for (String s : request.headers("Set-Cookie")) {
                String[] x = s.split(";");
                for (String y : x) {
                    if (!TextUtils.isEmpty(y)) {
                        cookieBuffer.append(s).append(";");
                    }
                }
            }
            CookieManager.getInstance().setCookie(request.url().host(), cookieBuffer.toString());
        }
        return response;
    }
}
