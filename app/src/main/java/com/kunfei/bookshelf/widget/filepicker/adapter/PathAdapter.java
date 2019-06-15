package com.kunfei.bookshelf.widget.filepicker.adapter;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.widget.filepicker.icons.FilePickerIcon;
import com.kunfei.bookshelf.widget.filepicker.util.ConvertUtils;

import java.util.Collections;
import java.util.LinkedList;


public class PathAdapter extends RecyclerView.Adapter<PathAdapter.MyViewHolder> {
    private static final String ROOT_HINT = "SD";
    private LinkedList<String> paths = new LinkedList<>();
    private Drawable arrowIcon = null;
    private String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    private CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public String getItem(int position) {
        StringBuilder tmp = new StringBuilder(sdCardDirectory + "/");
        //忽略根目录
        if (position == 0) {
            return tmp.toString();
        }
        for (int i = 1; i <= position; i++) {
            tmp.append(paths.get(i)).append("/");
        }
        return tmp.toString();
    }

    public void setArrowIcon(Drawable arrowIcon) {
        this.arrowIcon = arrowIcon;
    }

    public void updatePath(String path) {
        path = path.replace(sdCardDirectory, "");
        if (arrowIcon == null) {
            arrowIcon = ConvertUtils.toDrawable(FilePickerIcon.getARROW());
        }
        paths.clear();
        if (!path.equals("/") && !path.equals("")) {
            String[] tmps = path.substring(path.indexOf("/") + 1).split("/");
            Collections.addAll(paths, tmps);
        }
        paths.addFirst(ROOT_HINT);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_path_filepicker, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.textView.setText(paths.get(position));
        holder.imageView.setImageDrawable(arrowIcon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onPathClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textView = itemView.findViewById(R.id.text_view);
        }
    }

    public interface CallBack {
        void onPathClick(int position);
    }
}
