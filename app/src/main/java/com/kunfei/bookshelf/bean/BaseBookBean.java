package com.kunfei.bookshelf.bean;

import java.util.Map;

public interface BaseBookBean {

    String getTag();

    String getNoteUrl();

    void setNoteUrl(String noteUrl);

    String getVariable();

    void setVariable(String variable);

    void putVariable(String key, String value);

    Map<String, String> getVariableMap();
}
