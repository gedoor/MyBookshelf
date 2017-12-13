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
    private List<File> datas;
    private List<File> selectDatas;

    public interface OnCheckBookListener{
        void checkBook(int count);
    }

    private OnCheckBookListener checkBookListener;
    public ImportBookAdapter(@NonNull OnCheckBookListener checkBookListener){
        datas = new ArrayList<>();
        selectDatas = new ArrayList<>();
        this.checkBookListener = checkBookListener;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_adapter_importbook,parent,false));
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, final int position) {
        holder.tvNmae.setText(datas.get(position).getName());
        holder.tvSize.setText(convertByte(datas.get(position).length()));
        holder.tvLoc.setText(datas.get(position).getAbsolutePath().replace(Environment.getExternalStorageDirectory().getAbsolutePath(),"存储空间"));

        holder.scbSelect.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if(isChecked){
                selectDatas.add(datas.get(position));
            }else{
                selectDatas.remove(datas.get(position));
            }
            checkBookListener.checkBook(selectDatas.size());
        });
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
        datas.add(newItem);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, 1);
    }

    private Boolean canCheck = false;
    public void setCanCheck(Boolean canCheck){
        this.canCheck = canCheck;
        notifyDataSetChanged();
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

        public Viewholder(View itemView) {
            super(itemView);
            llContent = (LinearLayout) itemView.findViewById(R.id.ll_content);
            tvNmae = (TextView) itemView.findViewById(R.id.tv_name);
            tvSize = (TextView) itemView.findViewById(R.id.tv_size);
            scbSelect = (SmoothCheckBox) itemView.findViewById(R.id.scb_select);
            tvLoc = (TextView) itemView.findViewById(R.id.tv_loc);
        }
    }

    public static String convertByte(long size) {
        DecimalFormat df = new DecimalFormat("###.#");
        float f;
        if (size < 1024) {
            f = size / 1.0f;
            return (df.format(new Float(f).doubleValue()) + "B");
        } else if (size < 1024 * 1024) {
            f = (float) ((float) size / (float) 1024);
            return (df.format(new Float(f).doubleValue()) + "KB");
        } else if (size < 1024 * 1024 * 1024) {
            f = (float) ((float) size / (float) (1024 * 1024));
            return (df.format(new Float(f).doubleValue()) + "MB");
        } else {
            f = (float) ((float) size / (float) (1024 * 1024 * 1024));
            return (df.format(new Float(f).doubleValue()) + "GB");
        }
    }

    public List<File> getSelectDatas() {
        return selectDatas;
    }
}
