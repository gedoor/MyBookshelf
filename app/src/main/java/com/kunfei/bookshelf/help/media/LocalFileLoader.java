package com.kunfei.bookshelf.help.media;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;

/**
 * Created by newbiechen on 2018/1/14.
 */

public class LocalFileLoader extends CursorLoader {
    private static final String TAG = "LocalFileLoader";

    private static final Uri FILE_URI = Uri.parse("content://media/external/file");
    private static final String SELECTION = MediaStore.Files.FileColumns.DATA + " like ? or " + MediaStore.Files.FileColumns.DATA + " like ?";
    private static final String[] SEARCH_TYPE = new String[]{"%.txt", "%.epub"};
    private static final String SORT_ORDER = MediaStore.Files.FileColumns.DISPLAY_NAME + " DESC";
    private static final String[] FILE_PROJECTION = {
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME
    };

    public LocalFileLoader(Context context) {
        super(context);
        initLoader();
    }

    /**
     * 为 Cursor 设置默认参数
     */
    private void initLoader() {
        setUri(FILE_URI);
        setProjection(FILE_PROJECTION);
        setSelection(SELECTION);
        setSelectionArgs(SEARCH_TYPE);
        setSortOrder(SORT_ORDER);
    }

    public void parseData(Cursor cursor, final MediaStoreHelper.MediaResultCallback resultCallback) {
        List<File> files = new ArrayList<>();
        // 判断是否存在数据
        if (cursor == null) {
            // 暂时直接返回空数据
            resultCallback.onResultCallback(files);
            return;
        }
        // 重复使用Loader时，需要重置cursor的position；
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String path;

            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
            // 路径无效
            if (!TextUtils.isEmpty(path)) {
                File file = new File(path);
                if (!file.isDirectory() && file.exists() && file.length() > 1024) {
                    files.add(file);
                }
            }
        }
        if (resultCallback != null) {
            resultCallback.onResultCallback(files);
        }
    }

    /**
     * 从Cursor中读取对应columnName的值
     *
     * @param cursor
     * @param columnName
     * @param defaultValue
     * @return 当columnName无效时返回默认值；
     */
    protected Object getValueFromCursor(@NonNull Cursor cursor, String columnName, Object defaultValue) {
        try {
            int index = cursor.getColumnIndexOrThrow(columnName);
            int type = cursor.getType(index);
            switch (type) {
                case Cursor.FIELD_TYPE_STRING:
                    // TO SOLVE:某些手机的数据库将数值类型存为String类型
                    String value = cursor.getString(index);
                    try {
                        if (defaultValue instanceof String) {
                            return value;
                        } else if (defaultValue instanceof Long) {
                            return Long.valueOf(value);
                        } else if (defaultValue instanceof Integer) {
                            return Integer.valueOf(value);
                        } else if (defaultValue instanceof Double) {
                            return Double.valueOf(value);
                        } else if (defaultValue instanceof Float) {
                            return Float.valueOf(value);
                        }
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                case Cursor.FIELD_TYPE_INTEGER:
                    if (defaultValue instanceof Long) {
                        return cursor.getLong(index);
                    } else if (defaultValue instanceof Integer) {
                        return cursor.getInt(index);
                    }
                case Cursor.FIELD_TYPE_FLOAT:
                    if (defaultValue instanceof Float) {
                        return cursor.getFloat(index);
                    } else if (defaultValue instanceof Double) {
                        return cursor.getDouble(index);
                    }
                case Cursor.FIELD_TYPE_BLOB:
                    if (defaultValue instanceof Blob) {
                        return cursor.getBlob(index);
                    }
                case Cursor.FIELD_TYPE_NULL:
                default:
                    return defaultValue;
            }
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
