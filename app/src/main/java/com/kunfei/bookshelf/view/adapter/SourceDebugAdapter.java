package com.kunfei.bookshelf.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.kunfei.bookshelf.R;

import java.util.ArrayList;
import java.util.List;

public class SourceDebugAdapter extends Adapter<SourceDebugAdapter.MyViewHolder> {
    private Context context;
    private List<String> data = new ArrayList<>();

    public SourceDebugAdapter(Context context) {
        this.context = context;
    }

    public void clearData() {
        data.clear();
        notifyDataSetChanged();
    }

    public void addData(String msg) {
        data.add(msg);
        notifyItemChanged(data.size() - 1);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source_debug, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (holder.textView.getTag(R.id.tag1) == null) {
            View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    holder.textView.setCursorVisible(false);
                    holder.textView.setCursorVisible(true);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            };
            holder.textView.addOnAttachStateChangeListener(listener);
            holder.textView.setTag(R.id.tag1, listener);
        }
        holder.textView.setText(data.get(position));
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv);
        }
    }
}

