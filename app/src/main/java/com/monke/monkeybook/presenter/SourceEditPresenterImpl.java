package com.monke.monkeybook.presenter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.listener.OnObservableListener;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.impl.ISourceEditPresenter;
import com.monke.monkeybook.view.impl.ISourceEditView;

import java.util.Hashtable;

/**
 * Created by GKF on 2018/1/28.
 * 编辑书源
 */

public class SourceEditPresenterImpl extends BasePresenterImpl<ISourceEditView> implements ISourceEditPresenter {

    @Override
    public void copySource(BookSourceBean bookSourceBean) {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, mView.getBookSourceStr());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
        }
    }

    @Override
    public void pasteSource() {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard != null ? clipboard.getPrimaryClip() : null;
        if (clipData != null && clipData.getItemCount() > 0) {
            setText(String.valueOf(clipData.getItemAt(0).getText()));
        }
    }

    @Override
    public void setText(String bookSourceStr) {
        try {
            Gson gson = new Gson();
            BookSourceBean bookSourceBean = gson.fromJson(bookSourceStr, BookSourceBean.class);
            mView.setText(bookSourceBean);
        } catch (Exception e) {
            Toast.makeText(mView.getContext(), "数据格式不对", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Bitmap encodeAsBitmap(String str) {
        Bitmap bitmap = null;
        BitMatrix result;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            Hashtable<EncodeHintType, Object> hst = new Hashtable();
            hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 600, 600,hst);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) { // ?
            return null;
        }
        return bitmap;
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
    }

    @Override
    public void detachView() {

    }
}
