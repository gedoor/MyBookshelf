//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.kunfei.bookshelf;

import java.util.HashMap;
import java.util.Map;

public class BitIntentDataManager {
    private static Map<String, Object> bigData;

    private static BitIntentDataManager instance = null;

    private BitIntentDataManager() {
        bigData = new HashMap<>();
    }

    public static BitIntentDataManager getInstance() {
        if (instance == null) {
            synchronized (BitIntentDataManager.class) {
                if (instance == null) {
                    instance = new BitIntentDataManager();
                }
            }
        }
        return instance;
    }

    public Object getData(String key) {
        return bigData.get(key);
    }

    public void putData(String key, Object data) {
        bigData.put(key, data);
    }

    public void cleanData(String key) {
        bigData.remove(key);
    }
}
