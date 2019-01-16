package com.kunfei.bookshelf.model.analyzeRule;

import org.jsoup.select.Elements;

import java.util.List;

public class AnalyzeCollection {
    private int _pos = 0;
    private int _size;

    private boolean _isJSON;
    private List<Object> _Objects;
    private Elements _Elements;

    AnalyzeCollection(List<Object> Objects, boolean isJSON){
        _Objects = Objects;
        _size = Objects.size();
        _isJSON = isJSON;
    }

    AnalyzeCollection(Elements elements){
        _Elements = elements;
        _size = elements.size();
        _isJSON = false;
    }

    public boolean hasNext(){
        return _pos<_size;
    }

    public void next(AnalyzeRule analyzeRule) {
        if (_isJSON) {
            analyzeRule.setContent(_Objects.get(_pos++), true);
        } else {
            analyzeRule.setContent(_Elements.get(_pos++), false);
        }
    }

    public int size(){
        return _size;
    }
}
