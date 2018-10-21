package com.monke.monkeybook.view.activity;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.BaseTabActivity;
import com.monke.monkeybook.presenter.ImportBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.ImportBookContract;
import com.monke.monkeybook.view.fragment.BaseFileFragment;
import com.monke.monkeybook.view.fragment.FileCategoryFragment;
import com.monke.monkeybook.view.fragment.LocalBookFragment;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by newbiechen on 17-5-27.
 */

public class ImportBookActivity extends BaseTabActivity<ImportBookContract.Presenter> implements ImportBookContract.View {
    private static final String TAG = "ImportBookActivity";

    @BindView(R.id.file_system_cb_selected_all)
    CheckBox mCbSelectAll;
    @BindView(R.id.file_system_btn_delete)
    Button mBtnDelete;
    @BindView(R.id.file_system_btn_add_book)
    Button mBtnAddBook;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private LocalBookFragment mLocalFragment;
    private FileCategoryFragment mCategoryFragment;
    private BaseFileFragment mCurFragment;

    private BaseFileFragment.OnFileCheckedListener mListener = new BaseFileFragment.OnFileCheckedListener() {
        @Override
        public void onItemCheckedChange(boolean isChecked) {
            changeMenuStatus();
        }

        @Override
        public void onCategoryChanged() {
            //状态归零
            mCurFragment.setCheckedAll(false);
            //改变菜单
            changeMenuStatus();
            //改变是否能够全选
            changeCheckedAllStatus();
        }
    };


    @Override
    protected ImportBookContract.Presenter initInjector() {
        return new ImportBookPresenterImpl();
    }

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_import_book);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupActionBar();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected List<Fragment> createTabFragments() {
        mLocalFragment = new LocalBookFragment();
        mCategoryFragment = new FileCategoryFragment();
        return Arrays.asList(mLocalFragment, mCategoryFragment);
    }

    @Override
    protected List<String> createTabTitles() {
        return Arrays.asList("智能导入", "手机目录");
    }

    @Override
    protected void bindEvent() {
        mCbSelectAll.setOnClickListener(
                (view) -> {
                    //设置全选状态
                    boolean isChecked = mCbSelectAll.isChecked();
                    mCurFragment.setCheckedAll(isChecked);
                    //改变菜单状态
                    changeMenuStatus();
                }
        );

        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mCurFragment = mLocalFragment;
                } else {
                    mCurFragment = mCategoryFragment;
                }
                //改变菜单状态
                changeMenuStatus();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mBtnAddBook.setOnClickListener(
                (v) -> {
                    //获取选中的文件
                    List<File> files = mCurFragment.getCheckedFiles();
                    //转换成CollBook,并存储
                    mPresenter.importBooks(files);
                }
        );

        mBtnDelete.setOnClickListener(
                (v) -> {
                    //弹出，确定删除文件吗。
                    new AlertDialog.Builder(this)
                            .setTitle("删除文件")
                            .setMessage("确定删除文件吗?")
                            .setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
                                //删除选中的文件
                                mCurFragment.deleteCheckedFiles();
                                //提示删除文件成功
                                toast("删除文件成功");
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), null)
                            .show();
                }
        );

        mLocalFragment.setOnFileCheckedListener(mListener);
        mCategoryFragment.setOnFileCheckedListener(mListener);
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        mCurFragment = mLocalFragment;
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.book_local);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_import, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单状态
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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


    /**
     * 改变底部选择栏的状态
     */
    private void changeMenuStatus() {

        //点击、删除状态的设置
        if (mCurFragment.getCheckedCount() == 0) {
            mBtnAddBook.setText(getString(R.string.nb_file_add_shelf));
            //设置某些按钮的是否可点击
            setMenuClickable(false);

            if (mCbSelectAll.isChecked()) {
                mCurFragment.setChecked(false);
                mCbSelectAll.setChecked(mCurFragment.isCheckedAll());
            }

        } else {
            mBtnAddBook.setText(getString(R.string.nb_file_add_shelves, mCurFragment.getCheckedCount()));
            setMenuClickable(true);

            //全选状态的设置

            //如果选中的全部的数据，则判断为全选
            if (mCurFragment.getCheckedCount() == mCurFragment.getCheckableCount()) {
                //设置为全选
                mCurFragment.setChecked(true);
                mCbSelectAll.setChecked(mCurFragment.isCheckedAll());
            }
            //如果曾今是全选则替换
            else if (mCurFragment.isCheckedAll()) {
                mCurFragment.setChecked(false);
                mCbSelectAll.setChecked(mCurFragment.isCheckedAll());
            }
        }

        //重置全选的文字
        if (mCurFragment.isCheckedAll()) {
            mCbSelectAll.setText("取消");
        } else {
            mCbSelectAll.setText("全选");
        }

    }

    private void setMenuClickable(boolean isClickable) {

        //设置是否可删除
        mBtnDelete.setEnabled(isClickable);
        mBtnDelete.setClickable(isClickable);

        //设置是否可添加书籍
        mBtnAddBook.setEnabled(isClickable);
        mBtnAddBook.setClickable(isClickable);
    }

    /**
     * 改变全选按钮的状态
     */
    private void changeCheckedAllStatus() {
        //获取可选择的文件数量
        int count = mCurFragment.getCheckableCount();

        //设置是否能够全选
        if (count > 0) {
            mCbSelectAll.setClickable(true);
            mCbSelectAll.setEnabled(true);
        } else {
            mCbSelectAll.setClickable(false);
            mCbSelectAll.setEnabled(false);
        }
    }

    @Override
    public void addSuccess() {
        //设置HashMap为false
        mCurFragment.setCheckedAll(false);
        //改变菜单状态
        changeMenuStatus();
        //改变是否可以全选
        changeCheckedAllStatus();
    }

    @Override
    public void addError(String msg) {

    }
}
