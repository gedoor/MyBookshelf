package com.monke.monkeybook.view.activity;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.model.ReplaceRuleManage;
import com.monke.monkeybook.presenter.BookSourcePresenterImpl;
import com.monke.monkeybook.presenter.impl.IBookSourcePresenter;
import com.monke.monkeybook.view.adapter.ReplaceRuleAdapter;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2017/12/16.
 * 书源管理
 */

public class ReplaceRuleActivity extends MBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerViewBookSource;

    private MoProgressHUD moProgressHUD;
    private Animation animIn;
    private ReplaceRuleAdapter adapter;

    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_recycler_vew);
    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        initRecyclerView();
        moProgressHUD = new MoProgressHUD(this);
        initRecyclerView();
    }

    @Override
    protected void initData() {
        animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);
        Animation animOut = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_out);
    }

    private void initRecyclerView() {
        recyclerViewBookSource.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReplaceRuleAdapter(this);
        recyclerViewBookSource.setAdapter(adapter);
        adapter.resetDataS(ReplaceRuleManage.getAll());
    }

    public void editReplaceRule(ReplaceRuleBean replaceRuleBean) {
        moProgressHUD.showPutReplaceRule(replaceRuleBean, ruleBean -> {
            Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
                if (replaceRuleBean != null) {
                    DbHelper.getInstance().getmDaoSession().getReplaceRuleBeanDao()
                            .delete(replaceRuleBean);
                }
                ruleBean.setEnable(true);
                ReplaceRuleManage.saveData(ruleBean);
                e.onNext(ReplaceRuleManage.getAll());
                e.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                        @Override
                        public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                            adapter.resetDataS(replaceRuleBeans);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        });
    }

    public void delData(ReplaceRuleBean replaceRuleBean) {
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            ReplaceRuleManage.delData(replaceRuleBean);
            e.onNext(ReplaceRuleManage.getAll());
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        adapter.resetDataS(replaceRuleBeans);
                        Snackbar.make(llContent, replaceRuleBean.getReplaceSummary() + "已删除", Snackbar.LENGTH_LONG)
                                .setAction("恢复", view -> {
                                    restoreData(replaceRuleBean);
                                })
                                .show();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void restoreData(ReplaceRuleBean replaceRuleBean) {
        Observable.create((ObservableOnSubscribe<List<ReplaceRuleBean>>) e -> {
            ReplaceRuleManage.saveData(replaceRuleBean);
            e.onNext(ReplaceRuleManage.getAll());
            e.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<ReplaceRuleBean>>() {
                    @Override
                    public void onNext(List<ReplaceRuleBean> replaceRuleBeans) {
                        adapter.resetDataS(replaceRuleBeans);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
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
            actionBar.setTitle(R.string.replace_rule_title);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_replace_rule_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_replace_rule:
                editReplaceRule(null);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean mo = moProgressHUD.onKeyDown(keyCode, event);
        if (mo) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
