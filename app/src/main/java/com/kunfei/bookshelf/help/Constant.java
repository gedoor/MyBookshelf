package com.kunfei.bookshelf.help;

import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.MApplication;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by newbiechen on 17-4-16.
 */

public class Constant {

    //Book Date Convert Format
    public static final String FORMAT_BOOK_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";
    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = MApplication.downloadPath + File.separator + "book_cache" + File.separator;

    public static Type MAP_STRING = new TypeToken<Map<String, String>>() {
    }.getType();

}
