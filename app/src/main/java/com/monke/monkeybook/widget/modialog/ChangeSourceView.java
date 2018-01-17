package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.model.SearchBook;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class ChangeSourceView {
    private MoProgressView moProgressView;
    private SearchBook searchBook;
    private Context context;

    public static ChangeSourceView getInstance(MoProgressView moProgressView) {
        return new ChangeSourceView(moProgressView);
    }

    private ChangeSourceView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
    }

    void showChangeSource(String bookName, String bookAuthor, final MoProgressHUD.OnClickSource clickSource, View.OnClickListener cancel){
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_change_source, moProgressView, true);
        TextView atvTitle = moProgressView.findViewById(R.id.atv_title);
        atvTitle.setText(String.format("%s(%s)", bookName, bookAuthor));
        ImageButton ibRefrish = moProgressView.findViewById(R.id.iv_refresh);
        ibRefrish.setOnClickListener(view -> {});
        ListView lvSource = moProgressView.findViewById(R.id.lv_source);


    }

}
