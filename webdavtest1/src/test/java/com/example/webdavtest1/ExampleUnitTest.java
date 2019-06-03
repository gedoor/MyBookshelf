package com.example.webdavtest1;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        log("a1");
        log("a2");
        String s = null;
        s = "xx";
        log("a3"+ s.equals("xx"));
        log("a4");
    }

    public static void log(String s) {
        System.out.println(s);
    }
}