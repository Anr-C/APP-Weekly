package com.lckiss.weekly.widget;


import android.content.Context;
import android.widget.Toast;

/**
 * Created by Dacer on 10/8/13.
 */
public class MyUtils {
//    private static Toast toast;

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
/**
 * 无限弹窗 不必消失
 */
//    public static void showToast(Context context, String string){
//        if(toast==null){
//            toast=Toast.makeText(context,string,Toast.LENGTH_SHORT);
//        }
//        toast.setText(string);
//        toast.show();
//    }

}
