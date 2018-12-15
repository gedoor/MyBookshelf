package com.kunfei.bookshelf.presenter.contract;

import android.graphics.Bitmap;
import android.net.Uri;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.bean.BookSourceBean;

public interface SourceEditContract {
    interface Presenter extends IPresenter {

        void saveSource(BookSourceBean bookSource, BookSourceBean bookSourceOld);

        void copySource(BookSourceBean bookSourceBean);

        void pasteSource();

        void setText(String bookSourceStr);

        Bitmap encodeAsBitmap(String str);

        void analyzeBitmap(Uri uri);
    }

    interface View extends IView {

        void setText(BookSourceBean bookSourceBean);

        String getBookSourceStr();

        void saveSuccess();
    }
}
