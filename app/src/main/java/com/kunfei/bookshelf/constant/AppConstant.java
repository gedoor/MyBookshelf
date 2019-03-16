package com.kunfei.bookshelf.constant;

import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.MApplication;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

public class AppConstant {

    //Book Date Convert Format
    public static final String FORMAT_BOOK_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";
    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = MApplication.downloadPath + File.separator + "book_cache" + File.separator;

    public static Type MAP_STRING = new TypeToken<Map<String, String>>() {
    }.getType();

    public static final String DEFAULT_WEB_DAV_URL = "https://dav.jianguoyun.com/dav/";

    public static final Pattern JS_PATTERN = Pattern.compile("(<js>[\\w\\W]*?</js>|@js:[\\w\\W]*$)", Pattern.CASE_INSENSITIVE);
}
