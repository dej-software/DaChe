package com.jikexueyuan.dacheserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by dej on 2016/12/19.
 */
public class DataUtils {

    // flag对应的JSON值说明：
    // 连接信息
    public static final int DATA_FLAG_CONNECT = 0;
    // 注册信息（包括登录）
    public static final int DATA_FLAG_REGISTER = 1;
    // 用户端信息（如取消约车等）
    public static final int DATA_FLAG_USER = 2;
    // 司机端信息
    public static final int DATA_FLAG_DRIVER = 3;
    // 位置信息
    public static final int DATA_FLAG_LOCATION = 4;

    // JSON数据Key
    // 数据类型 对应上面的定义
    public static final String JSON_FLAG = "flag";
    // 用户类型
    public static final String JSON_USER_TYPE = "type";
    // 用户ID 具有唯一性（名字可能会重复）
    public static final String JSON_USER_ID = "id";
    // 消息要单独发给哪个ID
    public static final String JSON_DEST_ID = "dest_id";
    // 名字 电话 消息主体
    public static final String JSON_NAME = "name";
    public static final String JSON_PHONE = "phone";
    public static final String JSON_MSG = "message";

    // 用户类型 打车用户 \ 司机
    public static final int TAKE_TAXI_USER = 1;
    public static final int TAXI_DRIVER = 2;

    /**
     * 获取相应格式的当前系统时间
     *
     * @param format
     * @return
     */
    public static String getDateTime(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
    }

    /**
     * 把一个UserInfo列表转换为JSON格式
     *
     * @param data
     * @return
     */
    public static JSONArray convertJson(List<UserInfo> data) {

        if (data == null) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        for (UserInfo userInfo : data) {
            JSONObject member = new JSONObject();
            try {
                member.put("id", userInfo.getId());
                member.put("name", userInfo.getUserName());
                member.put("phone", userInfo.getUserPhone());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(member);
        }

        return jsonArray;
    }

    public static JSONObject makeMessage(int flag, String msg, UserInfo userInfo) {
        JSONObject root = new JSONObject();
        try {
            root.put(JSON_FLAG, flag);
            root.put(JSON_MSG, msg);
            root.put(JSON_USER_ID, userInfo.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

    /**
     * 从列表获取一个类似成员（名字和电话相同）
     *
     * @param userInfo
     * @param userInfoList
     * @return
     */
    public static UserInfo getUserFromList(UserInfo userInfo, List<UserInfo> userInfoList) {

        for (UserInfo u : userInfoList) {
            if (u.getUserName().equals(userInfo.getUserName()) && u.getUserPhone().equals(userInfo.getUserPhone())) {
                return u;
            }
        }
        return null;
    }

    /**
     * 从列表获取成员（根据ID）
     *
     * @param destId
     * @param userInfoList
     * @return
     */
    public static UserInfo getUserFromList(String destId, List<UserInfo> userInfoList) {

        for (UserInfo u : userInfoList) {
            if (u.getId().equals(destId)) {
                return u;
            }
        }
        return null;
    }

    /**
     * 从列表获取成员（根据ID）
     *
     * @param id
     * @param taxiInfoList
     * @return
     */
    public static TakeTaxiInfo getTakeTaxiFromList(String id, List<TakeTaxiInfo> taxiInfoList) {

        for (TakeTaxiInfo t : taxiInfoList) {
            if (t.getUserId().equals(id)) {
                return t;
            }
        }
        return null;
    }
}
