package com.monke.monkeybook.widget.libraryview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.SearchBookBean;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

public class LibraryKindBookAdapter extends RecyclerView.Adapter<LibraryKindBookAdapter.Viewholder>{
    private LibraryKindBookListView.OnItemListener itemListener;

    private List<SearchBookBean> datas = new ArrayList<>();

    public LibraryKindBookAdapter(){

    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int i) {
        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_library_kindbook, parent, false));
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, final int position) {
        Glide.with(holder.ivCover.getContext())
                .load(datas.get(position).getCoverUrl())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .fitCenter()
                .dontAnimate()
                .placeholder(R.drawable.img_cover_default)
                .into(holder.ivCover);
        holder.tvName.setText(datas.get(position).getName());
        holder.tvAuthor.setText(datas.get(position).getAuthor());
        holder.ibContent.setOnClickListener(v -> {
            if (itemListener != null)
                itemListener.onClickBook(holder.ivCover,datas.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        AutofitTextView tvName;
        AutofitTextView tvAuthor;
        ImageButton ibContent;
        public Viewholder(View itemView) {
            super(itemView);
            ivCover= (ImageView) itemView.findViewById(R.id.iv_cover);
            tvName = (AutofitTextView) itemView.findViewById(R.id.tv_name);
            tvAuthor = (AutofitTextView) itemView.findViewById(R.id.tv_author);
            ibContent = (ImageButton) itemView.findViewById(R.id.ib_content);
        }
    }

    public void setItemListener(LibraryKindBookListView.OnItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public void updateDataAll(List<SearchBookBean> newDatas){
        datas.clear();
        if(newDatas!=null && newDatas.size()>0)
            datas.addAll(newDatas);
        notifyDataSetChanged();
    }
}
