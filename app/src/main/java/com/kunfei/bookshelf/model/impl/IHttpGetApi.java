package com.kunfei.bookshelf.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by GKF on 2018/1/21.
 * get web content
 */

public interface IHttpGetApi {
    @GET
    Observable<Response<String>> getWebContent(@Url String url,
                                               @HeaderMap Map<String, String> headers);

    @GET
    Observable<Response<String>> searchBook(@Url String url,
                                            @QueryMap(encoded = true) Map<String, String> queryMap,
                                            @HeaderMap Map<String, String> headers);

    @GET
    Call<String> getWebContentCall(@Url String url,
                                   @HeaderMap Map<String, String> headers);
}
