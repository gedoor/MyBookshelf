package com.monke.monkeybook.view.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.BookshelfHelp;
import com.monke.monkeybook.help.media.MediaStoreHelper;
import com.monke.monkeybook.view.adapter.FileSystemAdapter;
import com.monke.monkeybook.widget.itemdecoration.DividerItemDecoration;
import com.monke.monkeybook.widget.refreshview.RefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by newbiechen on 17-5-27.
 * 本地书籍
 */

public class LocalBookFragment extends BaseFileFragment {
    @BindView(R.id.refresh_layout)
    RefreshLayout mRlRefresh;
    @BindView(R.id.local_book_rv_content)
    RecyclerView mRvContent;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_local_book, container, false);
    }

    @Override
    protected void bindView() {
        super.bindView();
        ButterKnife.bind(this, view);
        setUpAdapter();
    }

    private void setUpAdapter() {
        mAdapter = new FileSystemAdapter();
        mRvContent.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvContent.addItemDecoration(new DividerItemDecoration(getContext()));
        mRvContent.setAdapter(mAdapter);
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        mAdapter.setOnItemClickListener(
                (view, pos) -> {
                    //如果是已加载的文件，则点击事件无效。
                    String id = mAdapter.getItem(pos).getAbsolutePath();
                    if (BookshelfHelp.getBook(id) != null) {
                        return;
                    }

                    //点击选中
                    mAdapter.setCheckedItem(pos);

                    //反馈
                    if (mListener != null) {
                        mListener.onItemCheckedChange(mAdapter.getItemIsChecked(pos));
                    }
                }
        );
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        MediaStoreHelper.getAllBookFile(getActivity(),
                (files) -> {
                    if (files.isEmpty()) {
                        mRlRefresh.showEmpty();
                    } else {
                        mAdapter.refreshItems(files);
                        mRlRefresh.showFinish();
                        //反馈
                        if (mListener != null) {
                            mListener.onCategoryChanged();
                        }
                    }
                });
    }

}
