package com.kunfei.bookshelf.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused"})
public class GsonUtils {
    /**
     * 将Json数据解析成相应的映射对象
     */
    public static <T> T parseJObject(String jsonData, Class<T> type) {
        T result = null;
        if (!TextUtils.isEmpty(jsonData)) {
            Gson gson = new GsonBuilder().create();
            try {
                result = gson.fromJson(jsonData, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 将Json数组解析成相应的映射对象List
     */
    public static <T> List<T> parseJArray(String jsonData, Class<T> type) {
        List<T> result = null;
        if (!TextUtils.isEmpty(jsonData)) {
            Gson gson = new GsonBuilder().create();
            try {
                JsonParser parser = new JsonParser();
                JsonArray JArray = parser.parse(jsonData).getAsJsonArray();
                if (JArray != null) {
                    result = new ArrayList<>();
                    for (JsonElement obj : JArray) {
                        try {
                            T cse = gson.fromJson(obj, type);
                            result.add(cse);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 将对象转换成Json
     */
    public static <T> String toJsonWithSerializeNulls(T entity) {
        entity.getClass();
        Gson gson = new GsonBuilder().serializeNulls().create();
        String result = "";
        try {
            result = gson.toJson(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将list排除值为null的字段转换成Json数组
     */
    public static <T> String toJsonArrayWithSerializeNulls(List<T> list) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        String result = "";
        try {
            result = gson.toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将list中将Expose注解的字段转换成Json数组
     */
    public static <T> String toJsonArrayWithExpose(List<T> list) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String result = "";
        try {
            result = gson.toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
