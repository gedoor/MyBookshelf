package com.kunfei.bookshelf.utils;

import android.os.Build;
import android.text.Html;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownUtils {

    @SuppressWarnings("deprecation")
    public static CharSequence simpleMarkdownConverter(String text) {
        Pattern listPtn = Pattern.compile("^[\\-*] ");
        Pattern headPtn = Pattern.compile("^(#{1,6}) ");
        String strongemPtn = "\\*\\*\\*([^*]+)\\*\\*\\*";
        String strongPtn = "\\*\\*([^*]+)\\*\\*";
        String emPtn = "\\*([^*]+)\\*";
        boolean isInList = false;
        StringBuilder builder = new StringBuilder();
        for (String line : text.split("\\n")) {
            Matcher listMtc = listPtn.matcher(line);
            Matcher headMtc = headPtn.matcher(line);
            boolean isList = listMtc.find();
            if (!isInList && isList) {
                builder.append("<ul>\n");
                isInList = true;
            } else if (isInList && !isList) {
                builder.append("</ul>\n");
                isInList = false;
            }
            if (isList) {
                line = "<li>" + line.substring(2) + "</li>\n";
            } else if (headMtc.find()) {
                final int level = headMtc.group(1).length();
                line = "<h" + level + ">" + line.substring(level + 1) + "</h" + level + ">\n";
            } else {
                line = "<div>" + line + "</div>\n";
            }
            line = line.replaceAll(strongemPtn, "<strong><em>$1</em></strong>");
            line = line.replaceAll(strongPtn, "<strong>$1</strong>");
            line = line.replaceAll(emPtn, "<em>$1</em>");
            builder.append(line);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(builder.toString(), Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM);
        } else {
            return Html.fromHtml(builder.toString());
        }
    }

    public static void setText(TextView view, String text) {
        view.setText(simpleMarkdownConverter(text));
    }
}
