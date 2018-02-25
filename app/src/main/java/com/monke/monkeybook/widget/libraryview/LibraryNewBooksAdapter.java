package com.monke.monkeybook.widget.libraryview;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.LibraryNewBookBean;
import com.monke.monkeybook.widget.flowlayout.FlowLayout;
import com.monke.monkeybook.widget.flowlayout.TagAdapter;

import java.util.ArrayList;

public class LibraryNewBooksAdapter extends TagAdapter<LibraryNewBookBean> {
    private LibraryNewBooksView.OnClickAuthorListener clickNewBookListener;

    public LibraryNewBooksAdapter() {
        super(new ArrayList<LibraryNewBookBean>());
    }

    @Override
    public View getView(FlowLayout parent, int position, final LibraryNewBookBean libraryNewBookBean) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_library_hotauthor_item,
                parent, false);
        tv.setText(libraryNewBookBean.getName());
        tv.setOnClickListener(v -> {
            if(null != clickNewBookListener){
                clickNewBookListener.clickNewBook(libraryNewBookBean);
            }
        });
        return tv;
    }

    public LibraryNewBookBean getItemData(int position){
        return mTagDatas.get(position);
    }

    public int getDataSize(){
        return mTagDatas.size();
    }

    public void setClickNewBookListener(LibraryNewBooksView.OnClickAuthorListener clickNewBookListener) {
        this.clickNewBookListener = clickNewBookListener;
    }
}
