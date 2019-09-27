package com.kunfei.bookshelf;

import org.junit.Test;
import static org.junit.Assert.*;
import com.kunfei.bookshelf.utils.StringUtils;

public class StringUtilsTest {
    @Test
    public void escapeTestCase0() throws Exception {
        String input = "";
        String output;
        String expected = "";

        output = StringUtils.escape(input);

        assertEquals(output,expected);
    }
    @Test
    public void escapeTestCase1() throws Exception {
        String input = "Abc123";
        String output;
        String expected = "Abc123";

        output = StringUtils.escape(input);

        assertEquals(output,expected);
    }
    @Test
    public void escapeTestCase2() throws Exception {
        String input = "!@#$";
        String output;
        String expected = "%21%40%23%24";

        output = StringUtils.escape(input);

        assertEquals(output,expected);
    }
    @Test
    public void escapeTestCase3() throws Exception {
        String input = "\u0006\b";
        String output;
        String expected = "%06%08";

        output = StringUtils.escape(input);

        assertEquals(output,expected);
    }
    @Test
    public void escapeTestCase4() throws Exception {
        String input = "如果没";
        String output;
        String expected = "%u5982%u679c%u6ca1";

        output = StringUtils.escape(input);

        assertEquals(output,expected);
    }
}