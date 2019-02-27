package com.kunfei.bookshelf.view.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.ItemTouchCallback;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.view.activity.BookSourceActivity;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by GKF on 2017/12/22.
 * 书源Adapter
 */

public class BookSourceAdapter extends RecyclerView.Adapter<BookSourceAdapter.MyViewHolder> {
    private List<BookSourceBean> dataList;
    private List<BookSourceBean> allDataList;
    private BookSourceActivity activity;
    private int index;
    private int sort;

    private ItemTouchCallback.OnItemTouchCallbackListener itemTouchCallbackListener = new ItemTouchCallback.OnItemTouchCallbackListener() {
        @Override
        public void onSwiped(int adapterPosition) {

        }

        @Override
        public boolean onMove(int srcPosition, int targetPosition) {
            Collections.swap(dataList, srcPosition, targetPosition);
            notifyItemMoved(srcPosition, targetPosition);
            notifyItemChanged(srcPosition);
            notifyItemChanged(targetPosition);
            activity.saveDate(dataList);
            return true;
        }
    };

    public BookSourceAdapter(BookSourceActivity activity) {
        this.activity = activity;
        dataList = new ArrayList<>();
    }

    public void resetDataS(List<BookSourceBean> bookSourceBeanList) {
        this.dataList = bookSourceBeanList;
        notifyDataSetChanged();
        activity.upDateSelectAll();
        activity.upSearchView(dataList.size());
        activity.upGroupMenu();
    }

    private void allDataList(List<BookSourceBean> bookSourceBeanList) {
        this.allDataList = bookSourceBeanList;
        notifyDataSetChanged();
        activity.upDateSelectAll();
    }

    public List<BookSourceBean> getDataList() {
        return dataList;
    }

    public List<BookSourceBean> getSelectDataList() {
        List<BookSourceBean> selectDataS = new ArrayList<>();
        for (BookSourceBean data : dataList) {
            if (data.getEnable()) {
                selectDataS.add(data);
            }
        }
        return selectDataS;
    }

    public ItemTouchCallback.OnItemTouchCallbackListener getItemTouchCallbackListener() {
        return itemTouchCallbackListener;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_source, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (sort != 2) {
            holder.topView.setVisibility(View.VISIBLE);
        } else {
            holder.topView.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(dataList.get(position).getBookSourceGroup())) {
            holder.cbView.setText(dataList.get(position).getBookSourceName());
        } else {
            holder.cbView.setText(String.format("%s (%s)", dataList.get(position).getBookSourceName(), dataList.get(position).getBookSourceGroup()));
        }
        holder.cbView.setChecked(dataList.get(position).getEnable());
        holder.cbView.setOnClickListener((View view) -> {
            dataList.get(position).setEnable(holder.cbView.isChecked());
            activity.saveDate(dataList.get(position));
            activity.upDateSelectAll();
        });
        holder.editView.setOnClickListener(view -> SourceEditActivity.startThis(activity, dataList.get(position)));
        holder.delView.setOnClickListener(view -> {
            activity.delBookSource(dataList.get(position));
            dataList.remove(position);
            activity.upSearchView(dataList.size());
            notifyDataSetChanged();
        });
        holder.topView.setOnClickListener(view -> {
            allDataList(BookSourceManager.getAllBookSource());
            BookSourceBean moveData = dataList.get(position);
            if (sort == 0) {
                moveData.setSerialNumber(0);
            } else if (sort == 1) {
                int maxWeight = allDataList.get(0).getWeight();
                moveData.setWeight(maxWeight + 1);
                BookshelfHelp.saveBookSource(moveData);
            }
            dataList.remove(position);
            notifyItemRemoved(position);
            dataList.add(0, moveData);
            notifyItemInserted(0);

            if (dataList.size() != allDataList.size()) {
                for (int i = 0; i < allDataList.size(); i++) {
                    if (moveData.equals(allDataList.get(i))) {
                        index = i;
                        break;
                    }
                }
                BookSourceBean moveDataA = allDataList.get(index);
                allDataList.remove(index);
                allDataList.add(0, moveDataA);
            }
            activity.saveDate(allDataList);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbView;
        ImageView editView;
        ImageView delView;
        ImageView topView;

        MyViewHolder(View itemView) {
            super(itemView);
            cbView = itemView.findViewById(R.id.cb_book_source);
            editView = itemView.findViewById(R.id.iv_edit_source);
            delView = itemView.findViewById(R.id.iv_del_source);
            topView = itemView.findViewById(R.id.iv_top_source);
        }
    }
}
