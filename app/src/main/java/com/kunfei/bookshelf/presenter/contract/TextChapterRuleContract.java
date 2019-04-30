package com.kunfei.bookshelf.presenter.contract;

import com.google.android.material.snackbar.Snackbar;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;

public interface TextChapterRuleContract {
    interface Presenter extends IPresenter {

        void importDataSLocal(String uri);

        void importDataS(String text);
    }

    interface View extends IView {

        void refresh();

        Snackbar getSnackBar(String msg, int length);

    }
}
