package com.monke.monkeybook.model;

import android.app.Activity;

/**
 * 更新换源列表里最新章节
 */
public class UpLastChapterModel {
    private static UpLastChapterModel model;
    private Activity activity;

    public static UpLastChapterModel getInstance() {
        if (model == null) {
            model = new UpLastChapterModel();
        }
        return model;
    }

    private void startUpdate(Activity activity) {
        this.activity = activity;


    }


}
