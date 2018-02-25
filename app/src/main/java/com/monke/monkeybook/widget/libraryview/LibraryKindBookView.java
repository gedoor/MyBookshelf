package com.monke.monkeybook.widget.libraryview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.LibraryKindBookListBean;

public class LibraryKindBookView extends LinearLayout{
    private TextView tvKindName;
    private TextView tvMore;
    private RecyclerView rvBookLIst;
    private LibraryKindBookAdapter libraryKindBookAdapter;
    public LibraryKindBookView(Context context) {
        super(context);
        init();
    }

    public LibraryKindBookView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LibraryKindBookView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public LibraryKindBookView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_library_kindbook, this, true);
        tvKindName = (TextView) findViewById(R.id.tv_kindname);
        tvMore = (TextView) findViewById(R.id.tv_more);
        rvBookLIst = (RecyclerView) findViewById(R.id.rv_booklist);
        libraryKindBookAdapter = new LibraryKindBookAdapter();
        rvBookLIst.setAdapter(libraryKindBookAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvBookLIst.setLayoutManager(linearLayoutManager);

        setVisibility(GONE);
    }


    public void updateData(final LibraryKindBookListBean data, final LibraryKindBookListView.OnItemListener itemListener){
        updateData(data,itemListener,data.getKindUrl()==null?false:true);
    }
    public void updateData(final LibraryKindBookListBean data, final LibraryKindBookListView.OnItemListener itemListener,Boolean hasMore){
        if(data.getBooks()==null || data.getBooks().size()==0){
            setVisibility(GONE);
        }else
            setVisibility(VISIBLE);
        tvKindName.setText(data.getKindName());
        if(hasMore){
            tvMore.setVisibility(VISIBLE);
            tvMore.setOnClickListener(v -> {
                if(itemListener!=null)
                    itemListener.onClickMore(data.getKindName(),data.getKindUrl());
            });
        }else{
            tvMore.setVisibility(GONE);
            tvMore.setOnClickListener(null);
        }
        libraryKindBookAdapter.setItemListener(itemListener);
        libraryKindBookAdapter.updateDataAll(data.getBooks());
    }
}
