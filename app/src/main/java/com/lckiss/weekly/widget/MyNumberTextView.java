package com.lckiss.weekly.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by root on 17-7-3.
 */

    public class MyNumberTextView extends AppCompatTextView {
    public MyNumberTextView(Context context) {
        super(context);
        initView();
    }

    public MyNumberTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MyNumberTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    public void initView(){
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "TitilliumText25L-400wt.ttf"));
    }
}
