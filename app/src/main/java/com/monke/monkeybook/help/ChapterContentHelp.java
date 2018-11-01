package com.monke.monkeybook.help;

import android.text.TextUtils;

import com.luhuiguo.chinese.ChineseUtils;
import com.monke.monkeybook.bean.BookShelfBean;
import com.monke.monkeybook.bean.ReplaceRuleBean;
import com.monke.monkeybook.model.ReplaceRuleManager;

public class ChapterContentHelp {

    public static ChapterContentHelp getInstance() {
        return new ChapterContentHelp();
    }

    /**
     * 转繁体
     */
    public String toTraditional(ReadBookControl readBookControl, String content) {
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
    public String replaceContent(BookShelfBean mBook, String content) {
        String allLine[] = content.split("\n");
        //替换
        if (ReplaceRuleManager.getEnabled() != null && ReplaceRuleManager.getEnabled().size() > 0) {
            StringBuilder contentBuilder = new StringBuilder();
            for (String line : allLine) {
                if (!line.trim().equals("")) {
                    for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
                        if (TextUtils.isEmpty(replaceRule.getUseTo()) || isUseTo(mBook, replaceRule.getUseTo())) {
                            try {
                                line = line.replaceAll(replaceRule.getRegex(), replaceRule.getReplacement()).trim();
                                if (line.length() == 0) {
                                    break;
                                }
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
            }
            content = contentBuilder.toString();
            for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
                if (TextUtils.isEmpty(replaceRule.getUseTo()) || isUseTo(mBook, replaceRule.getUseTo())) {
                    if (replaceRule.getIsRegex() && !TextUtils.isEmpty(replaceRule.getRegex()) && replaceRule.getRegex().contains("\\n")) {
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

    private static boolean isUseTo(BookShelfBean mBook, String useTo) {
        return useTo.contains(mBook.getTag())
                || useTo.contains(mBook.getBookInfoBean().getName());
    }

}
