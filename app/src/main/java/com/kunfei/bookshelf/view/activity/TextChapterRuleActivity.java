package com.kunfei.bookshelf.view.activity;

import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.presenter.TextChapterRulePresenter;
import com.kunfei.bookshelf.presenter.contract.TextChapterRuleContract;
import com.kunfei.bookshelf.utils.theme.ThemeStore;

import butterknife.BindView;

public class TextChapterRuleActivity extends MBaseActivity<TextChapterRuleContract.Presenter> implements TextChapterRuleContract.View {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected TextChapterRuleContract.Presenter initInjector() {
        return new TextChapterRulePresenter();
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_recycler_vew);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public Snackbar getSnackBar(String msg, int length) {
        return Snackbar.make(llContent, msg, length);
    }
}
