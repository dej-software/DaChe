package com.jikexueyuan.taketaxiuser.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jikexueyuan.taketaxiuser.bean.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dej on 2016/12/12.
 * 用户信息配置相关操作
 */

public class PreferencesUtils {

    public static final String USER = "user";
    public static final String PHONE = "phone";

    // 当前城市 当前周围POI 城市重要POI
    public static String curCity = "";
    public static List<Location> curLocationList = new ArrayList<>();
    public static List<Location> cityLocationList = new ArrayList<>();

    private static String id;
    private static String user;
    private static String phone;

    private static SharedPreferences preferences = null;
    private static SharedPreferences.Editor editor = null;

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        PreferencesUtils.user = user;
    }

    public static String getPhone() {
        return phone;
    }

    public static void setPhone(String phone) {
        PreferencesUtils.phone = phone;
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static SharedPreferences.Editor getEditor() {
        return editor;
    }

    /**
     * 初始化配置数据
     *
     * @param context
     */
    public static void init(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        setUser(preferences.getString(USER, ""));
        setPhone(preferences.getString(PHONE, ""));

    }

    /**
     * 把配置数据提交
     *
     * @return
     */
    public static boolean commit() {
        editor.putString(USER, getUser());
        editor.putString(PHONE, getPhone());
        return editor.commit();
    }

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        PreferencesUtils.id = id;
    }
}
