package com.monke.basemvplib;

import org.mozilla.intl.chardet.nsDetector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CharsetDetector {

    private CharsetDetector() {
    }

    private static boolean found = false;
    private static String foundCharset;

    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * Tries to guess the charset of a given file using Mozilla's automatic
     * charset detection algorithm
     *
     * @param filename path of the file to be detected
     * @return detected charset name (e.g. "UTF-8") or "nomatch" if no known
     * charset can be guessed
     */
    public static String detectCharset(String filename) {
        return detectCharset(new File(filename));
    }

    /**
     * Tries to guess the charset of a given file using Mozilla's automatic
     * charset detection algorithm
     *
     * @param file the file to be detected
     * @return detected charset name (e.g. "UTF-8") or "nomatch" if no known
     * charset can be guessed
     */
    public static String detectCharset(File file) {
        try {
            return detectCharset(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return DEFAULT_CHARSET;
    }

    /**
     * Tries to guess the charset of a given file using Mozilla's automatic
     * charset detection algorithm
     *
     * @param inStream InputStream from the file to be detected
     * @return detected charset name (e.g. "UTF-8") or "nomatch" if no known
     * charset can be guessed
     */
    public static String detectCharset(InputStream inStream) {

        if (inStream == null) {
            return DEFAULT_CHARSET;
        }

        nsDetector detector = new nsDetector();

        detector.Init(charset -> {
            CharsetDetector.found = true;
            CharsetDetector.foundCharset = charset;
        });

        BufferedInputStream bis = new BufferedInputStream(inStream);
        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = true;

        try {
            while ((len = bis.read(buf, 0, buf.length)) != -1) {

                // Check if the stream is only ascii.
                if (isAscii)
                    isAscii = detector.isAscii(buf, len);

                // DoIt if non-ascii and not done yet.
                if (!isAscii && !done)
                    done = detector.DoIt(buf, len, false);
            }
            detector.DataEnd();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isAscii) {
            found = true;
            return "ascii";
        }

        if (!found) {
            String prob[] = detector.getProbableCharsets();
            foundCharset = prob[0];
        }
        found = false;
        return foundCharset;
    }

}