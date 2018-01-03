//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.common.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IZwduApi {

    @GET
    Observable<String> getBookInfo(@Url String url);

    @GET("/search.php")
    Observable<String> searchBook(@Query("keyword") String content, @Query("page") int page);

    @GET
    Observable<String> getBookContent(@Url String url);

    @GET
    Observable<String> getChapterList(@Url String url);

}
