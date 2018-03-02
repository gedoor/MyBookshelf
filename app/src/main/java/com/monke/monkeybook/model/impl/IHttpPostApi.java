package com.monke.monkeybook.model.impl;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * Created by GKF on 2018/1/29.
 * post
 */

public interface IHttpPostApi {

    @FormUrlEncoded
    @POST("{path}")
    @Headers({"User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36",})
    Observable<Response<String>> searchBook(@Path("path") String path, @FieldMap(encoded = true) Map<String, String> fieldMap);
}
