package com.kunfei.bookshelf.view.adapter.view;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.constant.AppConstant;
import com.kunfei.bookshelf.help.FileHelp;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.view.adapter.base.ViewHolderImpl;

import java.io.File;
import java.util.HashMap;

/**
 * Created by newbiechen on 17-5-27.
 */

public class FileHolder extends ViewHolderImpl<File> {
    private ImageView mIvIcon;
    private CheckBox mCbSelect;
    private TextView mTvName;
    private LinearLayout mLlBrief;
    private TextView mTvTag;
    private TextView mTvSize;
    private TextView mTvDate;
    private TextView mTvSubCount;

    private HashMap<File, Boolean> mSelectedMap;

    public FileHolder(HashMap<File, Boolean> selectedMap) {
        mSelectedMap = selectedMap;
    }

    @Override
    public void initView() {
        mIvIcon = findById(R.id.file_iv_icon);
        mCbSelect = findById(R.id.file_cb_select);
        mTvName = findById(R.id.file_tv_name);
        mLlBrief = findById(R.id.file_ll_brief);
        mTvTag = findById(R.id.file_tv_tag);
        mTvSize = findById(R.id.file_tv_size);
        mTvDate = findById(R.id.file_tv_date);
        mTvSubCount = findById(R.id.file_tv_sub_count);
    }

    @Override
    public void onBind(File data, int pos) {
        //判断是文件还是文件夹
        if (data.isDirectory()) {
            setFolder(data);
        } else {
            setFile(data);
        }
        mCbSelect.setClickable(false);
    }

    private void setFile(File file) {
        //选择

        if (BookshelfHelp.getBook(file.getAbsolutePath()) != null) {
            mIvIcon.setImageResource(R.drawable.ic_book_has);
            mIvIcon.setVisibility(View.VISIBLE);
            mCbSelect.setVisibility(View.GONE);
        } else {
            boolean isSelected = mSelectedMap.get(file);
            mCbSelect.setChecked(isSelected);
            mIvIcon.setVisibility(View.GONE);
            mCbSelect.setVisibility(View.VISIBLE);
        }

        mLlBrief.setVisibility(View.VISIBLE);
        mTvSubCount.setVisibility(View.GONE);

        mTvName.setText(file.getName());
        mTvTag.setText(file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase());
        mTvSize.setText(FileHelp.getFileSize(file.length()));
        mTvDate.setText(StringUtils.dateConvert(file.lastModified(), AppConstant.FORMAT_FILE_DATE));
    }

    public void setFolder(File folder) {
        //图片
        mIvIcon.setVisibility(View.VISIBLE);
        mCbSelect.setVisibility(View.GONE);
        mIvIcon.setImageResource(R.drawable.ic_folder_black_24dp);
        //名字
        mTvName.setText(folder.getName());
        //介绍
        mLlBrief.setVisibility(View.GONE);
        mTvSubCount.setVisibility(View.VISIBLE);

        mTvSubCount.setText(getContext().getString(R.string.nb_file_sub_count, folder.list().length));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_file;
    }
}
