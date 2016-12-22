package com.jikexueyuan.taketaxiuser.util;

/**
 * 一些公用的变量
 * Created by dej on 2016/11/7.
 */
public class ClientUtil {
    public static final String SERVER_IP = "192.168.10.102";
    public static final int SERVER_PORT = 20000;

    public static final String JSON_FLAG = "flag";
    public static final String JSON_USER_TYPE = "type";
    public static final String JSON_USER_ID = "id";
    public static final String JSON_NAME = "name";
    public static final String JSON_PHONE = "phone";
    public static final String JSON_MSG = "message";
    public static final String JSON_BEGIN = "begin";
    public static final String JSON_DEST = "dest";
    public static final String JSON_LATITUDE = "latitude";
    public static final String JSON_LONGITUDE = "longitude";

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

    // 类型 打车用户 \ 司机
    public static final int TAKE_TAXI_USER = 1;
    public static final int TAXI_DRIVER = 2;
}
