package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.luhuiguo.chinese.ChineseUtils;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.model.ReplaceRuleManager;

import java.util.ArrayList;
import java.util.List;

public class ChapterContentHelp {
    private static ChapterContentHelp instance;
    private List<ReplaceRuleBean> validReplaceRules;
    private BookShelfBean book;
    private long lastUpdateTime = 0;

    public static synchronized ChapterContentHelp getInstance() {
        if (instance == null)
            instance = new ChapterContentHelp();
        return instance;
    }

    public void updateBookShelf(BookShelfBean bookShelf, long upTime) {
        if (book == null || !book.equals(bookShelf) || lastUpdateTime != upTime) {
            book = bookShelf;
            lastUpdateTime = upTime;
            updateReplaceRules();
        }
    }

    private void updateReplaceRules() {
        if (validReplaceRules == null)
            validReplaceRules = new ArrayList<>();
        else
            validReplaceRules.clear();
        if (ReplaceRuleManager.getEnabled() == null) return;
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
            if (TextUtils.isEmpty(replaceRule.getUseTo()) || isUseTo(book, replaceRule.getUseTo())) {
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
    public synchronized String replaceContent(BookShelfBean mBook, String content) {
        updateBookShelf(mBook, ReplaceRuleManager.getLastUpTime());
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

    private boolean isUseTo(BookShelfBean mBook, String useTo) {
        return useTo.contains(mBook.getTag())
                || useTo.contains(mBook.getBookInfoBean().getName());
    }

}
