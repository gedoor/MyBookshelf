package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;
import com.monke.monkeybook.model.SearchBook;

import java.util.List;

import butterknife.BindView;
import me.grantland.widget.AutofitTextView;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class ChangeSourceView {
    private TextView atvTitle;
    private ImageButton ibRefrish;
    private ListView lvSource;

    private MoProgressView moProgressView;
    private SearchBook searchBook;
    private Context context;

    public static ChangeSourceView getInstance(MoProgressView moProgressView) {
        return new ChangeSourceView(moProgressView);
    }

    private ChangeSourceView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();

        searchBook = new SearchBook(new SearchBook.OnSearchListener() {
            @Override
            public void refreshSearchBook(List<SearchBookBean> value) {

            }

            @Override
            public void refreshFinish(Boolean value) {

            }

            @Override
            public void loadMoreFinish(Boolean value) {

            }

            @Override
            public Boolean checkIsExist(SearchBookBean value) {
                return null;
            }

            @Override
            public void loadMoreSearchBook(List<SearchBookBean> value) {

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

    void showChangeSource(String bookName, String bookAuthor, final MoProgressHUD.OnClickSource clickSource, View.OnClickListener cancel) {


        atvTitle.setText(String.format("%s(%s)", bookName, bookAuthor));

        ibRefrish.setOnClickListener(view -> {
        });



    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_change_source, moProgressView, true);

        atvTitle = moProgressView.findViewById(R.id.atv_title);
        ibRefrish = moProgressView.findViewById(R.id.iv_refresh);
        lvSource = moProgressView.findViewById(R.id.lv_source);
    }
}
