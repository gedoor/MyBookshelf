package com.kunfei.bookshelf.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.google.android.material.textfield.TextInputLayout;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.view.activity.SourceEditActivity;

import java.util.ArrayList;
import java.util.List;

public class SourceEditAdapter extends Adapter<SourceEditAdapter.MyViewHolder> {
    private Context context;
    private List<SourceEditActivity.SourceEdit> data = new ArrayList<>();

    public SourceEditAdapter(Context context) {
        this.context = context;
    }

    public void reSetData(List<SourceEditActivity.SourceEdit> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_source, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.editText.setText(data.get(position).getValue());
        holder.textInputLayout.setHint(context.getString(data.get(position).getHint()));
        holder.editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                data.get(position).setValue(holder.editText.getText().toString());
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextInputLayout textInputLayout;
        EditText editText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textInputLayout = itemView.findViewById(R.id.textInputLayout);
            editText = itemView.findViewById(R.id.editText);
        }
    }
}

