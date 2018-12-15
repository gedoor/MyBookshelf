package com.kunfei.bookshelf.model.analyzeRule;

import org.jsoup.select.Elements;

import java.util.List;

public class AnalyzeCollection {
    private int _pos = 0;
    private int _size = 0;

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

    public AnalyzeRule next(){
        if(_isJSON){
            return new AnalyzeRule(_Objects.get(_pos++), _isJSON);
        }
        return new AnalyzeRule(_Elements.get(_pos++), _isJSON);
    }

    public int size(){
        return _size;
    }
}
