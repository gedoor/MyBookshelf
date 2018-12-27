package com.kunfei.bookshelf.widget.font;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kunfei.bookshelf.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.MyViewHolder> {
    private List<File> fileList = new ArrayList<>();
    private FontSelector.OnThisListener thisListener;
    private Context context;
    private String selectPath;

    FontAdapter(Context context, String selectPath, FontSelector.OnThisListener thisListener) {
        this.context = context;
        this.selectPath = selectPath;
        this.thisListener = thisListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (fileList.size() > 0) {
            Typeface typeface = Typeface.createFromFile(fileList.get(position));
            holder.tvFont.setTypeface(typeface);
            holder.tvFont.setText(fileList.get(position).getName());
            if (fileList.get(position).getAbsolutePath().equals(selectPath)) {
                holder.ivChecked.setVisibility(View.VISIBLE);
            } else {
                holder.ivChecked.setVisibility(View.INVISIBLE);
            }
            holder.tvFont.setOnClickListener(view -> {
                if (thisListener != null) {
                    thisListener.setFontPath(fileList.get(position).getAbsolutePath());
                }
            });
        } else {
            holder.tvFont.setText(R.string.fonts_folder);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size() == 0 ? 1 : fileList.size();
    }

    public void upData(File[] files) {
        if (files != null) {
            fileList.clear();
            for (File file : files) {
                try {
                    Typeface.createFromFile(file);
                    fileList.add(file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvFont;
        ImageView ivChecked;

        MyViewHolder(View itemView) {
            super(itemView);
            tvFont = itemView.findViewById(R.id.tv_font);
            ivChecked = itemView.findViewById(R.id.iv_checked);
        }
    }

}
