package com.kunfei.bookshelf.widget.filepicker.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.widget.filepicker.entity.FileItem;
import com.kunfei.bookshelf.widget.filepicker.icons.FilePickerIcon;
import com.kunfei.bookshelf.widget.filepicker.util.ConvertUtils;
import com.kunfei.bookshelf.widget.filepicker.util.FileUtils;

import java.io.File;
import java.util.ArrayList;


public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {
    public static final String DIR_ROOT = ".";
    public static final String DIR_PARENT = "..";
    private ArrayList<FileItem> data = new ArrayList<>();
    private String rootPath = null;
    private String currentPath = null;
    private String[] allowExtensions = null;//允许的扩展名
    private boolean onlyListDir = false;//是否仅仅读取目录
    private boolean showHomeDir = false;//是否显示返回主目录
    private boolean showUpDir = true;//是否显示返回上一级
    private boolean showHideDir = true;//是否显示隐藏的目录（以“.”开头）
    private int itemHeight = 40;// dp
    private Drawable homeIcon = null;
    private Drawable upIcon = null;
    private Drawable folderIcon = null;
    private Drawable fileIcon = null;
    private CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public FileItem getItem(int pos) {
        return data.get(pos);
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setFileIcon(Drawable fileIcon) {
        this.fileIcon = fileIcon;
    }

    public void setFolderIcon(Drawable folderIcon) {
        this.folderIcon = folderIcon;
    }

    public void setHomeIcon(Drawable homeIcon) {
        this.homeIcon = homeIcon;
    }

    public void setUpIcon(Drawable upIcon) {
        this.upIcon = upIcon;
    }

    /**
     * 允许的扩展名
     */
    public void setAllowExtensions(String[] allowExtensions) {
        this.allowExtensions = allowExtensions;
    }

    /**
     * 是否仅仅读取目录
     */
    public void setOnlyListDir(boolean onlyListDir) {
        this.onlyListDir = onlyListDir;
    }

    public boolean isOnlyListDir() {
        return onlyListDir;
    }

    /**
     * 是否显示返回主目录
     */
    public void setShowHomeDir(boolean showHomeDir) {
        this.showHomeDir = showHomeDir;
    }

    public boolean isShowHomeDir() {
        return showHomeDir;
    }

    /**
     * 是否显示返回上一级
     */
    public void setShowUpDir(boolean showUpDir) {
        this.showUpDir = showUpDir;
    }

    public boolean isShowUpDir() {
        return showUpDir;
    }

    /**
     * 是否显示隐藏的目录（以“.”开头）
     */
    public void setShowHideDir(boolean showHideDir) {
        this.showHideDir = showHideDir;
    }

    public boolean isShowHideDir() {
        return showHideDir;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    public void loadData(String path) {
        if (path == null) {
            return;
        }
        if (homeIcon == null) {
            homeIcon = ConvertUtils.toDrawable(FilePickerIcon.getHOME());
        }
        if (upIcon == null) {
            upIcon = ConvertUtils.toDrawable(FilePickerIcon.getUPDIR());
        }
        if (folderIcon == null) {
            folderIcon = ConvertUtils.toDrawable(FilePickerIcon.getFOLDER());
        }
        if (fileIcon == null) {
            fileIcon = ConvertUtils.toDrawable(FilePickerIcon.getFILE());
        }
        ArrayList<FileItem> datas = new ArrayList<FileItem>();
        if (rootPath == null) {
            rootPath = path;
        }
        currentPath = path;
        if (showHomeDir) {
            //添加“返回主目录”
            FileItem fileRoot = new FileItem();
            fileRoot.setDirectory(true);
            fileRoot.setIcon(homeIcon);
            fileRoot.setName(DIR_ROOT);
            fileRoot.setSize(0);
            fileRoot.setPath(rootPath);
            datas.add(fileRoot);
        }
        if (showUpDir && !path.equals("/")) {
            //添加“返回上一级目录”
            FileItem fileParent = new FileItem();
            fileParent.setDirectory(true);
            fileParent.setIcon(upIcon);
            fileParent.setName(DIR_PARENT);
            fileParent.setSize(0);
            fileParent.setPath(new File(path).getParent());
            datas.add(fileParent);
        }
        File[] files;
        if (allowExtensions == null) {
            if (onlyListDir) {
                files = FileUtils.listDirs(currentPath);
            } else {
                files = FileUtils.listDirsAndFiles(currentPath);
            }
        } else {
            if (onlyListDir) {
                files = FileUtils.listDirs(currentPath, allowExtensions);
            } else {
                files = FileUtils.listDirsAndFiles(currentPath, allowExtensions);
            }
        }
        if (files != null) {
            for (File file : files) {
                if (!showHideDir && file.getName().startsWith(".")) {
                    continue;
                }
                FileItem fileItem = new FileItem();
                boolean isDirectory = file.isDirectory();
                fileItem.setDirectory(isDirectory);
                if (isDirectory) {
                    fileItem.setIcon(folderIcon);
                    fileItem.setSize(0);
                } else {
                    fileItem.setIcon(fileIcon);
                    fileItem.setSize(file.length());
                }
                fileItem.setName(file.getName());
                fileItem.setPath(file.getAbsolutePath());
                datas.add(fileItem);
            }
        }
        data.clear();
        data.addAll(datas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_filepicker, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        FileItem fileItem = data.get(position);
        holder.imageView.setImageDrawable(fileItem.getIcon());
        holder.textView.setText(fileItem.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onFileClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textView = itemView.findViewById(R.id.text_view);
        }
    }

    public interface CallBack {
        void onFileClick(int position);
    }

}

