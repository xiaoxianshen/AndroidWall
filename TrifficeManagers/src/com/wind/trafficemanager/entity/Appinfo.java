package com.wind.trafficemanager.entity;

/**
 * 作者：xiaobin on 2017/1/10 15:44
 * 邮箱：xiaobin01@wind-mobi.com
 */
public class Appinfo {

    private int uid;
    private int wifi;
    private int mobile;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getMobile() {
        return mobile;
    }

    public void setMobile(int mobile) {
        this.mobile = mobile;
    }

    public int getWifi() {
        return wifi;
    }

    public void setWifi(int wifi) {
        this.wifi = wifi;
    }

    @Override
    public String toString() {
        return "Appinfo{" +
                "uid=" + uid +
                ", wifi=" + wifi +
                ", mobile=" + mobile +
                '}';
    }
}
