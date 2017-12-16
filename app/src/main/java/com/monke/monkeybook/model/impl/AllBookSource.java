package com.monke.monkeybook.model.impl;

import com.monke.monkeybook.model.IStationBookModel;
import com.monke.monkeybook.model.content.FqxswModelImpl;
import com.monke.monkeybook.model.content.GxwztvBookModelImpl;
import com.monke.monkeybook.model.content.LingdiankanshuModelImpl;
import com.monke.monkeybook.model.content.XBQGModelImpl;

/**
 * Created by GKF on 2017/12/15.
 * 所有书源
 */

public class AllBookSource {
    public static AllBookSource getInstance() {
        return new AllBookSource();
    }

    //所有书源TAG
    public static String[] getAllBookSourceTag() {
        return new String[]{
                XBQGModelImpl.TAG,
                LingdiankanshuModelImpl.TAG,
                GxwztvBookModelImpl.TAG
        };
    }

    //所有书源Name
    public static String[] getAllBookSourceName() {
        return new String[]{
                "新笔趣阁",
                "零点看书",
                "梧州中文台"
        };
    }

    //获取book source class
    public static IStationBookModel getBookSourceModel(String tag) {
        if (tag.equals(GxwztvBookModelImpl.TAG)) {
            return GxwztvBookModelImpl.getInstance();
        } else if (tag.equals(LingdiankanshuModelImpl.TAG)) {
            return LingdiankanshuModelImpl.getInstance();
        } else if (tag.equals(XBQGModelImpl.TAG)) {
            return XBQGModelImpl.getInstance();
        } else if (tag.equals(FqxswModelImpl.TAG)) {
            return FqxswModelImpl.getInstance();
        }
        return null;
    }
}
