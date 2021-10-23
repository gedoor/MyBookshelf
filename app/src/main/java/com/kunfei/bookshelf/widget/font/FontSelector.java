package com.kunfei.bookshelf.widget.font;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.FileDoc;
import com.kunfei.bookshelf.utils.theme.ATH;

import java.util.List;

import kotlin.text.Regex;

public class FontSelector {
    private final AlertDialog.Builder builder;
    private final FontAdapter adapter;
    private OnThisListener thisListener;
    private AlertDialog alertDialog;
    public static Regex fontRegex = new Regex("(?i).*\\.[ot]tf");

    public FontSelector(Context context, String selectPath) {
        builder = new AlertDialog.Builder(context, R.style.alertDialogTheme);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.view_recycler_font, null);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        builder.setView(view);
        builder.setTitle(R.string.select_font);
        builder.setNegativeButton(R.string.cancel, null);
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
                    public void setFontPath(FileDoc fileDoc) {
                        if (thisListener != null) {
                            thisListener.setFontPath(fileDoc);
                        }
                        alertDialog.dismiss();
                    }
                });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public FontSelector setListener(OnThisListener thisListener) {
        this.thisListener = thisListener;
        builder.setPositiveButton(R.string.default_font, ((dialogInterface, i) -> thisListener.setDefault()));
        return this;
    }

    public FontSelector create(List<FileDoc> docItems) {
        adapter.upData(docItems);
        builder.create();
        return this;
    }

    public void show() {
        alertDialog = builder.show();
        ATH.setAlertDialogTint(alertDialog);
    }

    public interface OnThisListener {
        void setDefault();

        void setFontPath(FileDoc fileDoc);
    }
}
