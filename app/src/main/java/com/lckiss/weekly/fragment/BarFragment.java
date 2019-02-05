package com.lckiss.weekly.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ListView;

import com.lckiss.weekly.adapter.CommListAdapter;
import com.lckiss.weekly.R;
import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.db.Type;
import com.lckiss.weekly.lib.graph.Bar;
import com.lckiss.weekly.lib.graph.BarGraph;
import com.lckiss.weekly.lib.graph.HoloGraphAnimate;

import java.util.ArrayList;
import java.util.List;

import static com.lckiss.weekly.util.Calculate.getRes;
import static com.lckiss.weekly.util.Constants.PLUS;
import static com.lckiss.weekly.util.DataUtil.findType;

public class BarFragment extends Fragment {
    BarGraph barGraph;
    private List<List<Record>> records = new ArrayList<>();
    private List<List<Record>> listRecords = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_bargraph, container, false);

        barGraph = v.findViewById(R.id.bargraph);

        records = (List<List<Record>>) getArguments().getSerializable("ListSave");

        initBar();

        barGraph.setOnBarClickedListener(new BarGraph.OnBarClickedListener() {
            @Override
            public void onClick(int index) {

                View dialogView = View.inflate(getActivity(),R.layout.comm_listview,null);//填充ListView布局
                ListView dialogListView = dialogView.findViewById(R.id.entry_listview);//初始化ListView控件
                dialogListView.setAdapter(new CommListAdapter(getActivity(),listRecords.get(index)));//ListView设置适配器
                AlertDialog listDialog = new AlertDialog.Builder(getActivity())
                        .setView(dialogView)//在这里把写好的这个listview的布局加载dialog中
                        .create();
                listDialog.show();
            }
        });
        return v;
    }

    private void initBar() {
        ArrayList<Bar> aBars = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).size()!=0){
                listRecords.add(records.get(i));
                Float cost=getRes(records.get(i),PLUS);
                Record rf=records.get(i).get(0);
                Type t = findType(rf.getType());
                //图表初始化
                Bar bar = new Bar();
                bar.setColor(Color.parseColor("#" + t.getColor()));
//              bar.setSelectedColor(resources.getColor(R.color.transparent_orange));
                bar.setName(t.getDescribe());
                bar.setGoalValue(cost);
                bar.setValue(0);
                bar.setValueString("$"+cost);
                bar.setValuePrefix("$");
                bar.mAnimateSpecial = HoloGraphAnimate.ANIMATE_INSERT;
                aBars.add(bar);
            }
        }
        barGraph.setShowAxis(false);
        barGraph.setBars(aBars);
        barGraph.setDuration(1200);//default if unspecified is 300 ms
        barGraph.setInterpolator(new AccelerateDecelerateInterpolator());//Only use over/undershoot  when not inserting/deleting
//        barGraph.setAnimationListener(getAnimationListener());
        barGraph.animateToGoalValues();
    }

/*    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public Animator.AnimatorListener getAnimationListener(){
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
//                    Log.i("info:", "onAnimationStart: 动画开始");
            }
            @Override
            public void onAnimationEnd(Animator animation) {
//                    Log.i("info:", "onAnimationStart: 动画结束");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }*/
}
