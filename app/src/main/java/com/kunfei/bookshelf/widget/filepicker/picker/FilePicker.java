package com.kunfei.bookshelf.widget.filepicker.picker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.widget.filepicker.adapter.FileAdapter;
import com.kunfei.bookshelf.widget.filepicker.adapter.PathAdapter;
import com.kunfei.bookshelf.widget.filepicker.entity.FileItem;
import com.kunfei.bookshelf.widget.filepicker.popup.ConfirmPopup;
import com.kunfei.bookshelf.widget.filepicker.util.StorageUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * 文件目录选择器
 *
 * @author 李玉江[QQ:1032694760]
 * @since 2015/9/29, 2017/01/01, 2017/01/08
 */
public class FilePicker extends ConfirmPopup<LinearLayout> implements FileAdapter.CallBack, PathAdapter.CallBack {
    public static final int DIRECTORY = 0;
    public static final int FILE = 1;

    private String initPath;
    private FileAdapter adapter = new FileAdapter();
    private PathAdapter pathAdapter = new PathAdapter();
    private TextView emptyView;
    private OnFilePickListener onFilePickListener;
    private int mode;
    private CharSequence emptyHint = java.util.Locale.getDefault().getDisplayLanguage().contains("中文") ? "<空>" : "<Empty>";

    @IntDef(value = {DIRECTORY, FILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    /**
     * @see #FILE
     * @see #DIRECTORY
     */
    public FilePicker(Activity activity, @Mode int mode) {
        super(activity);
        setHalfScreen(true);
        try {
            this.initPath = StorageUtils.getDownloadPath();
        } catch (RuntimeException e) {
            this.initPath = StorageUtils.getInternalRootPath(activity);
        }
        this.mode = mode;
        adapter.setOnlyListDir(mode == DIRECTORY);
        adapter.setShowHideDir(false);
        adapter.setShowHomeDir(false);
        adapter.setShowUpDir(false);
        adapter.setCallBack(this);
        pathAdapter.setCallBack(this);
    }

    @Override
    @NonNull
    protected LinearLayout makeCenterView() {
        @SuppressLint("InflateParams") LinearLayout rootLayout = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.view_file_picker, null);

        RecyclerView recyclerView = rootLayout.findViewById(R.id.rv_file);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, LinearLayout.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);

        emptyView = rootLayout.findViewById(R.id.tv_empty);

        RecyclerView pathView = rootLayout.findViewById(R.id.rv_path);
        pathView.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
        pathView.setAdapter(pathAdapter);

        return rootLayout;
    }

    public void setRootPath(String initPath) {
        this.initPath = initPath;
    }

    public void setAllowExtensions(String[] allowExtensions) {
        adapter.setAllowExtensions(allowExtensions);
    }

    public void setShowUpDir(boolean showUpDir) {
        adapter.setShowUpDir(showUpDir);
    }

    public void setShowHomeDir(boolean showHomeDir) {
        adapter.setShowHomeDir(showHomeDir);
    }

    public void setShowHideDir(boolean showHideDir) {
        adapter.setShowHideDir(showHideDir);
    }

    public void setFileIcon(Drawable fileIcon) {
        adapter.setFileIcon(fileIcon);
    }

    public void setFolderIcon(Drawable folderIcon) {
        adapter.setFolderIcon(folderIcon);
    }

    public void setHomeIcon(Drawable homeIcon) {
        adapter.setHomeIcon(homeIcon);
    }

    public void setUpIcon(Drawable upIcon) {
        adapter.setUpIcon(upIcon);
    }

    public void setArrowIcon(Drawable arrowIcon) {
        pathAdapter.setArrowIcon(arrowIcon);
    }

    public void setItemHeight(int itemHeight) {
        adapter.setItemHeight(itemHeight);
    }

    public void setEmptyHint(CharSequence emptyHint) {
        this.emptyHint = emptyHint;
    }

    @Override
    protected void setContentViewBefore() {
        boolean isPickFile = mode == FILE;
        setCancelVisible(!isPickFile);
        if (isPickFile) {
            setSubmitText(activity.getString(android.R.string.cancel));
        } else {
            setSubmitText(activity.getString(android.R.string.ok));
        }
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        refreshCurrentDirPath(initPath);
    }

    @Override
    protected void onSubmit() {
        if (mode != FILE) {
            String currentPath = adapter.getCurrentPath();
            if (onFilePickListener != null) {
                onFilePickListener.onFilePicked(currentPath);
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public FileAdapter getAdapter() {
        return adapter;
    }

    public PathAdapter getPathAdapter() {
        return pathAdapter;
    }

    public String getCurrentPath() {
        return adapter.getCurrentPath();
    }

    /**
     * 响应选择器的列表项点击事件
     */
    @Override
    public void onFileClick(int position) {
        FileItem fileItem = adapter.getItem(position);
        if (fileItem.isDirectory()) {
            refreshCurrentDirPath(fileItem.getPath());
        } else {
            String clickPath = fileItem.getPath();
            if (mode != DIRECTORY) {
                dismiss();
                if (onFilePickListener != null) {
                    onFilePickListener.onFilePicked(clickPath);
                }
            }
        }
    }

    @Override
    public void onPathClick(int position) {
        refreshCurrentDirPath(pathAdapter.getItem(position));
    }

    private void refreshCurrentDirPath(String currentPath) {
        if (currentPath.equals("/")) {
            pathAdapter.updatePath("/");
        } else {
            pathAdapter.updatePath(currentPath);
        }
        adapter.loadData(currentPath);
        int adapterCount = adapter.getItemCount();
        if (adapter.isShowHomeDir()) {
            adapterCount--;
        }
        if (adapter.isShowUpDir()) {
            adapterCount--;
        }
        if (adapterCount < 1) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(emptyHint);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    public void setOnFilePickListener(OnFilePickListener listener) {
        this.onFilePickListener = listener;
    }

    public interface OnFilePickListener {

        void onFilePicked(String currentPath);

    }

}
