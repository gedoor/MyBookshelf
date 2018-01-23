package com.monke.monkeybook.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by GKF on 2018/1/21.
 * get web content
 */

public interface IGetWebApi {
    @GET
    Observable<String> getWebContent(@Url String url);

    @GET()
    Observable<String> searchBook(@Url String url, @QueryMap Map<String, String> queryMap);
}
