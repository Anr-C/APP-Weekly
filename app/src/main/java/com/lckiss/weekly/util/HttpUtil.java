package com.lckiss.weekly.util;

import com.google.gson.Gson;
import com.lckiss.weekly.gson.AppInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by root on 17-8-15.
 */

public class HttpUtil {
    /**
     * 发送http网络请求
     * @param address 请求地址
     * @param callback 回调
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 将json转为对象
     * @param response response
     * @return Object
     */
    public static AppInfo handleJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String jsonString = jsonObject.toString();
            return new Gson().fromJson(jsonString, AppInfo.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取文件名
     * @param conn Http连接
     */
    public static String getFileName(HttpURLConnection conn, String downloadUrl) {
        String filename = downloadUrl.substring(downloadUrl
                .lastIndexOf('/') + 1);// 截取下载路径中的文件名
        // 如果获取不到文件名称
        if (filename == null || "".equals(filename.trim())) {
            // 通过截取Http协议头分析下载的文件名
            for (int i = 0; ; i++) {
                String mine = conn.getHeaderField(i);
                if (mine == null)
                    break;
                /**
                 * Content-disposition 是 MIME 协议的扩展，MIME 协议指示 MIME
                 * 用户代理如何显示附加的文件。
                 * Content-Disposition就是当用户想把请求所得的内容存为一个文件的时候提供一个默认的文件名
                 * 协议头中的Content-Disposition格式如下：
                 * Content-Disposition","attachment;filename=FileName.txt");
                 */
                if ("content-disposition".equals(conn.getHeaderFieldKey(i)
                        .toLowerCase())) {
                    // 通过正则表达式匹配出文件名
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(
                            mine.toLowerCase());
                    // 如果匹配到了文件名
                    if (m.find())
                        return m.group(1);// 返回匹配到的文件名
                }
            }
            // 如果还是匹配不到文件名，则默认取一个随机数文件名
            filename = UUID.randomUUID() + ".tmp";
        }
        return filename;
    }
}
