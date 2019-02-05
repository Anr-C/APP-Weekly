package com.lckiss.weekly.util;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaActionSound;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.Display;
import android.view.TextureView;
import android.view.View;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static android.view.View.MeasureSpec;
import static com.lckiss.weekly.widget.MyUtils.showToast;

/**
 * The type ScreenShot class.
 */
public class ScreenShot {
    private static final ScreenShot ourInstance = new ScreenShot();
    private Activity activity;
    private View view;
    private ProgressDialog progressDialog;
    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static ScreenShot getInstance() {
        return ourInstance;
    }

    /**
     * Take screen shot of root view.
     *
     * @param v the v
     * @return the bitmap
     */
    public Bitmap takeScreenShotOfRootView(View v) {
        v = v.getRootView();
        return takeScreenShotOfView(v);
    }

    /**
     * Take screen shot of the View with spaces as per constraints
     *
     * @param v the v
     * @return the bitmap
     */
    public Bitmap takeScreenShotOfView(View v) {
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);

        // creates immutable clone
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false); // clear drawing cache
        return b;
    }

    /**
     * Take screen shot of texture view as bitmap.
     *
     * @param v the TextureView
     * @return the bitmap
     */
    public Bitmap takeScreenShotOfTextureView(TextureView v) {
        return v.getBitmap();
    }

    /**
     * Take screen shot of just the View without any constraints
     *
     * @param v the v
     * @return the bitmap
     */
    public Bitmap takeScreenShotOfJustView(View v) {
        v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        return takeScreenShotOfView(v);
    }

    /**
     * Save screenshot to pictures folder.
     *
     * @param context  the context
     * @param image    the image
     * @param filename the filename
     * @return the bitmap file object
     * @throws Exception the exception
     */
    public File saveScreenshotToPicturesFolder(Context context, Bitmap image, String filename)
            throws Exception {
        File bitmapFile = getOutputMediaFile(filename);
        if (bitmapFile == null) {
            throw new NullPointerException("Error creating media file, check storage permissions!");
        }
        FileOutputStream fos = new FileOutputStream(bitmapFile);
        //尽量不要压缩，会导致图片异常
        image.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();

        // Initiate media scanning to make the image available in gallery apps
        MediaScannerConnection.scanFile(context, new String[]{bitmapFile.getPath()},
                new String[]{"image/jpeg"}, null);
        return bitmapFile;
    }

    private File getOutputMediaFile(String filename) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDirectory = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        + File.separator);
        // Create the storage directory if it does not exist
        if (!mediaStorageDirectory.exists()) {
            if (!mediaStorageDirectory.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(new Date());
        File mediaFile;
        String mImageName = filename + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDirectory.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void shotNow(final Activity act, View v) {
        this.activity=act;
        this.view=v;
        progressDialog=new ProgressDialog(activity);
        progressDialog.setTitle("截图中");
        progressDialog.setMessage("可能需要点时间...");
        progressDialog.setCancelable(false);//不允许使用返回关闭
        progressDialog.show();

        if (Build.VERSION.SDK_INT >= M) {
            // 在其它任何地方：
            AndPermission.with(activity)
                    .requestCode(110)
                    .permission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .rationale(rationaleListener)
                    .callback(this)
                    .start();
        } else {
            shot();
        }
    }

    private void shot(){
        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
        }
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();
        //display.getHeight()与display.getWidth()已过时，使用Point替代
        Point size = new Point();
        display.getSize(size);
        int widths = size.x;
        int heights = size.y;
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, statusBarHeights,
                widths, heights - statusBarHeights);
        view.destroyDrawingCache();//释放缓存占用的资源
        File file = null;
        if (bitmap != null) {
            try {
                file =ScreenShot.getInstance()
                        .saveScreenshotToPicturesFolder(activity, bitmap, "weekly");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        progressDialog.dismiss();
        if (file != null) {
//            showToast(activity, "截图已保存至Picture目录");
            shareMsg(activity,"Weekly账本","分享应用","简约好用的账本",file.getAbsolutePath());
        }else {
            showToast(activity, "截图分享失败，请检查权限");
        }

    }
    /**
     * 分享功能. context 上下文. activityTitle Activity的名字. msgTitle 消息标题. msgText
     * 消息内容. imgPath 图片路径，不分享图片则传null.
     */
    public static void
    shareMsg(Context mContext, String activityTitle, String msgTitle, String msgText, String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if ((imgPath == null) || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if ((f != null) && f.exists() && f.isFile()) {
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(Intent.createChooser(intent, activityTitle));
    }

    // 成功回调的方法，用注解即可，这里的100就是请求时的requestCode。
    @PermissionYes(110)
    private void getPermissionYes(List<String> grantedPermissions) {
        shot();
    }

    @PermissionNo(110)
    private void getPermissionNo(List<String> deniedPermissions) {
        progressDialog.dismiss();
        showToast(activity,"您已拒绝授权，请手动在设置->应用管理->Weekly账本->权限管理中授予存储权限。");
    }

    /**
     * Rationale支持，这里自定义对话框。
     */
    private RationaleListener rationaleListener =new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            com.yanzhenjie.alertdialog.AlertDialog.newBuilder(activity)
                    .setTitle("友好提醒")
                    .setMessage("不要拒绝存储权限哦，沒有存储权限无法分享和保存截图！")
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
