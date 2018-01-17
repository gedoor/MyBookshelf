package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.SearchBook;
import com.monke.monkeybook.view.adapter.ChangeSourceAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class ChangeSourceView {
    private TextView atvTitle;
    private ImageButton ibRefresh;
    private RecyclerView rvSource;

    private MoProgressView moProgressView;
    private Context context;
    private ChangeSourceAdapter adapter;
    private SearchBook searchBook;
    private List<BookShelfBean> bookShelfS = new ArrayList<>();
    private String bookName;
    private String bookAuthor;

    public static ChangeSourceView getInstance(MoProgressView moProgressView) {
        return new ChangeSourceView(moProgressView);
    }

    private ChangeSourceView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
        adapter = new ChangeSourceAdapter();
        rvSource.setLayoutManager(new LinearLayoutManager(context));
        rvSource.setAdapter(adapter);
        searchBook = new SearchBook(new SearchBook.OnSearchListener() {
            @Override
            public void refreshSearchBook(List<SearchBookBean> value) {
                adapter.addSourceAdapter(value, bookName);
            }

            @Override
            public void refreshFinish(Boolean value) {

            }

            @Override
            public void loadMoreFinish(Boolean value) {

            }

            @Override
            public Boolean checkIsExist(SearchBookBean value) {
                return false;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {
                adapter.addSourceAdapter(value, bookName);
            }

            @Override
            public void searchBookError(Boolean value) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
    }

    void showChangeSource(BookShelfBean bookShelf, final MoProgressHUD.OnClickSource clickSource, View.OnClickListener cancel) {
        bookShelfS.add(bookShelf);
        bookName = bookShelf.getBookInfoBean().getName();
        bookAuthor = bookShelf.getBookInfoBean().getAuthor();
        atvTitle.setText(String.format("%s(%s)", bookName, bookAuthor));
        ibRefresh.setOnClickListener(view -> {
        });
        long startThisSearchTime = System.currentTimeMillis();
        searchBook.setSearchTime(startThisSearchTime);
        searchBook.searchReNew();
        searchBook.search(bookShelf.getBookInfoBean().getName(), startThisSearchTime, bookShelfS, false);
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_change_source, moProgressView, true);

        atvTitle = moProgressView.findViewById(R.id.atv_title);
        ibRefresh = moProgressView.findViewById(R.id.iv_refresh);
        rvSource = moProgressView.findViewById(R.id.rv_book_source_list);
    }
}
