package com.kunfei.bookshelf.presenter.contract;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.List;

public interface FindBookContract {
    interface Presenter extends IPresenter {

        void initData();

    }

    interface View extends IView {

        void upStyle();

        void updateUI(List<RecyclerViewData> group);

    }
}
