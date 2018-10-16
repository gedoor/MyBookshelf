package com.monke.monkeybook.presenter.contract;

import android.content.SharedPreferences;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.BookShelfBean;

import java.util.List;

public interface MainContract {

    interface View extends IView {

        void initImmersionBar();

        /**
         * 取消弹出框
         */
        void dismissHUD();

        /**
         * 刷新错误
         * @param error 错误
         */
        void refreshError(String error);

        /**
         * 显示等待框
         */
        void showLoading(String msg);

        /**
         * 恢复数据
         */
        void onRestore(String msg);

        SharedPreferences getPreferences();

        void recreate();
    }

    interface Presenter extends IPresenter {

        void backupData();

        void restoreData();

        void addBookUrl(String bookUrl);

        void clearBookshelf();
    }

}
