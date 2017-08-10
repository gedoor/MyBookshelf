package com.monke.monkeybook.widget.libraryview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.LibraryKindBookListBean;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.utils.DensityUtil;

import java.util.List;

public class LibraryKindBookListView extends LinearLayout{
    public interface OnItemListener{
        public void onClickMore(String title,String url);
        public void onClickBook(ImageView animView,SearchBookBean searchBookBean);
    }
    public LibraryKindBookListView(Context context) {
        super(context);
        init();
    }

    public LibraryKindBookListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LibraryKindBookListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public LibraryKindBookListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        setOrientation(VERTICAL);
        setVisibility(GONE);
        LayoutInflater.from(getContext()).inflate(R.layout.view_library_hotauthor, this, true);
    }

    public void updateData(List<LibraryKindBookListBean> datas, OnItemListener itemListener){
        removeAllViews();
        if(datas!=null && datas.size()>0){
            setVisibility(VISIBLE);
            for(int i=0;i<datas.size();i++){
                if(i>0){
                    LinearLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,DensityUtil.dp2px(getContext(),1f));
                    layoutParams.setMargins(0,DensityUtil.dp2px(getContext(),5),0,0);
                    View view = new View(getContext());
                    view.setBackgroundColor(getContext().getResources().getColor(R.color.bg_library));
                    view.setLayoutParams(layoutParams);
                    addView(view);
                }
                LibraryKindBookView itemView = new LibraryKindBookView(getContext());
                itemView.updateData(datas.get(i),itemListener);
                addView(itemView);
            }
        }else{
            setVisibility(GONE);
        }
    }
}
