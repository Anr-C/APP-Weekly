package com.lckiss.weekly.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

/**
 * Created by root on 17-7-8.
 */

public class ResUtil {
    /**
     * @param context 上下文
     * @param i       colorid
     * @return 颜色值
     */
    public static int getTxtColor(Context context, int i) {
        return ContextCompat.getColor(context, i);
    }

    /**
     * @param context 上下文
     * @param s       文件名
     * @return 返回image
     */
    public static int getImageId(Context context, String s) {
        return context.getResources().getIdentifier(s, "mipmap", context.getPackageName());
    }

    /**
     * @param context 上下文生命周期
     * @param appFile apk文件路径
     * @return 安装意图
     */
    public static Intent installApp(Context context, File appFile) {
        try {
            Uri fileUri = FileProvider.getUriForFile(context, "com.lckiss.weekly.util.fileProvider", appFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(appFile), "application/vnd.android.package-archive");
            }
            if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                return intent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
