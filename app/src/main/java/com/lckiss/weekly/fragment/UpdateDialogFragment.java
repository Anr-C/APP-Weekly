package com.lckiss.weekly.fragment;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lckiss.weekly.R;
import com.lckiss.weekly.gson.AppInfo;
import com.lckiss.weekly.service.AutoUpdateService;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static com.lckiss.weekly.widget.MyUtils.showToast;

/**
 * Created by root on 17-8-15.
 */

public class UpdateDialogFragment extends DialogFragment implements View.OnClickListener {

    private TextView mContentTextView;
    private Button mUpdateOkButton;
    private AppInfo appInfo;
    private ImageView mIvClose;
    private TextView mTitleTextView;
    //    private ImageView mTopIv;
//    private TextView mIgnore;
//    private LinearLayout mLlClose;
    private Context context;
    private static final String TAG = "info";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lib_update_app_dialog, container);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initTheme();
        initData();
    }

    private void initTheme() {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //点击window外的区域 是否消失
        getDialog().setCanceledOnTouchOutside(false);
    }

    private void initView(View view) {
        //提示内容
        mContentTextView = (TextView) view.findViewById(R.id.tv_update_info);
        //标题
        mTitleTextView = (TextView) view.findViewById(R.id.tv_title);
        //更新按钮
        mUpdateOkButton = (Button) view.findViewById(R.id.btn_ok);
        //进度条
//        mNumberProgressBar = (NumberProgressBar) view.findViewById(R.id.npb);
        //关闭按钮
        mIvClose = (ImageView) view.findViewById(R.id.iv_close);
        //关闭按钮+线 的整个布局
//        mLlClose = (LinearLayout) view.findViewById(R.id.ll_close);
        //顶部图片
//        mTopIv = (ImageView) view.findViewById(R.id.iv_top);
        //忽略
//        mIgnore = (TextView) view.findViewById(R.id.tv_ignore);
        mIvClose.setOnClickListener(this);
        mUpdateOkButton.setOnClickListener(this);
    }

    private void initData() {
        appInfo = getArguments().getParcelable("appInfo");
        mTitleTextView.setText("是否升级到 " + appInfo.getVersionType() + appInfo.getNewVersion() + " 版本？");
        mContentTextView.setText("新版本大小:" + appInfo.getTargetSize() + "\n\n" + appInfo.getUpdateLog());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ok:
                if (Build.VERSION.SDK_INT >= M) {
                    // 在其它任何地方：
                    AndPermission.with(context)
                            .requestCode(120)
                            .permission(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            .rationale(rationaleListener)
                            .callback(this)
                            .start();
                } else {
                    startService();
                }
                dismiss();
                break;
            case R.id.iv_close:
                dismiss();
                break;
            default:
                break;
        }
    }

    private void startService() {
        if (context == null) {
            context = getActivity();
        }
        Intent updateService = new Intent(context, AutoUpdateService.class);
        updateService.putExtra("url", appInfo.getDownloadUrl());
        context.startService(updateService);
    }

    // 成功回调的方法，用注解即可，这里的150就是请求时的requestCode。
    @PermissionYes(120)
    private void getPermissionYes(List<String> grantedPermissions) {
        startService();
    }

    @PermissionNo(120)
    private void getPermissionNo(List<String> deniedPermissions) {
        showToast(context, "您已拒绝授权，请手动在设置->应用管理->Weekly账本->权限管理中授予存储权限。");
    }

    /**
     * Rationale支持，这里自定义对话框。
     */
    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            com.yanzhenjie.alertdialog.AlertDialog.newBuilder(context)
                    .setTitle("友好提醒")
                    .setMessage("不要拒绝存储权限哦，沒有存储权限无法下载和保存安装包！")
                    .setPositiveButton("好，给你", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            rationale.resume();
                        }
                    })
                    .setNegativeButton("我拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            rationale.cancel();
                        }
                    }).show();
        }
    };
}
