package com.kunfei.bookshelf.view.adapter;

import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.view.adapter.base.BaseListAdapter;
import com.kunfei.bookshelf.view.adapter.base.IViewHolder;
import com.kunfei.bookshelf.view.adapter.view.FileHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by newbiechen on 17-5-27.
 */

public class FileSystemAdapter extends BaseListAdapter<File> {
    //记录item是否被选中的Map
    private HashMap<File, Boolean> mCheckMap = new HashMap<>();
    private int mCheckedCount = 0;

    @Override
    protected IViewHolder<File> createViewHolder(int viewType) {
        return new FileHolder(mCheckMap);
    }

    @Override
    public void refreshItems(List<File> list) {
        mCheckMap.clear();
        for (File file : list) {
            mCheckMap.put(file, false);
        }
        super.refreshItems(list);
    }

    @Override
    public void addItem(File value) {
        mCheckMap.put(value, false);
        super.addItem(value);
    }

    @Override
    public void addItem(int index, File value) {
        mCheckMap.put(value, false);
        super.addItem(index, value);
    }

    @Override
    public void addItems(List<File> values) {
        for (File file : values) {
            mCheckMap.put(file, false);
        }
        super.addItems(values);
    }

    @Override
    public void removeItem(File value) {
        mCheckMap.remove(value);
        super.removeItem(value);
    }

    @Override
    public void removeItems(List<File> value) {
        //删除在HashMap中的文件
        for (File file : value) {
            mCheckMap.remove(file);
            //因为，能够被移除的文件，肯定是选中的
            --mCheckedCount;
        }
        //删除列表中的文件
        super.removeItems(value);
    }

    //设置点击切换
    public void setCheckedItem(int pos) {
        File file = getItem(pos);
        if (isFileLoaded(file.getAbsolutePath())) return;

        boolean isSelected = mCheckMap.get(file);
        if (isSelected) {
            mCheckMap.put(file, false);
            --mCheckedCount;
        } else {
            mCheckMap.put(file, true);
            ++mCheckedCount;
        }
        notifyDataSetChanged();
    }

    public void setCheckedAll(boolean isChecked) {
        Set<Map.Entry<File, Boolean>> entrys = mCheckMap.entrySet();
        mCheckedCount = 0;
        for (Map.Entry<File, Boolean> entry : entrys) {
            //必须是文件，必须没有被收藏
            if (entry.getKey().isFile() && !isFileLoaded(entry.getKey().getAbsolutePath())) {
                entry.setValue(isChecked);
                //如果选中，则增加点击的数量
                if (isChecked) {
                    ++mCheckedCount;
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean isFileLoaded(String id) {
        //如果是已加载的文件，则点击事件无效。
        if (BookshelfHelp.getBook(id) != null) {
            return true;
        }
        return false;
    }

    public int getCheckableCount() {
        List<File> files = getItems();
        int count = 0;
        for (File file : files) {
            if (!isFileLoaded(file.getAbsolutePath()) && file.isFile())
                ++count;
        }
        return count;
    }

    public boolean getItemIsChecked(int pos) {
        File file = getItem(pos);
        return mCheckMap.get(file);
    }

    public List<File> getCheckedFiles() {
        List<File> files = new ArrayList<>();
        Set<Map.Entry<File, Boolean>> entrys = mCheckMap.entrySet();
        for (Map.Entry<File, Boolean> entry : entrys) {
            if (entry.getValue()) {
                files.add(entry.getKey());
            }
        }
        return files;
    }

    public int getCheckedCount() {
        return mCheckedCount;
    }

    public HashMap<File, Boolean> getCheckMap() {
        return mCheckMap;
    }
}
