package com.jikexueyuan.taketaxidriver;


import android.app.Activity;
import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

import java.util.LinkedList;
import java.util.List;

/**
 * 主Application，SDKInitializer
 */
public class MyApplication extends Application {

    private static List<Activity> activities = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        /***
         * 初始化定位sdk，建议在Application中创建
         */
        SDKInitializer.initialize(getApplicationContext());
    }

    /**
     * 把Activity添加到List中
     * @param activity
     */
    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    /**
     * 退出应用
     */
    public static void exitApp() {
        for (Activity activity : activities) {
            activity.finish();
        }
        System.exit(0);
    }
}
