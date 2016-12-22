package com.jikexueyuan.dacheserver;

import org.apache.mina.core.session.IoSession;

/**
 * Created by dej on 2016/12/19.
 * 用户信息
 */
public class UserInfo {
    private int userType;
    private String id;
    private String userName, userPhone;
    private IoSession session;

    public UserInfo(int userType, String id, String userName, String userPhone, IoSession session) {
        this.userType = userType;
        this.id = id;
        this.userName = userName;
        this.userPhone = userPhone;
        this.session = session;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

    public IoSession getSession() {
        return session;
    }
}
