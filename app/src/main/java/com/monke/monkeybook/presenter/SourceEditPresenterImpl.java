package com.monke.monkeybook.presenter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.gson.Gson;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.listener.OnObservableListener;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.impl.ISourceEditPresenter;
import com.monke.monkeybook.view.impl.ISourceEditView;

/**
 * Created by GKF on 2018/1/28.
 * 编辑书源
 */

public class SourceEditPresenterImpl extends BasePresenterImpl<ISourceEditView> implements ISourceEditPresenter {

    @Override
    public void copySource(BookSourceBean bookSourceBean) {
        Gson gson = new Gson();
        String bs = gson.toJson(bookSourceBean);
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, bs);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
        }
    }

    @Override
    public void pasteSource() {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard != null ? clipboard.getPrimaryClip() : null;
        if (clipData != null && clipData.getItemCount() > 0) {
            try {
                Gson gson = new Gson();
                BookSourceBean bookSourceBean = gson.fromJson(String.valueOf(clipData.getItemAt(0).getText()), BookSourceBean.class);
                mView.setText(bookSourceBean);
            } catch (Exception e) {
                Toast.makeText(mView.getContext(), "数据格式不对", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
    }

    @Override
    public void detachView() {

    }
}
