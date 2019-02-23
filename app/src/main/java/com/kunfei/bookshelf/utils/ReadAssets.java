package com.kunfei.bookshelf.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class ReadAssets {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String getText(Context context, String fileName) {
        try {
            //Return an AssetManager instance for your application's package
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            // Finally stick the string into the text view.
            return new String(buffer, "utf-8");
        } catch (IOException e) {
            // Should never happen!
            e.printStackTrace();
        }
        return "读取错误，请检查文件名";
    }
}
