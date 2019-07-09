package com.kunfei.bookshelf.model.content;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;

public class VipThrowable extends Throwable {

    private final static String tag = "VIP_THROWABLE";

    VipThrowable() {
        super(MApplication.getInstance().getString(R.string.donate_s));
    }
}
