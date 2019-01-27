package com.kunfei.bookshelf.bean;

import android.text.TextUtils;

import com.kunfei.bookshelf.utils.StringUtils;

import java.text.DecimalFormat;

public class BookKindBean {
    private String wordsS;
    private String state;
    private String kind;

    public BookKindBean(String kindS) {
        for (String kind : kindS.split(",")) {
            if (StringUtils.isContainNumber(kind) && TextUtils.isEmpty(wordsS)) {
                if (StringUtils.isNumeric(kind)) {
                    int words = Integer.valueOf(kind);
                    if (words > 0) {
                        wordsS = Long.toString(words) + "字";
                        if (words > 10000) {
                            DecimalFormat df = new DecimalFormat("#.#");
                            wordsS = df.format(words * 1.0f / 10000f) + "万字";
                        }
                    }
                } else {
                    wordsS = kind;
                }
            } else if (kind.matches(".*[连载|完结].*")){
                state = kind;
            }
            else if (TextUtils.isEmpty(this.kind) && !TextUtils.isEmpty(kind)){
                this.kind = kind;
            } else if (TextUtils.isEmpty(state) && !TextUtils.isEmpty(kind)){
                state = kind;
            } else if (wordsS != null && state != null && this.kind != null) {
                break;
            }
        }
    }

    public String getWordsS() {
        return wordsS;
    }

    public String getState() {
        return state;
    }

    public String getKind() {
        return kind;
    }
}
