package com.kunfei.bookshelf.help;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.luhuiguo.chinese.ChineseUtils;

public class ChapterContentHelp {
    private static ChapterContentHelp instance;

    public static synchronized ChapterContentHelp getInstance() {
        if (instance == null)
            instance = new ChapterContentHelp();
        return instance;
    }

    /**
     * 转繁体
     */
    private String toTraditional(int convert, String content) {
        switch (convert) {
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
    public String replaceContent(String bookName, String bookTag, String content) {
        int convertCTS = ReadBookControl.getInstance().getTextConvert();
        if (ReplaceRuleManager.getEnabled().size() == 0)
            return toTraditional(convertCTS, content);
        String allLine[] = content.split("\n");
        StringBuilder contentBuilder = new StringBuilder();
        //替换
        for (String line : allLine) {
            line = line.replaceAll("^[\\s\u3000]+", "").trim();
            if (line.length() == 0) continue;
            for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
                try {
                    if (isUseTo(replaceRule.getUseTo(), bookTag, bookName)) {
                        line = line.replaceAll(replaceRule.getFixedRegex(), replaceRule.getReplacement()).trim();
                    }
                    if (line.length() == 0) break;
                } catch (Exception ignored) {
                }
            }
            if (line.length() == 0) continue;
            contentBuilder.append(line).append("\n");
        }

        content = contentBuilder.toString();
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
            String rule = replaceRule.getFixedRegex();
            if (replaceRule.getIsRegex() && !TextUtils.isEmpty(rule) && rule.contains("\\n") && isUseTo(rule, bookTag, bookName)) {
                try {
                    content = content.replaceAll(rule, replaceRule.getReplacement());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return toTraditional(convertCTS, content);
    }

    private boolean isUseTo(String useTo, String bookTag, String bookName) {
        return TextUtils.isEmpty(useTo)
                || useTo.contains(bookTag)
                || useTo.contains(bookName);
    }

}
