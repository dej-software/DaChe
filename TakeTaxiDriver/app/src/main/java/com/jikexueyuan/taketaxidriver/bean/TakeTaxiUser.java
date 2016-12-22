package com.jikexueyuan.taketaxidriver.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dej on 2016/12/20.
 */

public class TakeTaxiUser implements Parcelable {
    private String id;
    private String userName, userPhone;
    private String beginAddr, destAddr;
    private int distance;

    public TakeTaxiUser(String id, String userName, String userPhone, String beginAddr, String destAddr, int distance) {
        this.id = id;
        this.userName = userName;
        this.userPhone = userPhone;
        this.beginAddr = beginAddr;
        this.destAddr = destAddr;
        this.distance = distance;
    }

    protected TakeTaxiUser(Parcel in) {
        id = in.readString();
        userName = in.readString();
        userPhone = in.readString();
        beginAddr = in.readString();
        destAddr = in.readString();
        distance = in.readInt();
    }

    public static final Creator<TakeTaxiUser> CREATOR = new Creator<TakeTaxiUser>() {
        @Override
        public TakeTaxiUser createFromParcel(Parcel in) {
            return new TakeTaxiUser(in);
        }

        @Override
        public TakeTaxiUser[] newArray(int size) {
            return new TakeTaxiUser[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getBeginAddr() {
        return beginAddr;
    }

    public void setBeginAddr(String beginAddr) {
        this.beginAddr = beginAddr;
    }

    public String getDestAddr() {
        return destAddr;
    }

    public void setDestAddr(String destAddr) {
        this.destAddr = destAddr;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(userName);
        parcel.writeString(userPhone);
        parcel.writeString(beginAddr);
        parcel.writeString(destAddr);
        parcel.writeInt(distance);
    }
}
