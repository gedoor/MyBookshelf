package com.kunfei.bookshelf.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by GKF on 2018/1/29.
 * post
 */

public interface IHttpPostApi {

    @FormUrlEncoded
    @POST
    Observable<Response<String>> searchBook(@Url String url, @FieldMap(encoded = true) Map<String, String> fieldMap, @HeaderMap Map<String, String> headers);
}
