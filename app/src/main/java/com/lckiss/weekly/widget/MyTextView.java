package com.lckiss.weekly.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;

/**
 * Created by root on 17-7-3.
 */

    public class MyTextView extends AppCompatTextView {
    public MyTextView(Context context) {
        super(context);
        initView();
    }

    public MyTextView(Context context,  AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MyTextView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    public void initView(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "segoe_script.ttf"));
    }
}
