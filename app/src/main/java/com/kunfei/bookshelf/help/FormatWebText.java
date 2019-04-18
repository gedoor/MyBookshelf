package com.kunfei.bookshelf.help;

import android.text.TextUtils;

import com.kunfei.bookshelf.utils.StringUtils;

/**
 * Created by GKF on 2017/12/27.
 * 去除空格等
 */

public class FormatWebText {

    public static String formatHtml(String html) {
        if (TextUtils.isEmpty(html)) return html;
        return html.replaceAll("(?i)<(br[\\s/]*|/*p.*?|/*div.*?)>", "\n")  // 替换特定标签为换行符
                .replaceAll("<[script>]*.*?>|&nbsp;", "")               // 删除script标签对和空格转义符
                .replaceAll("\\s*\\n+\\s*", "\n　　");                   // 移除空行,并增加段前缩进2个汉字
    }

    public static String getAuthor(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return StringUtils.trim(str.replace("&nbsp;", "")
                .replaceAll("[：:()【】\\[\\]（）\\u3000 ]+", "")
                .replaceAll("\\s", " ")
                .replaceAll("作.*?者", ""));
    }
}
