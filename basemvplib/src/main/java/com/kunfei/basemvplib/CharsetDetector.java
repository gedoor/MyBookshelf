package com.kunfei.basemvplib;


import android.text.TextUtils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CharsetDetector {

    private CharsetDetector() {
    }

    private static final String DEFAULT_CHARSET = "UTF-8";


    public static String detectCharset(String filename) {
        return detectCharset(new File(filename));
    }

    public static String detectCharset(File file) {
        try {
            return detectCharset(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return DEFAULT_CHARSET;
    }


    public static String detectCharset(InputStream inStream) {
        if (inStream == null) {
            return DEFAULT_CHARSET;
        }
        String encoding = DEFAULT_CHARSET;

        UniversalDetector detector = new UniversalDetector();

        int nread;
        byte[] buf = new byte[4096];
        try {
            while ((nread = inStream.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            encoding = detector.getDetectedCharset();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            detector.reset();
        }

        if (TextUtils.isEmpty(encoding)) {
            encoding = DEFAULT_CHARSET;
        }
        return encoding;
    }

}