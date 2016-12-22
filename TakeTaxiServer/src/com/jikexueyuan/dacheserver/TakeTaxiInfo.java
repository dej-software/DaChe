package com.jikexueyuan.dacheserver;

/**
 * Created by dej on 2016/12/20.
 * 打车订单信息
 */
public class TakeTaxiInfo {
    private String userId, driverId;
    private String jsonMsg;

    public TakeTaxiInfo(String userId, String driverId, String jsonMsg) {
        this.userId = userId;
        this.driverId = driverId;
        this.jsonMsg = jsonMsg;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getJsonMsg() {
        return jsonMsg;
    }

    public void setJsonMsg(String jsonMsg) {
        this.jsonMsg = jsonMsg;
    }
}
