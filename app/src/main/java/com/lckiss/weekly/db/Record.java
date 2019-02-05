package com.lckiss.weekly.db;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

import static com.lckiss.weekly.util.DataUtil.findType;

/**
 * Created by root on 17-7-5.
 * 序列化以方便传递对象
 */

public class Record extends DataSupport implements Serializable {

    private String uuid;
    private int id;
    private String classes;
    private String type;
    private String cost;
    private String comment;
    private String date;
    private String time;
    private String status;

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String UUID) {
        this.uuid = UUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toBackupString(){
        return classes+","+type+","+cost+","+comment+","+date+","+time;
    }
    @Override
    public String toString() {
        //收支 类别 金额 备注 日期 时间
        String sclass=classes.equals("0")?"收入":"支出";
        String stype=findType(type).getDescribe();
        return sclass+","+stype+","+cost+","+comment+","+date+","+time;
    }
}
