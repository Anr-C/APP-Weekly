package com.lckiss.weekly.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lckiss.weekly.R;
import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.widget.MyNumberTextView;

import java.util.List;

import static com.lckiss.weekly.util.DataUtil.findType;
import static com.lckiss.weekly.util.ResUtil.getImageId;
import static com.lckiss.weekly.util.ResUtil.getTxtColor;


/**
 * Created by root on 17-7-17.
 */

public class CommListAdapter extends BaseAdapter {
    private Context context;
    private List<Record> records;

    public CommListAdapter(Activity Activity, List<Record> record) {
        context = Activity;
        this.records = record;
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = View.inflate(context, R.layout.comm_list_item, null);
            viewHolder.entryDate = view.findViewById(R.id.entry_date);
            viewHolder.entryTime = view.findViewById(R.id.entry_time);
            viewHolder.commentTv = view.findViewById(R.id.comment_tv);
            viewHolder.feeTv = view.findViewById(R.id.fee_tv);
            viewHolder.itemTypeImg = view.findViewById(R.id.item_type_image);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Record r = records.get(i);
        int gvp_imageID = getImageId(context, findType(r.getType()).getImage_id());
        viewHolder.entryDate.setText(r.getDate());
        viewHolder.entryTime.setText(r.getTime());
        viewHolder.commentTv.setText(r.getComment());
        viewHolder.itemTypeImg.setImageResource(gvp_imageID);
        if ("-1".equals(r.getClasses())) {
            viewHolder.feeTv.setText("-" + r.getCost());
            viewHolder.feeTv.setTextColor(getTxtColor(context, R.color.reduce_color));
        } else {
            viewHolder.feeTv.setText("+" + r.getCost());
            viewHolder.feeTv.setTextColor(getTxtColor(context, R.color.rent));
        }
        return view;
    }

    private class ViewHolder {
        MyNumberTextView entryDate;
        MyNumberTextView entryTime;
        ImageView itemTypeImg;
        TextView commentTv;
        MyNumberTextView feeTv;

    }
}