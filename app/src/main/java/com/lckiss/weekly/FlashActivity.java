package com.lckiss.weekly;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import org.zackratos.ultimatebar.UltimateBar;

import java.util.ArrayList;

import static com.lckiss.weekly.util.TimeUtil.getDate;
import static com.lckiss.weekly.util.TimeUtil.getWeekLists;

public class FlashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 800;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        UltimateBar ultimateBar = new UltimateBar(this);
        ultimateBar.setImmersionBar();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(FlashActivity.this,
                        MainActivity.class);
                startActivity(intent);
                finish();
            }

        }, SPLASH_DISPLAY_LENGTH);

    }
}
