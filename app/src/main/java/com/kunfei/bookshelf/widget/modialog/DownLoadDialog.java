package com.kunfei.bookshelf.widget.modialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kunfei.bookshelf.R;

public class DownLoadDialog extends BaseDialog {
    private Context context;
    private EditText edtStart;
    private EditText edtEnd;
    private TextView tvCancel;
    private TextView tvDownload;

    public static DownLoadDialog builder(Context context, int startIndex, int endIndex, final int all) {
        return new DownLoadDialog(context, startIndex, endIndex, all);
    }

    private DownLoadDialog(Context context, int startIndex, int endIndex, final int all) {
        super(context, R.style.alertDialogTheme);
        this.context = context;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_download_choice, null);
        bindView(view, startIndex, endIndex, all);
        setContentView(view);
    }

    private void bindView(View view, int startIndex, int endIndex, final int all) {
        View llContent = view.findViewById(R.id.ll_content);
        llContent.setOnClickListener(null);
        edtStart = view.findViewById(R.id.edt_start);
        edtEnd = view.findViewById(R.id.edt_end);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvDownload = view.findViewById(R.id.tv_download);
        edtStart.setText(String.valueOf(startIndex + 1));
        edtEnd.setText(String.valueOf(endIndex + 1));
        edtStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtStart.getText().length() > 0) {
                    try {
                        int temp = Integer.parseInt(edtStart.getText().toString().trim());
                        if (temp > all) {
                            edtStart.setText(String.valueOf(all));
                            edtStart.setSelection(edtStart.getText().length());
                            Toast.makeText(context, "超过总章节", Toast.LENGTH_SHORT).show();
                        } else if (temp <= 0) {
                            edtStart.setText(String.valueOf(1));
                            edtStart.setSelection(edtStart.getText().length());
                        }
                    } catch (Exception e) {
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
                if (edtEnd.getText().length() > 0) {
                    try {
                        int temp = Integer.parseInt(edtEnd.getText().toString().trim());
                        if (temp > all) {
                            edtEnd.setText(String.valueOf(all));
                            edtEnd.setSelection(edtEnd.getText().length());
                            Toast.makeText(context, "超过总章节", Toast.LENGTH_SHORT).show();
                        } else if (temp <= 0) {
                            edtEnd.setText(String.valueOf(1));
                            edtEnd.setSelection(edtEnd.getText().length());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        tvCancel.setOnClickListener(v -> dismiss());
    }

    public DownLoadDialog setPositiveButton(Callback callback) {
        tvDownload.setOnClickListener(v -> {
            if (edtStart.getText().length() > 0 && edtEnd.getText().length() > 0) {
                if (Integer.parseInt(edtStart.getText().toString()) > Integer.parseInt(edtEnd.getText().toString())) {
                    Toast.makeText(context, "输入错误", Toast.LENGTH_SHORT).show();
                } else {
                    callback.download(Integer.parseInt(edtStart.getText().toString()) - 1, Integer.parseInt(edtEnd.getText().toString()) - 1);
                }
                dismiss();
            } else {
                Toast.makeText(context, "请输入要离线的章节", Toast.LENGTH_SHORT).show();
            }
        });
        return this;
    }

    public interface Callback {
        void download(int start, int end);
    }

}
