package com.lckiss.weekly.db;

import org.litepal.crud.DataSupport;

/**
 * Created by root on 17-7-5.
 */

public class Type extends DataSupport {
    private int id;
    private String type;
    private String image_id;
    private String describe;
    private String color;
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return type + ',' + image_id + ',' + describe + ',' + color + ','+ status;
    }
}
