package com.kunfei.bookshelf.model.content;

import android.annotation.SuppressLint;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.utils.TimeUtils;
import com.kunfei.bookshelf.view.activity.SourceDebugActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class Debug {
    @SuppressLint("ConstantLocale")
    public static final DateFormat DEBUG_TIME_FORMAT = new SimpleDateFormat("mm:ss.SSS", Locale.getDefault());

    static void printLog(String tag, String msg) {
        if (Objects.equals(SourceDebugActivity.DEBUG_TAG, tag)) {
            msg = String.format("%s %s", TimeUtils.getNowString(DEBUG_TIME_FORMAT), msg);
            RxBus.get().post(RxBusTag.PRINT_DEBUG_LOG, msg);
        }
    }
}
