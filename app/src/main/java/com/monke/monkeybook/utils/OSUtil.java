package com.monke.monkeybook.utils;

import android.annotation.SuppressLint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OSUtil {
    private static final String SYS_EMUI = "sys_emui";
    private static final String SYS_MIUI = "sys_miui";
    private static final String SYS_FLYME = "sys_flyme";

    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_EMUI_VERSION_NAME = "ro.build.version.emui";
    private static final String KEY_FLYME_SETUPWIZARD = "ro.meizu.setupwizard.flyme";

    public static String getSystem(){
        String OS = "";
        if(!"".equals(getProperty(KEY_MIUI_VERSION_NAME))) {
            OS = SYS_MIUI;//小米
        }else if(!"".equals(getProperty(KEY_EMUI_VERSION_NAME))){
            OS = SYS_EMUI;//华为
        }else if(!"".equals(getProperty(KEY_FLYME_SETUPWIZARD))){
            OS = SYS_FLYME;//魅族
        }
        return OS;
    }

    private static String getProperty(String key_version){
        String value = "";
        try {
            @SuppressLint("PrivateApi") Class<?> classType = Class.forName("android.os.SystemProperties");
            Method method = classType.getDeclaredMethod("get", String.class);
            value = (String) method.invoke(classType,key_version);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }
}
