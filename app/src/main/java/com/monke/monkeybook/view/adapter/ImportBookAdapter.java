//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.adapter;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.widget.checkbox.SmoothCheckBox;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ImportBookAdapter extends RecyclerView.Adapter<ImportBookAdapter.Viewholder>{
    private List<FileItem> datas;
    private boolean selectAll = false;

    public interface OnCheckBookListener{
        void checkBook(int count);
    }

    private OnCheckBookListener checkBookListener;
    public ImportBookAdapter(@NonNull OnCheckBookListener checkBookListener){
        datas = new ArrayList<>();
        this.checkBookListener = checkBookListener;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_importbook,parent,false));
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, final int position) {
        holder.tvNmae.setText(datas.get(position).getFile().getName());
        holder.tvSize.setText(convertByte(datas.get(position).getFile().length()));
        holder.tvLoc.setText(datas.get(position).getFile().getAbsolutePath().replace(Environment.getExternalStorageDirectory().getAbsolutePath(),"存储空间"));

        holder.scbSelect.setOnCheckedChangeListener((checkBox, isChecked) -> {
            datas.get(position).setChecked(true);
        });
        if (selectAll) {
            holder.scbSelect.setChecked(true);
        }
        if(canCheck){
            holder.scbSelect.setVisibility(View.VISIBLE);
            holder.llContent.setOnClickListener(v -> holder.scbSelect.setChecked(!holder.scbSelect.isChecked(),true));
        }else{
            holder.scbSelect.setVisibility(View.INVISIBLE);
            holder.llContent.setOnClickListener(null);
        }
    }

    public void addData(File newItem){
        int position = datas.size();
        FileItem fileItem = new FileItem(newItem, false);
        datas.add(fileItem);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, 1);
    }

    private Boolean canCheck = false;
    public void setCanCheck(Boolean canCheck){
        this.canCheck = canCheck;
        notifyDataSetChanged();
    }

    public void selectAll() {
        selectAll = true;
        notifyDataSetChanged();
        selectAll = false;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        LinearLayout llContent;
        TextView tvNmae;
        TextView tvSize;
        TextView tvLoc;
        SmoothCheckBox scbSelect;

        Viewholder(View itemView) {
            super(itemView);
            llContent = itemView.findViewById(R.id.ll_content);
            tvNmae = itemView.findViewById(R.id.tv_name);
            tvSize = itemView.findViewById(R.id.tv_size);
            scbSelect = itemView.findViewById(R.id.scb_select);
            tvLoc = itemView.findViewById(R.id.tv_loc);
        }
    }

    private static String convertByte(long size) {
        DecimalFormat df = new DecimalFormat("###.#");
        float f;
        if (size < 1024) {
            f = size / 1.0f;
            return (df.format(new Float(f).doubleValue()) + "B");
        } else if (size < 1024 * 1024) {
            f = (float) size / (float) 1024;
            return (df.format(new Float(f).doubleValue()) + "KB");
        } else if (size < 1024 * 1024 * 1024) {
            f = (float) size / (float) (1024 * 1024);
            return (df.format(new Float(f).doubleValue()) + "MB");
        } else {
            f = (float) size / (float) (1024 * 1024 * 1024);
            return (df.format(new Float(f).doubleValue()) + "GB");
        }
    }

    public List<File> getSelectDatas() {
        List<File> selectDatas = new ArrayList<>();
        for (FileItem fileItem : datas) {
            if (fileItem.checked) {
                selectDatas.add(fileItem.getFile());
            }
        }
        return selectDatas;
    }

    public class FileItem {
        private File file;
        private boolean checked;

        FileItem(File file, boolean checked) {
            this.file = file;
            this.checked = checked;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }

}
