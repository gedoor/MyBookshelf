package com.kunfei.bookshelf.presenter.contract;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.bean.FindKindGroupBean;

import java.util.List;

public interface FindBookContract {
    interface Presenter extends IPresenter {

        void initData();

    }

    interface View extends IView {

        /**
         * 更新UI
         */
        void updateUI(List<FindKindGroupBean> group);

    }
}
