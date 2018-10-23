package com.monke.monkeybook.help;

import android.support.annotation.StringDef;

import com.monke.monkeybook.MApplication;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by newbiechen on 17-4-16.
 */

public class Constant {

    //Book Date Convert Format
    public static final String FORMAT_BOOK_DATE = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_FILE_DATE = "yyyy-MM-dd";
    //RxBus
    public static final int MSG_SELECTOR = 1;
    public static final String[] BOOK_GROUPS = new String[]{"追更区", "养肥区", "完结区"};
    //BookCachePath (因为getCachePath引用了Context，所以必须是静态变量，不能够是静态常量)
    public static String BOOK_CACHE_PATH = MApplication.downloadPath + File.separator + "book_cache" + File.separator;
    //文件阅读记录保存的路径
    public static String BOOK_RECORD_PATH = FileHelp.getCachePath() + File.separator
            + "book_record" + File.separator;
    public static Map<String, String> bookType = new HashMap<String, String>() {{
        put("qt", "其他");
        put(BookType.XHQH, "玄幻奇幻");
        put(BookType.WXXX, "武侠仙侠");
        put(BookType.DSYN, "都市异能");
        put(BookType.LSJS, "历史军事");
        put(BookType.YXJJ, "游戏竞技");
        put(BookType.KHLY, "科幻灵异");
        put(BookType.CYJK, "穿越架空");
        put(BookType.HMZC, "豪门总裁");
        put(BookType.XDYQ, "现代言情");
        put(BookType.GDYQ, "古代言情");
        put(BookType.HXYQ, "幻想言情");
        put(BookType.DMTR, "耽美同人");
    }};

    //BookType
    @StringDef({
            BookType.ALL,
            BookType.XHQH,
            BookType.WXXX,
            BookType.DSYN,
            BookType.LSJS,
            BookType.YXJJ,
            BookType.KHLY,
            BookType.CYJK,
            BookType.HMZC,
            BookType.XDYQ,
            BookType.GDYQ,
            BookType.HXYQ,
            BookType.DMTR
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface BookType {
        String ALL = "all";

        String XHQH = "xhqh";

        String WXXX = "wxxx";

        String DSYN = "dsyn";

        String LSJS = "lsjs";

        String YXJJ = "yxjj";
        String KHLY = "khly";
        String CYJK = "cyjk";
        String HMZC = "hmzc";
        String XDYQ = "xdyq";
        String GDYQ = "gdyq";
        String HXYQ = "hxyq";
        String DMTR = "dmtr";
    }
}
