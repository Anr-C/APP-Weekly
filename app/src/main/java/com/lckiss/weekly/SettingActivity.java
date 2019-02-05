package com.lckiss.weekly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.lckiss.weekly.fragment.SettingsFragment;

import org.zackratos.ultimatebar.UltimateBar;

import static com.lckiss.weekly.util.Constants.bigCostNum;
import static com.lckiss.weekly.util.Constants.defaultNUm;
import static com.lckiss.weekly.widget.MyUtils.showToast;

public class SettingActivity extends AppCompatActivity {
    private Intent intentBack;
    private SharedPreferences preferences;
    private static Float ALLMONEY = 0F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        //状态栏的特殊处理
        UltimateBar ultimateBar = new UltimateBar(this);
        ultimateBar.setColorBar(ContextCompat.getColor(this, R.color.line_color));
        if (savedInstanceState == null) {
            this.getFragmentManager().beginTransaction()
                    .add(R.id.content, new SettingsFragment())
                    .commit();
        }
        intentBack = new Intent();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://actionbar的左侧图标的点击事件处理
                updateBudget();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                updateBudget();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateBudget() {
        String value = preferences.getString("budget", "");
        try {
            ALLMONEY = Float.parseFloat(value);
            if (ALLMONEY > bigCostNum) {
                SharedPreferences.Editor editor = preferences.edit();
                ALLMONEY = defaultNUm;
                editor.putString("budget", defaultNUm.toString());
                editor.apply();
                showToast(this, "数值超出正常范围使用默认值");
            }
        } catch (Exception e) {
            Log.d("info", "ALLMONEY : 未包含数据，使用默认数据");
        }
        intentBack.putExtra("budget", ALLMONEY);
        setResult(3, intentBack);
        finish();
    }
}
