package com.monke.monkeybook.model.content;

public class SourceRule {
    Mode mode;
    String rule;

    SourceRule(String ruleStr) {
        if (ruleStr.startsWith("@XPath:")) {
            mode = Mode.XPath;
            rule = ruleStr.substring(7);
        } else {
            mode = Mode.Default;
            rule = ruleStr;
        }
    }

    public enum  Mode {
        XPath, Default
    }

}
