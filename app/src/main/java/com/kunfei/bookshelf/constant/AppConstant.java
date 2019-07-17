package com.kunfei.bookshelf.constant;

import com.google.gson.reflect.TypeToken;
import com.kunfei.bookshelf.BuildConfig;
import com.kunfei.bookshelf.MApplication;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import okhttp3.MediaType;

public class AppConstant {

    public static final String ActionStartService = "startService";
    public static final String ActionDoneService = "doneService";

    public static final long TIME_OUT = BuildConfig.DEBUG ? 600 : 180;

    //Book Date Convert Format
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";
    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = MApplication.downloadPath + File.separator + "book_cache" + File.separator;

    public static Type MAP_STRING = new TypeToken<Map<String, String>>() {
    }.getType();

    public static final String DEFAULT_WEB_DAV_URL = "https://dav.jianguoyun.com/dav/";

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36";

    public static final Pattern JS_PATTERN = Pattern.compile("(<js>[\\w\\W]*?</js>|@js:[\\w\\W]*$)", Pattern.CASE_INSENSITIVE);
    public static final Pattern EXP_PATTERN = Pattern.compile("\\{\\{([\\w\\W]*?)\\}\\}");

    public static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");

    public static final MediaType jsonMediaType = MediaType.parse("Content-Type, application/json");
}
