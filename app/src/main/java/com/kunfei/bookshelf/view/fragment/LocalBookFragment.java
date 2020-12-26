package com.kunfei.bookshelf.view.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.databinding.FragmentLocalBookBinding;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.media.MediaStoreHelper;
import com.kunfei.bookshelf.view.adapter.FileSystemAdapter;
import com.kunfei.bookshelf.widget.itemdecoration.DividerItemDecoration;

/**
 * Created by newbiechen on 17-5-27.
 * 本地书籍
 */

public class LocalBookFragment extends BaseFileFragment {

    private FragmentLocalBookBinding binding;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentLocalBookBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void bindView() {
        super.bindView();
        setUpAdapter();
    }

    private void setUpAdapter() {
        mAdapter = new FileSystemAdapter();
        if (getContext() != null) {
            binding.localBookRvContent.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.localBookRvContent.addItemDecoration(new DividerItemDecoration(getContext()));
            binding.localBookRvContent.setAdapter(mAdapter);
        }
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
        if (getActivity() != null) {
            MediaStoreHelper.getAllBookFile(getActivity(),
                    (files) -> {
                        if (files.isEmpty()) {
                            binding.refreshLayout.showEmpty();
                        } else {
                            mAdapter.refreshItems(files);
                            binding.refreshLayout.showFinish();
                            //反馈
                            if (mListener != null) {
                                mListener.onCategoryChanged();
                            }
                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
