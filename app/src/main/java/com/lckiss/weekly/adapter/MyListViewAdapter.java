package com.lckiss.weekly.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lckiss.weekly.MainActivity;
import com.lckiss.weekly.lib.popupwindow.CommonPopupWindow;
import com.lckiss.weekly.R;
import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.widget.MyNumberTextView;

import java.util.List;

import static com.lckiss.weekly.R.layout.list_item_info;
import static com.lckiss.weekly.util.DataUtil.deleteRecordByUUID;
import static com.lckiss.weekly.util.DataUtil.findType;
import static com.lckiss.weekly.util.ResUtil.getImageId;

/**
 * Created by root on 17-7-6.
 */

public class MyListViewAdapter extends BaseAdapter implements CommonPopupWindow.ViewInterface {
    private List<Record> records;
    private Context context;
    private CommonPopupWindow popupWindow;
    private Record record;
    private Handler handler;
    private ListView listView;

    public MyListViewAdapter(Handler handler, ListView listView, MainActivity mainActivity, List<Record> records) {
        this.handler = handler;
        this.records = records;
        this.listView = listView;
        this.context = mainActivity;
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int i) {
        return records.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = View.inflate(context, list_item_info, null);
            viewHolder = new ViewHolder();
            viewHolder.commentTv = view.findViewById(R.id.comment_tv);
            viewHolder.itemTypeImage = view.findViewById(R.id.item_type_image);
            viewHolder.time = view.findViewById(R.id.time);
            viewHolder.feeTv = view.findViewById(R.id.fee_tv);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (records.size() == 0) {
            Log.d("info", "MyListViewAdapter...getView: 月份改变，但数据未同步，捕获异常");
            return view;
        }
        Record r = records.get(i);
        int gvp_imageID = getImageId(context, findType(r.getType()).getImage_id());
        viewHolder.commentTv.setText(r.getComment());
        viewHolder.itemTypeImage.setImageResource(gvp_imageID);
        viewHolder.time.setText(r.getTime());
        if ("-1".equals(r.getClasses())) {
            viewHolder.feeTv.setText("-" + r.getCost());
            viewHolder.feeTv.setTextColor(ContextCompat.getColor(context, R.color.reduce_color));
        } else {
            viewHolder.feeTv.setText("+" + r.getCost());
            viewHolder.feeTv.setTextColor(ContextCompat.getColor(context, R.color.rent));
        }
        //使用这种方式会丢失特效
        /*view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                record = r;
                showDownPop(view);
                return false;
            }
        });*/
        return view;
    }

    private class ViewHolder {
        TextView commentTv;
        ImageView itemTypeImage;
        MyNumberTextView time;
        MyNumberTextView feeTv;

        ViewHolder() {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    record = records.get(i);
                    showDownPop(view);
                    //返回true禁止触发单击事件
                    return true;
                }
            });
        }

    }

    //向下弹出
    public void showDownPop(View view) {
        if (popupWindow != null && popupWindow.isShowing()) return;
        popupWindow = new CommonPopupWindow.Builder(context)
                .setView(R.layout.popup_up)
                .setWidthAndHeight(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .setAnimationStyle(R.style.AnimDown)
                .setViewOnclickListener(this)
                .setOutsideTouchable(true)
                .create();
        popupWindow.showAsDropDown(view, view.getWidth() / 2, -(popupWindow.getHeight() + view.getMeasuredHeight()));
        //得到button的左上角坐标
//        int[] positions = new int[2];
//        view.getLocationOnScreen(positions);
//        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.NO_GRAVITY, 0, positions[1] + view.getHeight());
    }


    @Override
    public void getChildView(View view, int layoutResId) {
        //获得PopupWindow布局里的View
        switch (layoutResId) {
            case R.layout.popup_up:
                TextView tv_mod = view.findViewById(R.id.tv_mod);
                TextView tv_del = view.findViewById(R.id.tv_del);
                tv_mod.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message msg = new Message();
                        msg.what = 300;
                        msg.obj = record;
                        handler.sendMessage(msg);
                        popupWindow.dismiss();
                    }
                });
                tv_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("确认删除");
                        builder.setMessage("删除后将无法恢复，请确认是否删除");
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                popupWindow.dismiss();
                            }
                        });
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Message msg = new Message();
                                Boolean res = deleteRecordByUUID(record.getUUID());
                                if (res) {
                                    msg.what = 100;
                                    msg.obj = record.getDate();
                                } else {
                                    msg.what = 200;
                                    Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
                                }
                                handler.sendMessage(msg);
                                popupWindow.dismiss();
                            }
                        });
                        builder.create();
                        builder.show();
                    }
                });
                break;
        }
    }

}

