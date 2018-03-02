package com.monke.monkeybook.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by GKF on 2018/1/21.
 * get web content
 */

public interface IHttpGetApi {
    @GET
    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36",})
    Observable<String> getWebContent(@Url String url);

    @GET("{path}")
    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36",})
    Observable<Response<String>> searchBook(@Path("path") String path, @QueryMap(encoded = true) Map<String, String> queryMap);

}
