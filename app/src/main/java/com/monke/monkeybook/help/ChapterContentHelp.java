package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.luhuiguo.chinese.ChineseUtils;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.model.ReplaceRuleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChapterContentHelp {
    private static ChapterContentHelp instance;
    private static List<ReplaceRuleBean> validReplaceRules = new ArrayList<>();
    private static String bookName;
    private static String bookTag;
    private static long lastUpdateTime = 0;

    public static synchronized ChapterContentHelp getInstance() {
        if (instance == null)
            instance = new ChapterContentHelp();
        return instance;
    }

    private void updateBookShelf(String bookName, String bookTag, long upTime) {
        if (!Objects.equals(ChapterContentHelp.bookName, bookName) || !Objects.equals(ChapterContentHelp.bookTag, bookTag) || lastUpdateTime != upTime) {
            ChapterContentHelp.bookName = bookName;
            ChapterContentHelp.bookTag = bookTag;
            lastUpdateTime = upTime;
            updateReplaceRules();
        }
    }

    private void updateReplaceRules() {
        validReplaceRules.clear();
        if (ReplaceRuleManager.getEnabled() == null) return;
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
            if (TextUtils.isEmpty(replaceRule.getUseTo()) || isUseTo(replaceRule.getUseTo())) {
                validReplaceRules.add(replaceRule);
            }
        }

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
    public synchronized String replaceContent(String bookName, String bookTag, String content) {
        updateBookShelf(bookName, bookTag, ReplaceRuleManager.getLastUpTime());
        int convertCTS = ReadBookControl.getInstance().getTextConvert();
        if (validReplaceRules.size() == 0)
            return toTraditional(convertCTS, content);
        String allLine[] = content.split("\n");
        StringBuilder contentBuilder = new StringBuilder();
        //替换
        for (String line : allLine) {
            line = line.replaceAll("^[\\s\u3000]+", "").trim();
            if (line.length() == 0) continue;
            for (ReplaceRuleBean replaceRule : validReplaceRules) {
                try {
                    line = line.replaceAll(replaceRule.getRegex(), replaceRule.getReplacement()).trim();
                    if (line.length() == 0) break;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            if (line.length() == 0) continue;
            contentBuilder.append(line).append("\n");
        }

        content = contentBuilder.toString();
        for (ReplaceRuleBean replaceRule : validReplaceRules) {
            String rule = replaceRule.getRegex();
            if (replaceRule.getIsRegex() && !TextUtils.isEmpty(rule) && rule.contains("\\n")) {
                try {
                    content = content.replaceAll(rule, replaceRule.getReplacement());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return toTraditional(convertCTS, content);
    }

    private boolean isUseTo(String useTo) {
        return useTo.contains(bookTag)
                || useTo.contains(bookName);
    }

}
