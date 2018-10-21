package com.monke.monkeybook.widget.font;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.DocumentHelper;
import com.monke.monkeybook.utils.FileUtil;

import java.io.File;

public class FontSelector {
    private AlertDialog.Builder builder;
    private FontAdapter adapter;
    private Context context;
    private String fontPath;
    private OnThisListener thisListener;
    private AlertDialog alertDialog;

    public FontSelector(Context context, String selectPath) {
        builder = new AlertDialog.Builder(context, R.style.alertDialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.view_recycler_font, null);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        builder.setView(view);
        builder.setTitle(R.string.select_font);
        builder.setNegativeButton(R.string.cancel, null);
        fontPath = FileUtil.getSdCardPath() + "/Fonts";
        adapter = new FontAdapter(context, selectPath,
                new OnThisListener() {
                    @Override
                    public void setDefault() {
                        if (thisListener != null) {
                            thisListener.setDefault();
                        }
                        alertDialog.dismiss();
                    }

                    @Override
                    public void setFontPath(String fontPath) {
                        if (thisListener != null) {
                            thisListener.setFontPath(fontPath);
                        }
                        alertDialog.dismiss();
                    }
                });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public FontSelector setListener(OnThisListener thisListener) {
        this.thisListener = thisListener;
        builder.setPositiveButton(R.string.default_font, ((dialogInterface, i) -> {
            thisListener.setDefault();
        }));
        return this;
    }

    public FontSelector setPath(String path) {
        fontPath = path;
        return this;
    }

    public FontSelector create() {
        adapter.upData(getFontFiles());
        builder.create();
        return this;
    }

    public void show() {
        alertDialog = builder.show();
    }

    private File[] getFontFiles() {
        try {
            DocumentHelper.createDirIfNotExist(fontPath);
            File file = new File(fontPath);
            return file.listFiles(pathName -> pathName.getName().toLowerCase().matches(".*\\.[ot]tf"));
        } catch (Exception e) {
            return null;
        }
    }

    public interface OnThisListener {
        void setDefault();

        void setFontPath(String fontPath);
    }
}
