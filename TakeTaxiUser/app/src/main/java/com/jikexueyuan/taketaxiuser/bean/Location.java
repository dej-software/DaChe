package com.jikexueyuan.taketaxiuser.bean;

/**
 * Created by dej on 2016/12/20.
 */

public class Location {
    private String name;

    public Location(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 重写方法 显示到列表的数据
     * @return
     */
    @Override
    public String toString() {
        return name;
    }
}
