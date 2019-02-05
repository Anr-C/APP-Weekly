package com.lckiss.weekly.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lckiss.weekly.R;
import com.lckiss.weekly.db.Type;

import java.util.Collections;
import java.util.List;

import static com.lckiss.weekly.util.ResUtil.getImageId;

/**
 * modified by lckiss on 17-7-22.
 */
public class PluginListAdapter extends BaseAdapter {

    private static final String TAG = "info:";

    private Context mContext;
    private List<Type> typeList;
    private Handler handler;

    public PluginListAdapter(Context context, Handler handler, List<Type> added) {
        this.mContext = context;
        this.typeList = added;
        this.handler = handler;
    }

    private Type getType(int position) {
        if (position < typeList.size() + 1) {
            return typeList.get(position);
        } else if (position == typeList.size()) { // 跳过条目名称（未添加）
            return null;
        } else {
            return typeList.get(position);
        }
    }

    /**
     * 交换数据的位置
     *
     * @param src
     * @param dst
     * @return
     */
    public boolean exchange(int src, int dst) {
        boolean success = false;

        Type srcType = getType(src);
        Type dstType = getType(dst);
        int srcIndex = typeList.indexOf(srcType);
        int dstIndex = typeList.indexOf(dstType);
        if (srcIndex != -1 && dstIndex != -1) {
            Collections.swap(typeList, srcIndex, dstIndex);
            success = true;
        }
        //成功通知刷新
        if (success) {
            notifyDataSetChanged();
        }

        Message msg=new Message();
        msg.what=100;
        msg.obj=typeList;
        handler.sendMessage(msg);

        return success;
    }

    @Override
    public int getCount() {
        return typeList.size();
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }


    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.drag_type_item, null);
            viewHolder = new ViewHolder();
            viewHolder.type_image = convertView.findViewById(R.id.type_image);
            viewHolder.type_describe = convertView.findViewById(R.id.type_describe);
            viewHolder.drag_list_item_image = convertView.findViewById(R.id.drag_list_item_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Type type = typeList.get(position);
        viewHolder.type_describe.setText(type.getDescribe());

        int type_imageID = getImageId(mContext,type.getImage_id());
        viewHolder.type_image.setImageResource(type_imageID);
        viewHolder.type_describe.setText(type.getDescribe());
        viewHolder.drag_list_item_image.setImageResource(R.mipmap.drag_item_image);

        ((ViewGroup) convertView).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        return convertView;
    }

    /**
     *
     */
    private class ViewHolder {
         ImageView type_image;
         TextView type_describe;
         ImageView drag_list_item_image;
    }


}
