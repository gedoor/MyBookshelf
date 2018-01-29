package com.monke.monkeybook.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by GKF on 2018/1/29.
 */

public interface IHttpPostApi {

    @FormUrlEncoded()
    @POST
    Observable<String> searchBook(@Url String url, @FieldMap Map<String, String> fieldMap);
}
