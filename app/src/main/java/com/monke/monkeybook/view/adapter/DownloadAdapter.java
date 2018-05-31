package com.monke.monkeybook.view.adapter;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.DownloadBookBean;
import com.monke.monkeybook.view.activity.DownloadActivity;

import java.util.ArrayList;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.MyViewHolder> {
    private DownloadActivity activity;
    private List<DownloadBookBean> dataS;

    public DownloadAdapter(DownloadActivity activity) {
        this.activity = activity;
        dataS = new ArrayList<>();
    }

    public void upDataS(List<DownloadBookBean> dataS) {
        this.dataS = dataS;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_download_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.ivDel.getDrawable().mutate();
        holder.ivDel.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        Glide.with(activity)
                .load(dataS.get(position).getCoverUrl())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                        .dontAnimate().placeholder(R.drawable.img_cover_default))
                .into(holder.ivCover);
        holder.tvName.setText(dataS.get(position).getName());
        holder.tvDownload.setText(String.format(activity.getString(R.string.un_download), dataS.get(position).getDownload()));
        holder.ivDel.setOnClickListener(view -> activity.delDownload(dataS.get(position).getNoteUrl()));
    }


    @Override
    public int getItemCount() {
        return dataS.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName;
        TextView tvDownload;
        ImageView ivDel;

        MyViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDownload = itemView.findViewById(R.id.tv_download);
            ivDel = itemView.findViewById(R.id.iv_delete);
        }
    }
}
