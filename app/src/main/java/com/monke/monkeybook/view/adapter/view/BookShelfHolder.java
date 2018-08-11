package com.monke.monkeybook.view.adapter.view;

import android.app.Activity;
import android.view.animation.Animation;

import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.view.adapter.base.OnItemClickListener;
import com.monke.monkeybook.view.adapter.base.ViewHolderImpl;

public abstract class BookShelfHolder extends ViewHolderImpl<BookShelfBean> {

    protected Activity activity;
    protected OnItemClickListener itemClickListener;
    protected Boolean needAnim;
    protected String bookshelfPx;

    BookShelfHolder(Activity activity, boolean needAnim) {
        this.activity = activity;
        this.needAnim = needAnim;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setPx(String bookshelfPx) {
        this.bookshelfPx = bookshelfPx;
    }

    abstract class AnimationStartListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            onAnimStart(animation);
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        abstract void onAnimStart(Animation animation);
    }
}
