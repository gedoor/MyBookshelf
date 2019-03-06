package com.kunfei.bookshelf.help;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.utils.DocumentUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.documentfile.provider.DocumentFile;


/**
 * Created by PureDark on 2016/9/24.
 */

public class DocumentHelper {

    public static boolean isFileExist(String fileName, String rootPath, String... subDirs) {
        return DocumentUtil.isFileExist(MApplication.getInstance(), fileName, rootPath, subDirs);
    }

    public static DocumentFile getDirDocument(String rootPath, String... subDirs) {
        return DocumentUtil.getDirDocument(MApplication.getInstance(), rootPath, subDirs);
    }

    public static DocumentFile createFileIfNotExist(String fileName, String path, String... subDirs) {
        if (!path.startsWith("content://"))
            path = "file://" + Uri.decode(path);
        return DocumentUtil.createFileIfNotExist(MApplication.getInstance(), fileName, path, subDirs);
    }

    public static DocumentFile createDirIfNotExist(String path, String... subDirs) {
        if (!path.startsWith("content://"))
            path = "file://" + Uri.decode(path);
        return DocumentUtil.createDirIfNotExist(MApplication.getInstance(), path, subDirs);
    }

    public static boolean deleteFile(String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.deleteFile(MApplication.getInstance(), fileName, rootPath, subDirs);
    }

    public static boolean writeString(String string, DocumentFile file) {
        return DocumentUtil.writeBytes(MApplication.getInstance(), string.getBytes(), file);
    }

    public static boolean writeString(String string, String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.writeBytes(MApplication.getInstance(), string.getBytes(), fileName, rootPath, subDirs);
    }

    public static String readString(String fileName, String rootPath, String... subDirs) {
        byte[] data = DocumentUtil.readBytes(MApplication.getInstance(), fileName, rootPath, subDirs);
        String string = null;
        try {
            string = new String(data, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String readString(Uri uri) {
        byte[] data = DocumentUtil.readBytes(MApplication.getInstance(), uri);
        String string = null;
        try {
            string = new String(data, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String readString(DocumentFile file) {
        byte[] data = DocumentUtil.readBytes(MApplication.getInstance(), file);
        String string = null;
        try {
            string = new String(data, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static boolean writeBytes(byte[] data, String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.writeBytes(MApplication.getInstance(), data, fileName, rootPath, subDirs);
    }

    public static boolean writeBytes(byte[] data, DocumentFile file) {
        if (file == null)
            return false;
        return DocumentUtil.writeBytes(MApplication.getInstance(), data, file);
    }

    public static boolean writeFromFile(File fromFile, DocumentFile file) {
        if (file == null)
            return false;
        try {
            return DocumentUtil.writeFromInputStream(MApplication.getInstance(), new FileInputStream(fromFile), file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeFromInputStream(InputStream inStream, DocumentFile file) {
        if (file == null)
            return false;
        return DocumentUtil.writeFromInputStream(MApplication.getInstance(), inStream, file);
    }

    public static void saveBitmapToFile(Bitmap bitmap, DocumentFile file) throws IOException {
        saveBitmapToFile(bitmap, file.getUri());
    }

    public static void saveBitmapToFile(Bitmap bitmap, Uri fileUri) throws IOException {
        OutputStream out = MApplication.getInstance().getContentResolver().openOutputStream(fileUri);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();
    }

    public static OutputStream getFileOutputSteam(String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.getFileOutputSteam(MApplication.getInstance(), fileName, rootPath, subDirs);
    }

    public static InputStream getFileInputSteam(String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.getFileInputSteam(MApplication.getInstance(), fileName, rootPath, subDirs);
    }

    public static String filenameFilter(String str) {
        return DocumentUtil.filenameFilter(str);
    }

    public static byte[] getBytes(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
