package com.monke.monkeybook.utils.fileSelectorUtil;

import android.content.Context;

import android.os.storage.StorageManager;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class SDCardScanner {


    public static ArrayList<String> getStorageData(Context pContext) {

        final StorageManager storageManager = (StorageManager) pContext.getSystemService(Context.STORAGE_SERVICE);

        try {

            final Method getVolumeList = storageManager.getClass().getMethod("getVolumeList");

            final Class<?> storageValumeClazz = Class.forName("android.os.storage.StorageVolume");
            final Method getPath = storageValumeClazz.getMethod("getPath");

            final Object invokeVolumeList = getVolumeList.invoke(storageManager);
            final int length = Array.getLength(invokeVolumeList);

            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                final Object storageValume = Array.get(invokeVolumeList, i);//得到StorageVolume对象
                final String path = (String) getPath.invoke(storageValume);

                list.add(path);
            }

            return list;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}