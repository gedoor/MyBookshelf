package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.monke.monkeybook.R;

/**
 * Created by GKF on 2018/1/17.
 * 换源
 */

public class EditBookUrlView {
    private EditText etUrl;
    private TextView tvOk;

    private MoProgressHUD moProgressHUD;
    private MoProgressView moProgressView;
    private OnPutBookUrl onPutBookUrl;
    private Context context;

    public static EditBookUrlView getInstance(MoProgressView moProgressView) {
        return new EditBookUrlView(moProgressView);
    }

    private EditBookUrlView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
        tvOk.setOnClickListener(view -> {
            onPutBookUrl.addBookUrl(etUrl.getText().toString());
            moProgressHUD.dismiss();
        });
    }

    void showEditBookUrl(final OnPutBookUrl onPutBookUrl, MoProgressHUD moProgressHUD) {
        this.moProgressHUD = moProgressHUD;
        this.onPutBookUrl = onPutBookUrl;

    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_edit_book_url, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        etUrl = moProgressView.findViewById(R.id.et_book_url);
        tvOk = moProgressView.findViewById(R.id.tv_ok);
    }

    /**
     * 输入book地址确定
     */
    public interface OnPutBookUrl {
        void addBookUrl(String bookUrl);
    }
}
