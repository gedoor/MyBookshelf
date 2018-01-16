package com.monke.monkeybook.view.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.model.impl.BookSourceManage;
import com.monke.monkeybook.presenter.IBookSourcePresenter;
import com.monke.monkeybook.presenter.impl.BookSourcePresenterImpl;
import com.monke.monkeybook.view.IBookSourceManageView;
import com.monke.monkeybook.view.adapter.BookSourceAdapter;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GKF on 2017/12/16.
 * 书源管理
 */

public class BookSourceActivity extends MBaseActivity<IBookSourcePresenter> implements IBookSourceManageView {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.rv_book_source_list)
    RecyclerView recyclerViewBookSource;

    private Animation animIn;
    private Animation animOut;
    private BookSourceAdapter bookSourceAdapter;
    private List<BookSourceBean> bookSourceBeanList;

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_source);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initReayclerView();
    }

    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);
    }

    private void initReayclerView() {
        recyclerViewBookSource.setLayoutManager(new LinearLayoutManager(this));
        bookSourceBeanList = BookSourceManage.getAllBookSource();
        bookSourceAdapter = new BookSourceAdapter(bookSourceBeanList);
        recyclerViewBookSource.setAdapter(bookSourceAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerViewBookSource);
    }

    @Override
    protected void firstRequest() {
        llContent.startAnimation(animIn);
    }

    @Override
    protected IBookSourcePresenter initInjector() {
        return new BookSourcePresenterImpl();
    }

    ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            //也就是说返回值是组合式的
            //makeMovementFlags (int dragFlags, int swipeFlags)，看下面的解释说明
            int swipFlag=0;
            //如果也监控左右方向的话，swipFlag=ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
            int dragflag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            //等价于：0001&0010;多点触控标记触屏手指的顺序和个数也是这样标记哦
            return  makeMovementFlags(dragflag,swipFlag);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //直接按照文档来操作啊，这文档写得太给力了,简直完美！
            bookSourceAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            //注意这里有个坑的，itemView 都移动了，对应的数据也要移动
            Collections.swap(bookSourceBeanList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //暂不处理
        }

        @Override
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            //return true后，可以实现长按拖动排序和拖动动画了
            return true;
        }
    };

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_source_manage);
        }
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
