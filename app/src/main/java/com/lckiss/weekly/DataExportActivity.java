package com.lckiss.weekly;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.db.Type;
import com.lckiss.weekly.lib.filepicker.FilePicker;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.os.Build.VERSION_CODES.M;
import static com.lckiss.weekly.util.CSVUtil.backup;
import static com.lckiss.weekly.util.CSVUtil.exportCSV;
import static com.lckiss.weekly.util.CSVUtil.importCSV;
import static com.lckiss.weekly.util.CSVUtil.restore;
import static com.lckiss.weekly.util.DataUtil.deleteAllRecord;
import static com.lckiss.weekly.util.DataUtil.deleteAllType;
import static com.lckiss.weekly.util.DataUtil.findAllRecord;
import static com.lckiss.weekly.util.InitDB.initTypeDB;
import static com.lckiss.weekly.util.TimeUtil.getDate;
import static com.lckiss.weekly.widget.MyUtils.showToast;

public class DataExportActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "info";
    private ExecutorService cachedThreadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_export);
        grant();
        initView();
    }

    private void grant() {
        if (Build.VERSION.SDK_INT >= M) {
            // 在其它任何地方：
            AndPermission.with(this)
                    .requestCode(100)
                    .permission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .rationale(rationaleListener)
                    .callback(this)
                    .start();
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        TextView cleanAll = (TextView) findViewById(R.id.remove);
        cleanAll.setOnClickListener(this);
        Button importBtn = (Button) findViewById(R.id.import_button);
        importBtn.setOnClickListener(this);
        Button exportBtn = (Button) findViewById(R.id.export_button);
        exportBtn.setOnClickListener(this);

        cachedThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_backup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.ic_backup:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("备份所有数据")
                        .setMessage("备份文件在内存/Weekly/Backup/下,使用导入功能即可恢复数据")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                cachedThreadPool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        backupToFile();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create();
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.remove:
                buildDialog();
                break;
            case R.id.import_button:
                //noinspection MissingPermission
                FilePicker picker = new FilePicker(this, FilePicker.FILE);
                picker.setShowHideDir(false);
                picker.setShowHomeDir(true);
                picker.setShowUpDir(true);
                picker.setGravity(Gravity.CENTER);
                picker.setRootPath(Environment.getExternalStorageDirectory() + "/Weekly");
//                picker.setRootPath("/");
                picker.setAllowExtensions(new String[]{".csv",".setting"});
                picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
                    @Override
                    public void onFilePicked(final String currentPath) {
                        cachedThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                importFromCSV(currentPath);
                            }
                        });
                    }
                });
                picker.show();
                break;
            case R.id.export_button:
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        exportToCSV();
                    }
                });
                break;
        }
    }

    private void backupToFile(){
        File file = new File(Environment.getExternalStorageDirectory() + "/Weekly/Backup/" + getDate() + "Weekly设置备份" + ".setting");
        if (!file.exists()) {
            boolean mkdir = file.getParentFile().mkdirs();
            Log.d(TAG, "CSVUtil backup mkdir: " + mkdir);
        } else {
            boolean delete = file.delete();
            Log.d(TAG, "CSVUtil backup delete: " + delete);
        }
        //备份支出
        List<Record> records = DataSupport.findAll(Record.class);
        boolean res1 = backup(file, records, 0);
        //备份类目
        List<Type> types = DataSupport.findAll(Type.class);
        boolean res2 = backup(file, types, 1);
        Looper.prepare();
        if (res1 && res2) {
            showToast(this, "备份成功");
        } else {
            showToast(this, "备份失败,请检查是否授予存储权限");
        }
        Looper.loop();
    }

    private void exportToCSV() {
        boolean res = exportCSV(findAllRecord(), "所有账目导出");
        Looper.prepare();
        if (res) {
            showToast(this, "导出成功");
        } else {
            showToast(this, "导出失败,请检查是否授予存储权限");
        }
        Looper.loop();
    }

    private void importFromCSV(String file) {

        if (file.contains(".setting")) {
           boolean res= restore(file);
            Looper.prepare();
            if (res) {
                showToast(this, "恢复数据成功");
            } else {
                showToast(this, "恢复失败,请检查文件是否有误.");
            }
            Looper.loop();
        } else {
          int  res = importCSV(file);
            Looper.prepare();
            if (res >= 0) {
                showToast(this, "导入数据" + res + "条");
            } else {
                showToast(this, "导入失败,请检查文件是否有误.");
            }
            Looper.loop();
        }

    }

    private void buildDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("清除所有数据")
                .setMessage("清除所有数据后,如果没有备份,无法再回复,请谨慎操作(建议先备份数据)")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AlertDialog.Builder(DataExportActivity.this)
                                .setTitle("清除所有数据")
                                .setMessage("不是点错了吗?真的清除了...")
                                .setPositiveButton("确认清除", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteAllRecord();
                                        //清空类目
                                        deleteAllType();
                                        //重新初始化
                                        initTypeDB();
                                        showToast(DataExportActivity.this, "数据已重新初始化");
                                    }
                                })
                                .setNegativeButton("我点错了", null)
                                .create().show();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    // 成功回调的方法，用注解即可，这里的150就是请求时的requestCode。
    @PermissionYes(100)
    private void getPermissionYes(List<String> grantedPermissions) {
        // TODO: 17-8-20
        Log.d(TAG, "getPermissionYes: 已授权");
    }

    @PermissionNo(100)
    private void getPermissionNo(List<String> deniedPermissions) {
        showToast(this, "您已拒绝授权，请手动在设置->应用管理->Weekly账本->权限管理中授予存储权限。");
        finish();
    }

    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            com.yanzhenjie.alertdialog.AlertDialog.newBuilder(DataExportActivity.this)
                    .setTitle("友好提醒")
                    .setMessage("请不要拒绝存储权限，沒有存储权限无法导入导出数据！")
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
