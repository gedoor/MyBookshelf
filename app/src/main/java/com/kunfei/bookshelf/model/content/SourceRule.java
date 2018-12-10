package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

public class SourceRule {
    Mode mode = Mode.Default;
    String rule = "";

    SourceRule(String ruleStr) {
        if (TextUtils.isEmpty(ruleStr)) {
            return;
        }
        if (ruleStr.startsWith("@XPath:")) {
            mode = Mode.XPath;
            rule = ruleStr.substring(7);
        } else if (ruleStr.startsWith("@JSon:")) {
            mode = Mode.JSon;
            rule = ruleStr.substring(6);
        } else {
            mode = Mode.Default;
            rule = ruleStr;
        }
    }

    public enum Mode {
        XPath, JSon, Default
    }

}
