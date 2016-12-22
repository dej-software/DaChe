package com.jikexueyuan.dacheserver;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by dej on 2016/11/6.
 */
public class TakeTaxiDataHandler extends IoHandlerAdapter {

    // 用户端列表、司机端列表、订单列表
    private List<UserInfo> userList = new ArrayList<>();
    private List<UserInfo> driverList = new ArrayList<>();
    private List<TakeTaxiInfo> takeTaxiInfoList = new ArrayList<>();

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);

        System.out.println(session.toString());
        // 连接服务器成功发送一个信息
//        session.write(DataUtils.makeMessage(DataUtils.DATA_FLAG_CONNECT, "@success", null).toString());
        session.write("{\"flag\":0,\"message\":\"@success\"}");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);

        System.out.println(session.toString());
        // 并发送 {"flag":4,"type":2,"id":"20161220114158","name":"233","latitude":-1,"longitude":-1}

        for (UserInfo u : userList) {
            if (u.getSession().equals(session)) {
                System.out.println(u.getUserName());
                u.setSession(null);
                return;
            }
        }

        for (UserInfo u : driverList) {
            if (u.getSession().equals(session)) {
                System.out.println(u.getUserName());
                u.setSession(null);
                // 把断开的司机端发给所以打车用户 消去地图上的标记
                for (UserInfo user : userList) {
                    if (user.getSession() != null) {
                        user.getSession().write(String.format("{\"flag\":4,\"id\":\"%s\",\"latitude\":-1,\"longitude\":-1}", u.getId()));
                    }
                }
            }
        }
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        System.out.println("message:" + message);
        // 使用JSON格式解析数据
        JSONObject root = null;
        try {
            root = new JSONObject((String) message);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        int flag = root.getInt(DataUtils.JSON_FLAG);
        switch (flag) {
            // 处理用户数据 添加
            case DataUtils.DATA_FLAG_REGISTER:
                addUser(root, session);
                break;

            // 处理定位数据
            case DataUtils.DATA_FLAG_LOCATION:
                int type = root.getInt(DataUtils.JSON_USER_TYPE);
                if (type == DataUtils.TAXI_DRIVER) {
                    // 发送给每一个用户端（在地图上显示周围的司机）
                    for (UserInfo u : userList) {
                        if (u.getSession() != null) {
                            u.getSession().write(message);
                        }
                    }
                }
                break;
            // 处理打车用户端发来的信息
            case DataUtils.DATA_FLAG_USER:
                String id = root.getString(DataUtils.JSON_USER_ID);
                TakeTaxiInfo takeTaxiInfo = DataUtils.getTakeTaxiFromList(id, takeTaxiInfoList);
                if (takeTaxiInfo == null) {
                    takeTaxiInfo = new TakeTaxiInfo(id, "", (String) message);
                    takeTaxiInfoList.add(takeTaxiInfo);
                } else {
                    takeTaxiInfo.setDriverId("");
                    takeTaxiInfo.setJsonMsg((String) message);
                }

                // 发送给每一个司机端（显示附近用户的打车信息）
                for (UserInfo u : driverList) {
                    u.getSession().write(message);
                }
                break;
            // 处理司机端发来的信息（接单）
            case DataUtils.DATA_FLAG_DRIVER:
                String destId = root.getString(DataUtils.JSON_DEST_ID);
                UserInfo destUser = DataUtils.getUserFromList(destId, userList);
                if (destUser != null && destUser.getSession() != null) {
                    // 把信息转给该打车用户
                    destUser.getSession().write(message);
                    // 简单处理 直接发送取消订单的格式信息给所以司机
                    for (UserInfo u : driverList) {
                        if (u.getSession() != null) {
                            u.getSession().write(String.format("{\"flag\":2,\"type\":1,\"id\":\"%s\"," +
                                    "\"name\":\"\",\"phone\":\"\",\"begin\":\"\",\"dest\":\"\"," +
                                    "\"latitude\":-1,\"longitude\":-1}", destId));
                        }
                    }
                }

                break;
        }
    }

    /**
     * 添加用户
     *
     * @param root
     */
    private void addUser(JSONObject root, IoSession session) {

        String id = DataUtils.getDateTime("yyyyMMddHHmmss");

        try {
            int type = root.getInt(DataUtils.JSON_USER_TYPE);
            String name = root.getString(DataUtils.JSON_NAME);
            String phone = root.getString(DataUtils.JSON_PHONE);
            UserInfo userInfo = new UserInfo(type, id, name, phone, session);
            if (type == DataUtils.TAKE_TAXI_USER) {
                UserInfo u = DataUtils.getUserFromList(userInfo, userList);
                if (u != null) {
                    u.setSession(session);
                    userInfo = u;
                } else {
                    userList.add(userInfo);
                }
            } else if (type == DataUtils.TAXI_DRIVER) {
                UserInfo u = DataUtils.getUserFromList(userInfo, driverList);
                if (u != null) {
                    u.setSession(session);
                    userInfo = u;
                } else {
                    driverList.add(userInfo);
                }
            }

            session.write(DataUtils.makeMessage(DataUtils.DATA_FLAG_REGISTER, "@user_success", userInfo).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
