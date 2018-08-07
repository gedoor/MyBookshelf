package com.monke.monkeybook.presenter.contract;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.bean.FindKindGroupBean;

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
