package com.lckiss.weekly.lib;

import android.graphics.Color;

/**
 * Created by root on 17-7-12.
 */

public class Utils {

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
