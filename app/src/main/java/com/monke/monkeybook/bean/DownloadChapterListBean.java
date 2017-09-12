//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载章节列表
 */
public class DownloadChapterListBean implements Parcelable {
    private List<DownloadChapterBean> data;

    public DownloadChapterListBean(List<DownloadChapterBean> result) {
        this.data = result;
    }

    protected DownloadChapterListBean(Parcel in) {
        if(data == null)
            data = new ArrayList<>();
        in.readTypedList(data,DownloadChapterBean.CREATOR);
    }

    public static final Creator<DownloadChapterListBean> CREATOR = new Creator<DownloadChapterListBean>() {
        @Override
        public DownloadChapterListBean createFromParcel(Parcel in) {
            return new DownloadChapterListBean(in);
        }

        @Override
        public DownloadChapterListBean[] newArray(int size) {
            return new DownloadChapterListBean[size];
        }
    };

    public List<DownloadChapterBean> getData() {
        return data;
    }

    public void setData(List<DownloadChapterBean> data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(data);
    }
}
