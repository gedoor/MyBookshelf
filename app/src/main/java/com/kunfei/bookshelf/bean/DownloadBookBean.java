package com.kunfei.bookshelf.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import io.reactivex.annotations.Nullable;

public class DownloadBookBean  implements Parcelable, Comparable<DownloadBookBean> {
    private String name; //小说名
    private String noteUrl;
    private String coverUrl;
    private int downloadCount;
    private int start;
    private int end;
    private int successCount;
    private boolean isValid;
    private long finalDate;

    public DownloadBookBean() {
    }

    public DownloadBookBean(String name, String noteUrl, String coverUrl, int start, int end) {
        this.name = name;
        this.noteUrl = noteUrl;
        this.coverUrl = coverUrl;
        this.start = start;
        this.end = end;
    }

    protected DownloadBookBean(Parcel in) {
        name = in.readString();
        noteUrl = in.readString();
        coverUrl = in.readString();
        downloadCount = in.readInt();
        start = in.readInt();
        end = in.readInt();
        successCount = in.readInt();
        isValid = in.readByte() != 0;
        finalDate = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(noteUrl);
        dest.writeString(coverUrl);
        dest.writeInt(downloadCount);
        dest.writeInt(start);
        dest.writeInt(end);
        dest.writeInt(successCount);
        dest.writeByte((byte) (isValid ? 1 : 0));
        dest.writeLong(finalDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<DownloadBookBean> CREATOR = new Parcelable.Creator<DownloadBookBean>() {
        @Override
        public DownloadBookBean createFromParcel(Parcel in) {
            return new DownloadBookBean(in);
        }

        @Override
        public DownloadBookBean[] newArray(int size) {
            return new DownloadBookBean[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
        setValid(downloadCount > 0);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getWaitingCount(){
        return this.downloadCount - this.successCount;
    }

    public synchronized void successCountAdd() {
        if(this.successCount < this.downloadCount) {
            this.successCount += 1;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public long getFinalDate() {
        return finalDate;
    }

    public void setFinalDate(long finalDate) {
        this.finalDate = finalDate;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof DownloadBookBean) {
            return TextUtils.equals(((DownloadBookBean) obj).getNoteUrl(), this.noteUrl);
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(DownloadBookBean o) {
        return (int) (this.finalDate - o.finalDate);
    }
}
