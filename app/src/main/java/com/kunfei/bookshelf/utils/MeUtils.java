package com.kunfei.bookshelf.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * by yangyxd
 * data: 2019.08.29
 */
public class MeUtils {

    /** 获取 assets 中指定目录中的文件名称列表 */
    public static CharSequence[] getAssetsFileList(AssetManager am, String path) throws IOException {
        final String[] fs = am.list(path);
        if (fs == null || fs.length == 0)
            return null;
        final CharSequence[] items = new CharSequence[fs.length];
        for (int i=0; i<fs.length; i++) {
            items[i] = MeUtils.getFileName(fs[i]);
        }
        return items;
    }

    public static String getFileName(String pathandname){
        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if (end < 0) end = pathandname.length();
        return pathandname.substring(start+1,end);
    }

    public static String getOriginalFundData(AssetManager am, String filename) {
        InputStream input = null;
        try {
            input = am.open(filename);
            String json = convertStreamToString(input);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertStreamToString(InputStream is) {
        String s = null;
        try {
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext())
                s = scanner.next();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static Bitmap getFitAssetsSampleBitmap(AssetManager am, String file, int width, int height) {
        InputStream assetFile = null;
        try {
            assetFile = am.open(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(assetFile, null, options);
            options.inSampleSize = getFitInSampleSize(width, height, options);
            options.inJustDecodeBounds = false;
            assetFile.close();
            assetFile = am.open(file);
            Bitmap bm = BitmapFactory.decodeStream(assetFile, null, options);
            assetFile.close();
            return bm;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (assetFile != null) assetFile.close();
            } catch (Exception ee) {}
            return null;
        }
    }

    public static int getFitInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options) {
        int inSampleSize = 1;
        if (options.outWidth > reqWidth || options.outHeight > reqHeight) {
            int widthRatio = Math.round((float) options.outWidth / (float) reqWidth);
            int heightRatio = Math.round((float) options.outHeight / (float) reqHeight);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }
}
