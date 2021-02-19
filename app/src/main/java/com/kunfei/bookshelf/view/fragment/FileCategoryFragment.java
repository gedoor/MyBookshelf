package com.kunfei.bookshelf.view.fragment;

import android.graphics.PorterDuff;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.databinding.FragmentFileCategoryBinding;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.FileHelp;
import com.kunfei.bookshelf.utils.FileStack;
import com.kunfei.bookshelf.utils.FileUtils;
import com.kunfei.bookshelf.view.adapter.FileSystemAdapter;
import com.kunfei.bookshelf.widget.itemdecoration.DividerItemDecoration;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FileCategoryFragment extends BaseFileFragment {
    private static final String TAG = "FileCategoryFragment";

    private FragmentFileCategoryBinding binding;
    private FileStack mFileStack;
    private String rootFilePath;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentFileCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void bindView() {
        super.bindView();
        mFileStack = new FileStack();
        setUpAdapter();
    }

    private void setUpAdapter() {
        mAdapter = new FileSystemAdapter();
        binding.fileCategoryRvContent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fileCategoryRvContent.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext())));
        binding.fileCategoryRvContent.setAdapter(mAdapter);
        setTextViewIconColor(binding.fileCategoryTvBackLast);
    }

    @Override
    protected void bindEvent() {
        super.bindEvent();
        mAdapter.setOnItemClickListener(
                (view, pos) -> {
                    File file = mAdapter.getItem(pos);
                    if (file.isDirectory()) {
                        //保存当前信息。
                        FileStack.FileSnapshot snapshot = new FileStack.FileSnapshot();
                        snapshot.filePath = binding.fileCategoryTvPath.getText().toString();
                        snapshot.files = new ArrayList<>(mAdapter.getItems());
                        snapshot.scrollOffset = binding.fileCategoryRvContent.computeVerticalScrollOffset();
                        mFileStack.push(snapshot);
                        //切换下一个文件
                        toggleFileTree(file);
                    } else {

                        //如果是已加载的文件，则点击事件无效。
                        String id = mAdapter.getItem(pos).getAbsolutePath();
                        if (BookshelfHelp.getBook(id) != null) {
                            return;
                        }
                        //点击选中
                        mAdapter.setCheckedItem(pos);
                        //反馈
                        if (mListener != null) {
                            mListener.onItemCheckedChange(mAdapter.getItemIsChecked(pos));
                        }
                    }
                }
        );

        binding.fileCategoryTvBackLast.setOnClickListener(v -> {
            FileStack.FileSnapshot snapshot = mFileStack.pop();
            int oldScrollOffset = binding.fileCategoryRvContent.computeHorizontalScrollOffset();
            if (snapshot == null) return;
            binding.fileCategoryTvPath.setText(snapshot.filePath);
            mAdapter.refreshItems(snapshot.files);
            binding.fileCategoryRvContent.scrollBy(0, snapshot.scrollOffset - oldScrollOffset);
            //反馈
            if (mListener != null) {
                mListener.onCategoryChanged();
            }
                }
        );

        binding.tvSd.setOnClickListener(v -> {
            if (getContext() != null) {
                List<String> list = FileUtils.getStorageData(getContext());
                if (list != null) {
                    String[] filePathS = list.toArray(new String[0]);
                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setTitle(R.string.select_sd_file)
                            .setSingleChoiceItems(filePathS, 0, (dialogInterface, i) -> {
                                upRootFile(filePathS[i]);
                                dialogInterface.dismiss();
                            })
                            .create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected void firstRequest() {
        super.firstRequest();
        upRootFile(Environment.getExternalStorageDirectory().getPath());
    }

    private void upRootFile(String rootFilePath) {
        this.rootFilePath = rootFilePath;
        toggleFileTree(new File(rootFilePath));
    }

    private void setTextViewIconColor(TextView textView) {
        // textView.getCompoundDrawables()[0].mutate();
        try {
            textView.getCompoundDrawables()[0].setColorFilter(getResources().getColor(R.color.tv_text_default), PorterDuff.Mode.SRC_ATOP);
        } catch (Exception ignored) {
        }
    }

    private void toggleFileTree(File file) {
        //路径名
        binding.fileCategoryTvPath.setText(file.getPath().replace(rootFilePath, ""));
        //获取数据
        File[] files = file.listFiles(new SimpleFileFilter());
        //转换成List
        List<File> rootFiles = Arrays.asList(files);
        //排序
        Collections.sort(rootFiles, new FileComparator());
        //加入
        mAdapter.refreshItems(rootFiles);
        //反馈
        if (mListener != null) {
            mListener.onCategoryChanged();
        }
    }

    @Override
    public int getFileCount() {
        int count = 0;
        Set<Map.Entry<File, Boolean>> entrys = mAdapter.getCheckMap().entrySet();
        for (Map.Entry<File, Boolean> entry : entrys) {
            if (!entry.getKey().isDirectory()) {
                ++count;
            }
        }
        return count;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            if (o1.isDirectory() && o2.isFile()) {
                return -1;
            }
            if (o2.isDirectory() && o1.isFile()) {
                return 1;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    public static class SimpleFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().startsWith(".")) {
                return false;
            }
            //文件夹内部数量为0
            if (pathname.isDirectory() && (pathname.list() == null || pathname.list().length == 0)) {
                return false;
            }

            //文件内容为空,或者不以txt为开头
            return pathname.isDirectory() ||
                    (pathname.length() != 0
                            && (pathname.getName().toLowerCase().endsWith(FileHelp.SUFFIX_TXT)
                            || pathname.getName().toLowerCase().endsWith(FileHelp.SUFFIX_EPUB)));
        }
    }
}
