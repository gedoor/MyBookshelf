package com.monke.monkeybook.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by GKF on 2018/1/21.
 * get web content
 */

public interface IHttpGetApi {
    @GET
    Observable<String> getWebContent(@Url String url,
                                     @Header("User-Agent") String userAgent,
                                     @HeaderMap Map<String, String> headers);

    @GET("{path}")
    Observable<Response<String>> searchBook(@Path("path") String path,
                                            @QueryMap(encoded = true) Map<String, String> queryMap,
                                            @HeaderMap Map<String, String> headers);

}
