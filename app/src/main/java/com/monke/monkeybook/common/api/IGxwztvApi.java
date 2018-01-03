//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.common.api;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IGxwztvApi {

    @GET
    Observable<String> getBookInfo(@Url String url);

    @GET("/search.htm")
    Observable<String> searchBook(@Query("keyword")String content, @Query("pn")int page);

    @GET
    Observable<String> getBookContent(@Url String url);

    @GET
    Observable<String> getChapterList(@Url String url);

    @GET
    Observable<String> getKindBooks(@Url String url);

    @GET
    Observable<String> getLibraryData(@Url String url);
}
