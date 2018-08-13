package com.monke.monkeybook.widget.font;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.monke.monkeybook.R;

public class FontSelector {
    private AlertDialog.Builder builder;
    private RecyclerView recyclerView;

    public FontSelector(Context context) {
        builder = new AlertDialog.Builder(context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.view_recycler, null);
        recyclerView = view.findViewById(R.id.recycler_view);
        builder.setView(view);
    }



}
