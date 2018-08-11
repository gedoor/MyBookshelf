package com.monke.monkeybook.view.adapter.view;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.view.adapter.base.OnItemClickListener;
import com.monke.monkeybook.view.adapter.base.ViewHolderImpl;
import com.monke.mprogressbar.MHorProgressBar;
import com.victor.loading.rotate.RotateLoading;

import java.util.Objects;

import me.grantland.widget.AutofitTextView;

public class BookShelfHolderList extends BookShelfHolder {
    private FrameLayout flContent;
    private ImageView ivCover;
    private ImageView ivHasNew;
    private AutofitTextView tvName;
    private AutofitTextView tvRead;
    private AutofitTextView tvLast;
    private MHorProgressBar mpbDurProgress;
    private ImageButton ibContent;
    private ImageButton ibCover;
    private RotateLoading rotateLoading;

    public BookShelfHolderList(Activity activity, boolean needAnim) {
        super(activity, needAnim);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.adapter_bookshelf_list;
    }

    @Override
    public void initView() {
        flContent = findById(R.id.fl_content);
        ivCover = findById(R.id.iv_cover);
        ivHasNew = findById(R.id.iv_has_new);
        tvName = findById(R.id.tv_name);
        tvRead = findById(R.id.tv_read);
        tvLast = findById(R.id.tv_last);
        mpbDurProgress = findById(R.id.mpb_durProgress);
        ibContent = findById(R.id.ib_content);
        ibCover = findById(R.id.ib_cover);
        rotateLoading = findById(R.id.rl_loading);
    }

    @Override
    public void onBind(BookShelfBean data, int index) {
        if (needAnim) {
            final Animation animation = AnimationUtils.loadAnimation(flContent.getContext(), R.anim.anim_bookshelf_item);
            animation.setAnimationListener(new AnimationStartListener() {
                @Override
                void onAnimStart(Animation animation) {
                    needAnim = false;
                    flContent.setVisibility(View.VISIBLE);
                }
            });
            long DUR_ANIM_ITEM = 30;
            new Handler().postDelayed(() -> flContent.startAnimation(animation), index * DUR_ANIM_ITEM);
        } else {
            flContent.setVisibility(View.VISIBLE);
        }
        ibContent.setContentDescription(String.format("%s,最新章节:%s",
                data.getBookInfoBean().getName(),
                data.getLastChapterListBean().getDurChapterName()));
        if (!activity.isFinishing()) {
            Glide.with(activity).load(data.getBookInfoBean().getCoverUrl())
                    .apply(new RequestOptions().dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop().placeholder(R.drawable.img_cover_default))
                    .into(ivCover);
        }
        if (!TextUtils.isEmpty(data.getBookInfoBean().getAuthor())) {
            tvName.setText(String.format("%s(%s)", data.getBookInfoBean().getName(), data.getBookInfoBean().getAuthor()));
        } else {
            tvName.setText(data.getBookInfoBean().getName());
        }
        if (null != data.getBookInfoBean() && null != data.getChapterList()
                && data.getChapterListSize() > data.getDurChapter()) {
            tvRead.setText(String.format(tvRead.getContext().getString(R.string.read_dur_progress),
                    data.getDurChapterListBean().getDurChapterName()));
            tvLast.setText(String.format(tvLast.getContext().getString(R.string.book_search_last),
                    data.getLastChapterListBean().getDurChapterName()));
            if (data.getHasUpdate()) {
                ivHasNew.setVisibility(View.VISIBLE);
            } else {
                ivHasNew.setVisibility(View.INVISIBLE);
            }
        }
        //进度条
        mpbDurProgress.setVisibility(View.VISIBLE);
        mpbDurProgress.setMaxProgress(data.getChapterListSize());
        float speed = data.getChapterListSize() * 1.0f / 60;

        mpbDurProgress.setSpeed(speed <= 0 ? 1 : speed);

        if (needAnim) {
            mpbDurProgress.setDurProgressWithAnim(data.getDurChapter());
        } else {
            mpbDurProgress.setDurProgress(data.getDurChapter());
        }
        ibCover.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, index);
        });
        ibCover.setOnLongClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onLongClick(v, index);
            }
            return true;
        });
        ibContent.setOnClickListener(v -> {
            if (itemClickListener != null)
                itemClickListener.onClick(v, index);
        });
        if (!Objects.equals(bookshelfPx, "2")) {
            ibContent.setOnLongClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onLongClick(v, index);
                }
                return true;
            });
        } else if (data.getSerialNumber() != index){
            data.setSerialNumber(index);
            new Thread(){
                public void run() {
                    DbHelper.getInstance().getmDaoSession().getBookShelfBeanDao().insertOrReplace(data);
                }
            }.start();
        }
        if (data.isLoading()) {
            rotateLoading.setVisibility(View.VISIBLE);
            rotateLoading.start();
        } else {
            rotateLoading.setVisibility(View.INVISIBLE);
            rotateLoading.stop();
        }
    }


}
