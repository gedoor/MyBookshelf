package com.kunfei.bookshelf.base;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;

import java.util.List;

/**
 * Created by newbiechen on 17-4-24.
 */

public abstract class BaseTabActivity<T extends IPresenter> extends MBaseActivity<T> {
    /**************View***************/
    protected TabLayout mTlIndicator;
    protected ViewPager mVp;
    /**************Adapter***************/
    protected TabFragmentPageAdapter tabFragmentPageAdapter;
    /************Params*******************/
    protected List<Fragment> mFragmentList;
    private List<String> mTitleList;

    /**************abstract***********/
    protected abstract List<Fragment> createTabFragments();

    protected abstract List<String> createTabTitles();

    @Override
    protected void bindView() {
        super.bindView();
        mTlIndicator = findViewById(R.id.tab_tl_indicator);
        mVp = findViewById(R.id.tab_vp);
        setUpTabLayout();
    }

    /*****************rewrite method***************************/


    private void setUpTabLayout() {
        mFragmentList = createTabFragments();
        mTitleList = createTabTitles();

        checkParamsIsRight();

        tabFragmentPageAdapter = new TabFragmentPageAdapter(getSupportFragmentManager());
        mVp.setAdapter(tabFragmentPageAdapter);
        mVp.setOffscreenPageLimit(3);
        mTlIndicator.setupWithViewPager(mVp);
    }

    /**
     * 检查输入的参数是否正确。即Fragment和title是成对的。
     */
    private void checkParamsIsRight() {
        if (mFragmentList == null || mTitleList == null) {
            throw new IllegalArgumentException("fragmentList or titleList doesn't have null");
        }

        if (mFragmentList.size() != mTitleList.size())
            throw new IllegalArgumentException("fragment and title size must equal");
    }

    /******************inner class*****************/
    public class TabFragmentPageAdapter extends FragmentPagerAdapter {

        TabFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

    }
}
