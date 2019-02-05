package com.lckiss.weekly.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lckiss.weekly.adapter.CommListAdapter;
import com.lckiss.weekly.R;
import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.db.Type;
import com.lckiss.weekly.lib.graph.PieGraph;
import com.lckiss.weekly.lib.graph.PieSlice;
import com.lckiss.weekly.widget.MyNumberTextView;


import java.util.ArrayList;
import java.util.List;

import static com.lckiss.weekly.util.Calculate.getRes;
import static com.lckiss.weekly.util.Constants.PLUS;
import static com.lckiss.weekly.util.DataUtil.findAllType;
import static com.lckiss.weekly.util.DataUtil.findType;
import static com.lckiss.weekly.util.ResUtil.getImageId;

public class PieFragment extends Fragment {
    private static final String TAG = "info:";
    private List<List<Record>> records = new ArrayList<>();
    private List<List<Record>> listRecords = new ArrayList<>();
    private PieGraph pg;
    private ListView pie_listView;
    private List<Record> tmpList = new ArrayList<>();
    private List<Type> listType=new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_piegraph, container, false);
        pg = v.findViewById(R.id.piegraph);

        records = (List<List<Record>>) getArguments().getSerializable("ListCost");
        pie_listView = v.findViewById(R.id.pie_listView);


        listType=findAllType();

        initPie();
        pg.setOnSliceClickedListener(new PieGraph.OnSliceClickedListener() {
            @Override
            public void onClick(int index) {
                if (index < 0) {
                    //中心区域为-1 无需设置数据，亦不可读取数据
                    return;
                }
                buildDialog(index);
            }
        });

        return v;
    }

    private void initPie() {

        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).size()!=0) {
                listRecords.add(records.get(i));
                Float value = getRes(records.get(i), PLUS);
                Record rf=records.get(i).get(0);
                Type t = findType(rf.getType());

                int color = Color.parseColor("#" + t.getColor());
                String title = t.getDescribe();
                //图表初始化
                PieSlice slice = new PieSlice();
                slice.setColor(color);
                slice.setValue(value);
                slice.setTitle(title);
                slice.setGoalValue(value);

                Record r = new Record();
                r.setComment(title);
                r.setCost(value.toString());
                r.setType(rf.getType());
                tmpList.add(r);

                pg.setDuration(1000);//default if unspecified is 300 ms
                pg.setInterpolator(new AccelerateDecelerateInterpolator());//default if unspecified is linear; constant speed
//                pg.setAnimationListener(getAnimationListener());
                pg.animateToGoalValues();
                pg.addSlice(slice);
            }
        }
        pie_listView.setAdapter(new MyAdapter());
        pie_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                buildDialog(i);
            }
        });
    }

    public void buildDialog(int index) {
        View dialogView = View.inflate(getActivity(), R.layout.comm_listview, null);//填充ListView布局
        ListView dialogListView = dialogView.findViewById(R.id.entry_listview);//初始化ListView控件
        dialogListView.setAdapter(new CommListAdapter(getActivity(), listRecords.get(index)));//ListView设置适配器

        AlertDialog listDialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)//在这里把写好的这个listview的布局加载dialog中
                .create();
        listDialog.show();
    }

/*    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public Animator.AnimatorListener getAnimationListener() {
            return new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
//                    Log.d("piefrag", "anim end");
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    //you might want to call slice.setvalue(slice.getGoalValue)
//                    Log.d("piefrag", "anim cancel");
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            };

    }*/

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return tmpList.size();
        }

        @Override
        public Object getItem(int i) {
            return tmpList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            Record r = tmpList.get(i);
            Type t = findType(r.getType());
            //动态设置图标
            int imageID = getImageId(getActivity(), t.getImage_id());
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.activity_pie_item, null);
                viewHolder = new ViewHolder();
                viewHolder.typeImage = view.findViewById(R.id.type_image);
                viewHolder.typeTitle = view.findViewById(R.id.type_title);
                viewHolder.expendMoney = view.findViewById(R.id.expend_money);
                view.setTag(viewHolder);
            }else {
                viewHolder=(ViewHolder)view.getTag();
            }
            viewHolder.typeImage.setImageResource(imageID);
            viewHolder.typeTitle.setText(r.getComment());
            viewHolder.expendMoney.setText(r.getCost());
            return view;
        }
    }

    private class ViewHolder {
        ImageView typeImage;
        TextView typeTitle;
        MyNumberTextView expendMoney;
    }
}
