package com.kunfei.basemvplib;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Activity管理器,管理项目中Activity的状态
 */
public class AppActivityManager {

    private static List<WeakReference<Activity>> activities;

    private AppActivityManager() {
        activities = new ArrayList<>();
    }

    private static volatile AppActivityManager instance;

    public static AppActivityManager getInstance() {
        if (null == instance) {
            synchronized (AppActivityManager.class) {
                if (null == instance) {
                    instance = new AppActivityManager();
                }
            }
        }
        return instance;
    }

    public List<WeakReference<Activity>> getActivities() {
        return activities;
    }

    /**
     * 添加Activity
     */
    public void add(Activity activity) {
        activities.add(new WeakReference<Activity>(activity));
    }

    /**
     * 移除Activity
     */
    public void remove(Activity activity) {
        for (WeakReference<Activity> temp : activities) {
            if (null != temp.get() && temp.get() == activity) {
                activities.remove(temp);
                break;
            }
        }
    }

    /**
     * 移除Activity
     */
    public void remove(Class<?> activityClass) {
        for (Iterator<WeakReference<Activity>> iterator = activities.iterator(); iterator.hasNext(); ) {
            WeakReference<Activity> item = iterator.next();
            if (null != item && null != item.get() && item.get().getClass() == activityClass) {
                iterator.remove();
            }
        }
    }

    /**
     * 关闭指定 activity
     */
    public void finishActivity(BaseActivity... activities) {
        for (BaseActivity activity : activities) {
            if (null != activity) {
                activity.finish();
            }
        }
    }

    /**
     * 关闭指定 activity(class)
     */
    public void finishActivity(Class<?>... activityClasses) {
        ArrayList<WeakReference<Activity>> waitFinish = new ArrayList<>();
        for (WeakReference<Activity> temp : activities) {
            for (Class<?> activityClass : activityClasses) {
                if (null != temp.get() && temp.get().getClass() == activityClass) {
                    waitFinish.add(temp);
                    break;
                }
            }
        }
        for (WeakReference<Activity> activityWeakReference : waitFinish) {
            if (null != activityWeakReference.get()) {
                activityWeakReference.get().finish();
            }
        }
    }

    /**
     * 判断指定Activity是否存在
     */
    public Boolean isExist(Class<?> activityClass) {
        Boolean result = false;
        for (WeakReference<Activity> item : activities) {
            if (null != item && null != item.get() && item.get().getClass() == activityClass) {
                result = true;
                break;
            }
        }
        return result;
    }
}