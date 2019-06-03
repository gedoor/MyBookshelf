package com.example.webdavtest1;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        A.log("a1");
        Context appContext = InstrumentationRegistry.getTargetContext();
        A.log("a2");
String s = null;
        assertEquals("com.example.webdavtest1", appContext.getPackageName());
        s = "f";
        A.log("a3", s.equals("xx"));
        A.log("a4");
    }
    @Test
    public void test1(){
        int i = 50;
        int j= 100;
        A.log("print", i * j);
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL("http://163.com");
                    A.log("11");
                    URLConnection conn = url.openConnection();
                    A.log("22", conn.getContentType());
                    String text = T.inputStream2String(conn.getInputStream());
                    A.log(text);
                    A.log("33");
                } catch (Exception e) {
                    A.error(e);
                }
                A.log("44");
            }
        }.start();
    }
}
