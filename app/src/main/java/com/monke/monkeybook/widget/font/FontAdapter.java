package com.monke.monkeybook.widget.font;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.monke.monkeybook.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FontAdapter extends Adapter<FontAdapter.MyViewHolder> {
    private List<File> fileList = new ArrayList<>();
    private FontSelector.OnThisListener thisListener;

    FontAdapter(FontSelector.OnThisListener thisListener) {
        this.thisListener = thisListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Typeface typeface = Typeface.createFromFile(fileList.get(position));
        holder.tvFont.setTypeface(typeface);
        holder.tvFont.setText(fileList.get(position).getName());
        holder.tvFont.setOnClickListener(view -> {
            if (thisListener != null) {
                thisListener.setFontPath(fileList.get(position).getAbsolutePath());
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public void upData(File[] files) {
        if (files == null) {
            return;
        }
        fileList.clear();
        for (File file : files) {
            try {
                Typeface.createFromFile(file);
                fileList.add(file);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvFont;

        MyViewHolder(View itemView) {
            super(itemView);
            tvFont = itemView.findViewById(R.id.tv_font);
        }
    }

}
