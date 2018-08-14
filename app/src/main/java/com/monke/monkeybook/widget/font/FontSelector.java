package com.monke.monkeybook.widget.font;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.FileUtil;

import java.io.File;

public class FontSelector {
    private AlertDialog.Builder builder;
    private RecyclerView recyclerView;
    private FontAdapter adapter;
    private Context context;
    private String fontPath;

    public FontSelector(Context context) {
        builder = new AlertDialog.Builder(context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.view_recycler_font, null);
        recyclerView = view.findViewById(R.id.recycler_view);
        builder.setView(view);
        builder.setTitle("选择字体");
        fontPath = FileUtil.getSdCardPath() + "/Fonts";
        adapter = new FontAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
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
        builder.show();
    }

    private File[] getFontFiles() {
        File file = new File(fontPath);
        return file.listFiles(pathName -> pathName.getName().endsWith(".TTF") || pathName.getName().endsWith(".ttf"));
    }
}
