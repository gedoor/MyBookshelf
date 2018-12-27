package com.kunfei.bookshelf.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import androidx.documentfile.provider.DocumentFile;

/**
 * Created by PureDark on 2016/9/24.
 */

public class DocumentUtil {

    private static Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");

    public static boolean isFileExist(Context context, String fileName, String rootPath, String... subDirs) {
        Uri rootUri;
        if (rootPath.startsWith("content"))
            rootUri = Uri.parse(rootPath);
        else
            rootUri = Uri.parse(Uri.decode(rootPath));
        return isFileExist(context, fileName, rootUri, subDirs);
    }

    public static boolean isFileExist(Context context, String fileName, Uri rootUri, String... subDirs) {
        DocumentFile root;
        if ("content".equals(rootUri.getScheme()))
            root = DocumentFile.fromTreeUri(context, rootUri);
        else
            root = DocumentFile.fromFile(new File(rootUri.getPath()));
        return isFileExist(fileName, root, subDirs);
    }

    public static boolean isFileExist(String fileName, DocumentFile root, String... subDirs) {
        DocumentFile parent = getDirDocument(root, subDirs);
        if (parent == null)
            return false;
        fileName = filenameFilter(Uri.decode(fileName));
        DocumentFile file = parent.findFile(fileName);
        if (file != null && file.exists())
            return true;
        return false;
    }

    public static DocumentFile createDirIfNotExist(Context context, String rootPath, String... subDirs) {
        Uri rootUri;
        if (rootPath.startsWith("content"))
            rootUri = Uri.parse(rootPath);
        else
            rootUri = Uri.parse(Uri.decode(rootPath));
        return createDirIfNotExist(context, rootUri, subDirs);
    }

    public static DocumentFile createDirIfNotExist(Context context, Uri rootUri, String... subDirs) {
        DocumentFile root;
        if ("content".equals(rootUri.getScheme()))
            root = DocumentFile.fromTreeUri(context, rootUri);
        else
            root = DocumentFile.fromFile(new File(rootUri.getPath()));
        return createDirIfNotExist(root, subDirs);
    }

    public static DocumentFile createDirIfNotExist(DocumentFile root, String... subDirs) {
        DocumentFile parent = root;
        try {
            for (int i = 0; i < subDirs.length; i++) {
                String subDirName = filenameFilter(Uri.decode(subDirs[i]));
                DocumentFile subDir = parent.findFile(subDirName);
                if (subDir == null) {
                    subDir = parent.createDirectory(subDirName);
                }
                parent = subDir;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return parent;
    }

    public static DocumentFile createFileIfNotExist(Context context, String fileName, String rootPath, String... subDirs) {
        Uri rootUri;
        if (rootPath.startsWith("content"))
            rootUri = Uri.parse(rootPath);
        else
            rootUri = Uri.parse(Uri.decode(rootPath));
        return createFileIfNotExist(context, "", fileName, rootUri, subDirs);
    }

    public static DocumentFile createFileIfNotExist(Context context, String fileName, Uri rootUri, String... subDirs) {
        return createFileIfNotExist(context, "", fileName, rootUri, subDirs);
    }

    public static DocumentFile createFileIfNotExist(Context context, String mimeType, String fileName, String rootPath, String... subDirs) {
        Uri rootUri;
        if (rootPath.startsWith("content"))
            rootUri = Uri.parse(rootPath);
        else
            rootUri = Uri.parse(Uri.decode(rootPath));
        return createFileIfNotExist(context, mimeType, fileName, rootUri, subDirs);
    }

    public static DocumentFile createFileIfNotExist(Context context, String mimeType, String fileName, Uri rootUri, String... subDirs) {
        DocumentFile parent = createDirIfNotExist(context, rootUri, subDirs);
        if (parent == null)
            return null;
        fileName = filenameFilter(Uri.decode(fileName));
        DocumentFile file = parent.findFile(fileName);
        if (file == null) {
            file = parent.createFile(mimeType, fileName);
        }
        return file;
    }

    public static boolean deleteFile(Context context, String fileName, String rootPath, String... subDirs) {
        Uri rootUri;
        if (rootPath.startsWith("content"))
            rootUri = Uri.parse(rootPath);
        else
            rootUri = Uri.parse(Uri.decode(rootPath));
        return deleteFile(context, fileName, rootUri, subDirs);
    }

    public static boolean deleteFile(Context context, String fileName, Uri rootUri, String... subDirs) {
        DocumentFile root;
        if ("content".equals(rootUri.getScheme()))
            root = DocumentFile.fromTreeUri(context, rootUri);
        else
            root = DocumentFile.fromFile(new File(rootUri.getPath()));
        return deleteFile(fileName, root, subDirs);
    }

    public static boolean deleteFile(String fileName, DocumentFile root, String... subDirs) {
        DocumentFile parent = getDirDocument(root, subDirs);
        if (parent == null)
            return false;
        fileName = filenameFilter(Uri.decode(fileName));
        DocumentFile file = parent.findFile(fileName);
        return file != null && file.exists() && file.delete();
    }

    public static boolean writeBytes(Context context, byte[] data, String fileName, String rootPath, String... subDirs) {
        DocumentFile parent = getDirDocument(context, rootPath, subDirs);
        if (parent == null)
            return false;
        DocumentFile file = parent.findFile(fileName);
        return writeBytes(context, data, file.getUri());
    }

    public static boolean writeBytes(Context context, byte[] data, String fileName, Uri rootUri, String... subDirs) {
        DocumentFile parent = getDirDocument(context, rootUri, subDirs);
        if (parent == null)
            return false;
        fileName = filenameFilter(Uri.decode(fileName));
        DocumentFile file = parent.findFile(fileName);
        return writeBytes(context, data, file.getUri());
    }

    public static boolean writeBytes(Context context, byte[] data, String fileName, DocumentFile root, String... subDirs) {
        DocumentFile parent = getDirDocument(root, subDirs);
        if (parent == null)
            return false;
        fileName = filenameFilter(Uri.decode(fileName));
        DocumentFile file = parent.findFile(fileName);
        return writeBytes(context, data, file.getUri());
    }

    public static boolean writeBytes(Context context, byte[] data, DocumentFile file) {
        return writeBytes(context, data, file.getUri());
    }

    public static boolean writeBytes(Context context, byte[] data, Uri fileUri) {
        try {
            OutputStream out = context.getContentResolver().openOutputStream(fileUri);
            out.write(data);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeFromInputStream(Context context, InputStream inStream, DocumentFile file) {
        return writeFromInputStream(context, inStream, file.getUri());
    }

    public static boolean writeFromInputStream(Context context, InputStream inStream, Uri fileUri) {
        try {
            OutputStream out = context.getContentResolver().openOutputStream(fileUri);
            int byteread;
            byte[] buffer = new byte[1024];
            while ((byteread = inStream.read(buffer)) > 0) {
                out.write(buffer, 0, byteread);
            }
            inStream.close();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] readBytes(Context context, String fileName, String rootPath, String... subDirs) {
        DocumentFile parent = getDirDocument(context, rootPath, subDirs);
        if (parent == null)
            return null;
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return readBytes(context, file.getUri());
    }

    public static byte[] readBytes(Context context, String fileName, Uri rootUri, String... subDirs) {
        DocumentFile parent = getDirDocument(context, rootUri, subDirs);
        if (parent == null)
            return null;
        fileName = filenameFilter(Uri.decode(fileName));
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return readBytes(context, file.getUri());
    }

    public static byte[] readBytes(Context context, String fileName, DocumentFile root, String... subDirs) {
        DocumentFile parent = getDirDocument(root, subDirs);
        if (parent == null)
            return null;
        fileName = filenameFilter(Uri.decode(fileName));
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return readBytes(context, file.getUri());
    }

    public static byte[] readBytes(Context context, DocumentFile file) {
        if (file == null)
            return null;
        return readBytes(context, file.getUri());
    }

    public static byte[] readBytes(Context context, Uri fileUri) {
        try {
            InputStream fis = context.getContentResolver().openInputStream(fileUri);
            int len = fis.available();
            byte[] buffer = new byte[len];
            fis.read(buffer);
            fis.close();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DocumentFile getDirDocument(Context context, String rootPath, String... subDirs) {
        Uri rootUri;
        if (rootPath.startsWith("content"))
            rootUri = Uri.parse(rootPath);
        else
            rootUri = Uri.parse(Uri.decode(rootPath));
        return getDirDocument(context, rootUri, subDirs);
    }

    public static DocumentFile getDirDocument(Context context, Uri rootUri, String... subDirs) {
        DocumentFile root;
        if ("content".equals(rootUri.getScheme()))
            root = DocumentFile.fromTreeUri(context, rootUri);
        else
            root = DocumentFile.fromFile(new File(rootUri.getPath()));
        return getDirDocument(root, subDirs);
    }

    public static DocumentFile getDirDocument(DocumentFile root, String... subDirs) {
        DocumentFile parent = root;
        for (int i = 0; i < subDirs.length; i++) {
            String subDirName = Uri.decode(subDirs[i]);
            DocumentFile subDir = parent.findFile(subDirName);
            if (subDir != null)
                parent = subDir;
            else
                return null;
        }
        return parent;
    }

    public static OutputStream getFileOutputSteam(Context context, String fileName, String rootPath, String... subDirs) {
        DocumentFile parent = getDirDocument(context, rootPath, subDirs);
        if (parent == null)
            return null;
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return getFileOutputSteam(context, file.getUri());
    }

    public static OutputStream getFileOutputSteam(Context context, String fileName, Uri rootUri, String... subDirs) {
        DocumentFile parent = getDirDocument(context, rootUri, subDirs);
        if (parent == null)
            return null;
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return getFileOutputSteam(context, file.getUri());
    }

    public static OutputStream getFileOutputSteam(Context context, String fileName, DocumentFile root, String... subDirs) {
        DocumentFile parent = getDirDocument(root, subDirs);
        if (parent == null)
            return null;
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return getFileOutputSteam(context, file.getUri());
    }

    public static OutputStream getFileOutputSteam(Context context, DocumentFile file) {
        return getFileOutputSteam(context, file.getUri());
    }

    public static OutputStream getFileOutputSteam(Context context, Uri fileUri) {
        try {
            OutputStream out = context.getContentResolver().openOutputStream(fileUri);
            return out;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getFileInputSteam(Context context, String fileName, String rootPath, String... subDirs) {
        DocumentFile parent = getDirDocument(context, rootPath, subDirs);
        if (parent == null)
            return null;
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return getFileInputSteam(context, file.getUri());
    }

    public static InputStream getFileInputSteam(Context context, String fileName, Uri rootUri, String... subDirs) {
        DocumentFile parent = getDirDocument(context, rootUri, subDirs);
        if (parent == null)
            return null;
        fileName = filenameFilter(Uri.decode(fileName));
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return getFileInputSteam(context, file.getUri());
    }

    public static InputStream getFileInputSteam(Context context, String fileName, DocumentFile root, String... subDirs) {
        DocumentFile parent = getDirDocument(root, subDirs);
        if (parent == null)
            return null;
        DocumentFile file = parent.findFile(fileName);
        if (file == null)
            return null;
        return getFileInputSteam(context, file.getUri());
    }

    public static InputStream getFileInputSteam(Context context, DocumentFile file) {
        return getFileInputSteam(context, file.getUri());
    }

    public static InputStream getFileInputSteam(Context context, Uri fileUri) {
        try {
            InputStream in = context.getContentResolver().openInputStream(fileUri);
            return in;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String filenameFilter(String str) {
        return str == null ? null : FilePattern.matcher(str).replaceAll("_");
    }

}
