package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.model.BookSourceManage;
import com.monke.monkeybook.presenter.BookSourcePresenterImpl;
import com.monke.monkeybook.presenter.impl.IBookSourcePresenter;
import com.monke.monkeybook.view.adapter.BookSourceAdapter;
import com.monke.monkeybook.view.impl.IBookSourceManageView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GKF on 2017/12/16.
 * 书源管理
 */

public class ReplaceRuleActivity extends MBaseActivity<IBookSourcePresenter> implements IBookSourceManageView {
    public static final int EDIT_SOURCE = 101;

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
    ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            //也就是说返回值是组合式的
            //makeMovementFlags (int dragFlags, int swipeFlags)，看下面的解释说明
            int swipeFlag = 0;
            //如果也监控左右方向的话，swipeFlag=ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
            int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            //等价于：0001&0010;多点触控标记触屏手指的顺序和个数也是这样标记哦
            return makeMovementFlags(dragFlag, swipeFlag);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //直接按照文档来操作啊，这文档写得太给力了,简直完美！
            bookSourceAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            //注意这里有个坑的，itemView 都移动了，对应的数据也要移动
            Collections.swap(bookSourceBeanList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mPresenter.saveDate(bookSourceBeanList);
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

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_book_source);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initRecyclerView();
    }

    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);
    }

    private void initRecyclerView() {

    }

    private void resetBookSource() {
        bookSourceBeanList = BookSourceManage.saveBookSourceToDb();
        bookSourceAdapter.resetBookSource(bookSourceBeanList);
    }

    @Override
    public void refreshBookSource() {
        bookSourceBeanList = BookSourceManage.getAllBookSource();
        bookSourceAdapter.resetBookSource(bookSourceBeanList);
    }

    public void delBookSource(BookSourceBean bookSource) {
        mPresenter.delDate(bookSource);
    }

    public void saveDate(List<BookSourceBean> date) {
        mPresenter.saveDate(date);
    }

    @Override
    public View getView() {
        return llContent;
    }

    @Override
    protected void firstRequest() {
        llContent.startAnimation(animIn);
    }

    @Override
    protected IBookSourcePresenter initInjector() {
        return new BookSourcePresenterImpl();
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_source_manage);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_source_activity, menu);
        return true;
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_book_source:
                Intent intent = new Intent(this, SourceEditActivity.class);
                startActivityForResult(intent, EDIT_SOURCE);
                break;
            case R.id.action_reset_book_source:
                resetBookSource();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_SOURCE) {
            refreshBookSource();
        }
    }
}
