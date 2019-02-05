package com.lckiss.weekly;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;

import com.lckiss.weekly.fragment.UpdateDialogFragment;
import com.lckiss.weekly.gson.AppInfo;
import com.lckiss.weekly.util.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.lckiss.weekly.widget.MyUtils.showToast;

/**
 * Created by root on 17-8-18.
 */

public class UpdateManager {
    private ProgressDialog progressDialog;
    private Activity context;
    private String version = null;
    private boolean hasTips;

    public UpdateManager(Activity context, boolean hasTips) {
        this.context = context;
        this.hasTips = hasTips;
        init();
        update();
    }

    private void init() {
        //系统版本
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (hasTips) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("检查更新");
            progressDialog.setMessage("可能需要点时间...");
            progressDialog.setCancelable(false);//不允许使用返回关闭
            progressDialog.show();
        }
    }

    private void update() {
        String url = "http://s.lckiss.com/WeeklySys/versiontojson";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    Looper.prepare();
                    showToast(context, "检查更新失败,请稍后再试.");
                    Looper.loop();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                AppInfo appInfo = HttpUtil.handleJson(response.body().string());
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (appInfo == null) {
                    return;
                }
                if (!version.equals(appInfo.getNewVersion())) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("appInfo", appInfo);
                    UpdateDialogFragment updateDialogFragment = new UpdateDialogFragment();
                    updateDialogFragment.setArguments(bundle);
                    updateDialogFragment.show(context.getFragmentManager(), "dialog");
                } else {
                    if (hasTips) {
                        Looper.prepare();
                        showToast(context, "已是最新版");
                        Looper.loop();
                    }
                }
            }
        });
    }
}
