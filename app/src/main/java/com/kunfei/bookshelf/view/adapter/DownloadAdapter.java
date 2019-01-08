package com.kunfei.bookshelf.view.adapter;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.DownloadBookBean;
import com.kunfei.bookshelf.service.DownloadService;
import com.kunfei.bookshelf.view.activity.DownloadActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.MyViewHolder> {
    private DownloadActivity activity;
    private List<DownloadBookBean> dataS;
    private final Object mLock = new Object();

    public DownloadAdapter(DownloadActivity activity) {
        this.activity = activity;
        dataS = new ArrayList<>();
    }


    public void upDataS(List<DownloadBookBean> dataS) {
        synchronized (mLock) {
            this.dataS.clear();
            if (dataS != null) {
                this.dataS.addAll(dataS);
                Collections.sort(this.dataS);
            }
        }
        if(dataS != null) {
            notifyDataSetChanged();
        }
    }

    public void upData(DownloadBookBean data) {
        int index = -1;
        synchronized (mLock) {
            if (data != null && !this.dataS.isEmpty()) {
                index = this.dataS.indexOf(data);
                if(index >= 0) {
                    this.dataS.set(index, data);
                }
            }
        }
        if(index >= 0){
            notifyItemChanged(index, data.getWaitingCount());
        }
    }

    public void removeData(DownloadBookBean data) {
        int index = -1;
        synchronized (mLock) {
            if (data != null && !this.dataS.isEmpty()) {
                index = this.dataS.indexOf(data);
                if (index >= 0) {
                    this.dataS.remove(index);
                }
            }
        }
        if(index >=0){
            notifyItemRemoved(index);
        }
    }

    public void addData(DownloadBookBean data){
        synchronized (mLock) {
            if (data != null) {
                this.dataS.add(data);
            }
        }
        if(data != null){
            notifyItemInserted(this.dataS.size() - 1);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull List<Object> payloads) {
        final DownloadBookBean item = dataS.get(holder.getLayoutPosition());
        if (!payloads.isEmpty()) {
            holder.tvName.setText(String.format(Locale.getDefault(), "%s(正在下载)", item.getName()));
            holder.tvDownload.setText(activity.getString(R.string.un_download, (Integer) payloads.get(0)));
        }else {
            holder.ivDel.getDrawable().mutate();
            holder.ivDel.getDrawable().setColorFilter(activity.getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
            Glide.with(activity)
                    .load(item.getCoverUrl())
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop()
                            .dontAnimate().placeholder(R.drawable.img_cover_default)
                            .error(R.drawable.img_cover_default))
                    .into(holder.ivCover);
            if (item.getSuccessCount() > 0) {
                holder.tvName.setText(String.format(Locale.getDefault(), "%s(正在下载)", item.getName()));
            } else {
                holder.tvName.setText(String.format(Locale.getDefault(), "%s(等待下载)", item.getName()));
            }
            holder.tvDownload.setText(activity.getString(R.string.un_download, item.getDownloadCount() - item.getSuccessCount()));
            holder.ivDel.setOnClickListener(view -> DownloadService.removeDownload(activity, item.getNoteUrl()));
        }
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
