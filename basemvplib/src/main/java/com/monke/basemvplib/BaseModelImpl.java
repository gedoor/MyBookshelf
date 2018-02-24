package com.monke.basemvplib;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class BaseModelImpl {
    private static OkHttpClient.Builder clientBuilder;

    private OkHttpClient.Builder getClientBuilder(Context mContext) {
        if (clientBuilder == null) {
            clientBuilder = new OkHttpClient.Builder()
                    .cache(new Cache(new File(mContext.getExternalCacheDir(), "http_cache"), 10 * 1024 * 1024))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addInterceptor(new RetryIntercepter(3));
        }
        return clientBuilder;
    }

    protected Retrofit getRetrofitString(Context mContext, String url) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create())
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClientBuilder(mContext).build())
                .build();
    }

}