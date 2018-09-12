package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.luhuiguo.chinese.ChineseUtils;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.model.ReplaceRuleManage;

public class ChapterContentHelp {

    /**
     * 转繁体
     */
    public static String toTraditional(ReadBookControl readBookControl, String content) {
        switch (readBookControl.getTextConvert()) {
            case 0:
                break;
            case 1:
                content = ChineseUtils.toSimplified(content);
                break;
            case 2:
                content = ChineseUtils.toTraditional(content);
                break;
        }
        return content;
    }

    /**
     * 替换净化
     */
    public static String replaceContent(String sourceUrl, String bookName, String content) {
        String allLine[] = content.split("\n\u3000\u3000");
        //替换
        if (ReplaceRuleManage.getEnabled() != null && ReplaceRuleManage.getEnabled().size() > 0) {
            StringBuilder contentBuilder = new StringBuilder();
            for (String line : allLine) {
                for (ReplaceRuleBean replaceRule : ReplaceRuleManage.getEnabled()) {
                    if (TextUtils.isEmpty(replaceRule.getUseTo()) || replaceRule.getUseTo().contains(sourceUrl) || replaceRule.getUseTo().contains(bookName)) {
                        try {
                            line = line.replaceAll(replaceRule.getRegex(), replaceRule.getReplacement());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                if (line.length() > 0) {
                    if (contentBuilder.length() == 0) {
                        contentBuilder.append(line);
                    } else {
                        contentBuilder.append("\n").append("\u3000\u3000").append(line);
                    }
                }
            }
            content = contentBuilder.toString();
            for (ReplaceRuleBean replaceRule : ReplaceRuleManage.getEnabled()) {
                if (TextUtils.isEmpty(replaceRule.getUseTo()) || replaceRule.getUseTo().contains(sourceUrl) || replaceRule.getUseTo().contains(bookName)) {
                    if (replaceRule.getRegex().contains("\\n")) {
                        try {
                            content = content.replaceAll(replaceRule.getRegex(), replaceRule.getReplacement());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        return content;
    }

}
