package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
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

public class EditBookUrlView {
    private EditText etUrl;
    private TextView tvCancel;
    private TextView tvDone;
    private RotateAnimation rotateAnimation;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private MoProgressHUD.OnPutBookUrl onPutBookUrl;
    private Context context;
    private ChangeSourceAdapter adapter;
    private SearchBook searchBook;
    private List<BookShelfBean> bookShelfS = new ArrayList<>();
    private String bookName;
    private String bookAuthor;

    public static EditBookUrlView getInstance(MoProgressView moProgressView) {
        return new EditBookUrlView(moProgressView);
    }

    private EditBookUrlView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();

    }

    void showEditBookUrl(final MoProgressHUD.OnPutBookUrl onPutBookUrl, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.onPutBookUrl = onPutBookUrl;


    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_edit_book_url, moProgressView, true);

        etUrl = moProgressView.findViewById(R.id.et_book_url);
        tvCancel = moProgressView.findViewById(R.id.tv_cancel);
        tvDone = moProgressView.findViewById(R.id.tv_done);

    }


}
