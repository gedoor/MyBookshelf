package com.kunfei.bookshelf.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {

    public static <T> List<T> filter(List<T> list, ListLook<T> hook) {
        ArrayList<T> r = new ArrayList<>();
        for (T t : list) {
            if (hook.test(t)) {
                r.add(t);
            }
        }
        r.trimToSize();
        return r;
    }

    public interface ListLook<T> {
        boolean test(T t);
    }


}
