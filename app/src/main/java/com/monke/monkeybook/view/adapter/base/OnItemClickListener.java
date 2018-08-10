package com.monke.monkeybook.view.adapter.base;

import android.view.View;

public interface OnItemClickListener {
    void onClick(View view, int index);

    void onLongClick(View view, int index);
}
