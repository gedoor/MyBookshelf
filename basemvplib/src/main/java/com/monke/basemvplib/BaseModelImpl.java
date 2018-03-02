package com.monke.basemvplib;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class BaseModelImpl {
    private static OkHttpClient.Builder clientBuilder;

    protected Retrofit getRetrofitString(Context mContext, String url) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create())
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClientBuilder(mContext).build())
                .build();
    }

    private OkHttpClient.Builder getClientBuilder(Context mContext) {
        if (clientBuilder == null) {
            clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.createTrustAllManager())
                    .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                    .retryOnConnectionFailure(true)
                    .addInterceptor(getHeaderInterceptor())
                    .addInterceptor(new RetryInterceptor(1));
        }
        return clientBuilder;
    }

    private Interceptor getHeaderInterceptor() {
        return new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Keep-Alive", "300")
                        .addHeader("Connection", "Keep-Alive")
                        .addHeader("Cache-Control", "no-cache")
                        .build();
                return chain.proceed(request);
            }
        };
    }
}