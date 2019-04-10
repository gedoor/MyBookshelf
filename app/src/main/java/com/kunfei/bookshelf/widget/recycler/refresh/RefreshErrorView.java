package com.kunfei.bookshelf.widget.recycler.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.widget.views.ATEAccentBgTextView;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RefreshErrorView extends LinearLayout {
    @BindView(R.id.tv_error_msg)
    TextView tvErrorMsg;
    @BindView(R.id.tv_refresh_again)
    ATEAccentBgTextView tvRefreshAgain;

    public RefreshErrorView(Context context) {
        super(context);
        init(context);
    }

    public RefreshErrorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshErrorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_searchbook_refresh_error, null);
        addView(view);
        ButterKnife.bind(this, view);
        view.setOnClickListener(null);
    }

    public void setErrorMsg(String msg) {
        tvErrorMsg.setText(msg);
    }

    public void setRefreshListener(OnClickListener onClickListener) {
        tvRefreshAgain.setOnClickListener(onClickListener);
    }

}
