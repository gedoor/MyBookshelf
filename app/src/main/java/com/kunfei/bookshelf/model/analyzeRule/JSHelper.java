package com.kunfei.bookshelf.model.analyzeRule;

import org.jsoup.select.Elements;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JSHelper {

    // 参考 新方圆 参数调整
    // https://github.com/qiusunshine/movienow/blob/master/core/parser/JSEngine.java
    // line486: private Object argsNativeObjectAdjust(Object input)
    Object UnWrapFromJS(Object o){
        if(o instanceof ConsString){
            return String.valueOf(o);
        }
        if(o instanceof Double && ((Double) o) % 1.0 == 0){
            return ((Double) o).intValue();
        }
        if(o instanceof NativeObject){
            NativeObject nativeObject = (NativeObject) o;
            Map<String, Object> map = new HashMap<>();
            for (Object key : nativeObject.keySet()) {
                map.put((String)key, UnWrapFromJS(nativeObject.get(key)));
            }
            return map;
        }
        if(o instanceof NativeArray){
            NativeArray nativeArray = (NativeArray) o;
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < nativeArray.size(); i++) {
                list.add(UnWrapFromJS(nativeArray.get(i)));
            }
            return list;
        }
        return o;
    }

    Object WrapForJS(Object o){
        if(o instanceof Elements){
            return String.valueOf(o);
        }
        if(o instanceof Map){
            Map map = (Map) o;
            NativeObject nativeObject = new NativeObject();
            for (Object key : map.keySet()) {
                ScriptableObject.putProperty(nativeObject, (String) key, WrapForJS(map.get(key)));
            }
            return nativeObject;
        }
        if(o instanceof List){
            List list = (List) o;
            NativeArray nativeArray = new NativeArray(list.size());
            for (int i = 0; i < list.size(); i++) {
                ScriptableObject.putProperty(nativeArray, i, WrapForJS(list.get(i)));
            }
            return nativeArray;
        }
        return o;
    }
}
