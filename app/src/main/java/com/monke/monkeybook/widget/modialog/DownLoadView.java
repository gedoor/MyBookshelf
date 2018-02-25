package com.monke.monkeybook.widget.modialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.R;

/**
 * Created by GKF on 2018/1/17.
 * 离线下载
 */

class DownLoadView {
    private MoProgressView moProgressView;
    private Context context;

    private EditText edtStart;
    private EditText edtEnd;
    private TextView tvCancel;
    private TextView tvDownload;

    public static DownLoadView getInstance(MoProgressView moProgressView) {
        return new DownLoadView(moProgressView);
    }

    private DownLoadView(MoProgressView moProgressView) {
        this.moProgressView = moProgressView;
        this.context = moProgressView.getContext();
        bindView();
    }

    void showDownloadList(int startIndex, int endIndex, final int all, final OnClickDownload clickDownload, View.OnClickListener cancel){
        tvCancel.setOnClickListener(cancel);
        edtStart.setText(String.valueOf(startIndex+1));
        edtEnd.setText(String.valueOf(endIndex+1));
        edtStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(edtStart.getText().length()>0){
                    try{
                        int temp = Integer.parseInt(edtStart.getText().toString().trim());
                        if(temp>all){
                            edtStart.setText(String.valueOf(all));
                            edtStart.setSelection(edtStart.getText().length());
                            Toast.makeText(context,"超过总章节",Toast.LENGTH_SHORT).show();
                        }else if(temp<=0){
                            edtStart.setText(String.valueOf(1));
                            edtStart.setSelection(edtStart.getText().length());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        edtEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(edtEnd.getText().length()>0){
                    try{
                        int temp = Integer.parseInt(edtEnd.getText().toString().trim());
                        if(temp>all){
                            edtEnd.setText(String.valueOf(all));
                            edtEnd.setSelection(edtEnd.getText().length());
                            Toast.makeText(context,"超过总章节",Toast.LENGTH_SHORT).show();
                        }else if(temp<=0){
                            edtEnd.setText(String.valueOf(1));
                            edtEnd.setSelection(edtEnd.getText().length());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        tvDownload.setOnClickListener(v -> {
            if(edtStart.getText().length()>0 && edtEnd.getText().length()>0){
                if(Integer.parseInt(edtStart.getText().toString())>Integer.parseInt(edtEnd.getText().toString())){
                    Toast.makeText(context,"输入错误",Toast.LENGTH_SHORT).show();
                }else{
                    if(clickDownload!=null){
                        clickDownload.download(Integer.parseInt(edtStart.getText().toString())-1,Integer.parseInt(edtEnd.getText().toString())-1);
                    }
                }
            }else{
                Toast.makeText(context,"请输入要离线的章节",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindView() {
        moProgressView.removeAllViews();
        LayoutInflater.from(context).inflate(R.layout.moprogress_dialog_download_choice, moProgressView, true);

        View llContent = moProgressView.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        edtStart = moProgressView.findViewById(R.id.edt_start);
        edtEnd = moProgressView.findViewById(R.id.edt_end);
        tvCancel = moProgressView.findViewById(R.id.tv_cancel);
        tvDownload = moProgressView.findViewById(R.id.tv_download);
    }


    /**
     * 离线下载确定
     */
    public interface OnClickDownload {
        void download(int start, int end);
    }
}
