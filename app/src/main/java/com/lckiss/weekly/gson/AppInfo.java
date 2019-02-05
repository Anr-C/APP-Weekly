package com.lckiss.weekly.gson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by root on 17-8-15.
 */
public class AppInfo implements Parcelable {
    //apk大小
    @SerializedName("target_size")
    private String targetSize;
    //是否强制
    private String constraint;
    //版本号
    @SerializedName("new_version")
    private String newVersion;
    //下载地址
    @SerializedName("apk_file_url")
    private String downloadUrl;
    //更新日志
    @SerializedName("update_log")
    private String updateLog;
    //MD5
    @SerializedName("new_md5")
    private String newMd5;

    @SerializedName("version_type")
    private String versionType;

    public String getTargetSize() {
        return targetSize;
    }

    public void setTargetSize(String targetSize) {
        this.targetSize = targetSize;
    }

    public String getConstraint() {
        return constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    public String getNewMd5() {
        return newMd5;
    }

    public void setNewMd5(String newMd5) {
        this.newMd5 = newMd5;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.targetSize);
        dest.writeString(this.constraint);
        dest.writeString(this.newVersion);
        dest.writeString(this.downloadUrl);
        dest.writeString(this.updateLog);
        dest.writeString(this.newMd5);
        dest.writeString(this.versionType);
    }

    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel source) {
            AppInfo appInfo=new AppInfo();
            appInfo.targetSize = source.readString();
            appInfo.constraint = source.readString();
            appInfo.newVersion = source.readString();
            appInfo.downloadUrl = source.readString();
            appInfo.updateLog = source.readString();
            appInfo.newMd5 = source.readString();
            appInfo.versionType = source.readString();

            return appInfo;
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}
