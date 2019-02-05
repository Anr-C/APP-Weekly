package com.lckiss.weekly;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lckiss.weekly.db.Type;
import com.lckiss.weekly.fragment.BarFragment;
import com.lckiss.weekly.fragment.PieFragment;
import com.lckiss.weekly.lib.datepicker.DateRangePicker;
import com.lckiss.weekly.lib.popupwindow.CommonPopupWindow;
import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.util.ScreenShot;

import org.zackratos.ultimatebar.UltimateBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.lckiss.weekly.lib.datepicker.DateRangePicker.YEAR_MONTH_DAY;
import static com.lckiss.weekly.util.Calculate.bigDecimalToString;
import static com.lckiss.weekly.util.Calculate.getRes;
import static com.lckiss.weekly.util.Constants.PLUS;
import static com.lckiss.weekly.util.DataUtil.findAllType;
import static com.lckiss.weekly.util.DataUtil.findCostAround;
import static com.lckiss.weekly.util.DataUtil.findMonthCostList;
import static com.lckiss.weekly.util.DataUtil.findMonthSaveList;
import static com.lckiss.weekly.util.DataUtil.findSaveAround;
import static com.lckiss.weekly.util.DataUtil.findWeekCostList;
import static com.lckiss.weekly.util.DataUtil.findWeekSaveList;
import static com.lckiss.weekly.util.DataUtil.findYearCostList;
import static com.lckiss.weekly.util.DataUtil.findYearSaveList;
import static com.lckiss.weekly.util.TimeUtil.compareDate;
import static com.lckiss.weekly.util.TimeUtil.getDayOfMonth;
import static com.lckiss.weekly.util.TimeUtil.getMonth;
import static com.lckiss.weekly.util.TimeUtil.getWeekLists;
import static com.lckiss.weekly.util.TimeUtil.getYear;
import static com.lckiss.weekly.widget.MyUtils.showToast;

public class PieBarActivity extends AppCompatActivity implements CommonPopupWindow.ViewInterface, View.OnClickListener {
    private CommonPopupWindow popupWindow;
    private TextView pie_cost_txt, pie_save_txt, flag_date;
    private Float cost, save;
    private List<List<Record>> listCostByType = new ArrayList<>();
    private List<List<Record>> listSaveByType = new ArrayList<>();
    private LinearLayout barNoData, pieNoData;


    private List<Record> costList = new ArrayList<>();
    private List<Record> saveList = new ArrayList<>();

    private List<String> listType = new ArrayList<>();

    //从MainActivity传过来的倍数以及月份
    private String month;
    private String day;
    private String year;

    private DateRangePicker picker;
    private static final String TAG = "info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie);
        initView();
        initTypeData();
        getRecordByWeek();
        updateFragment();
    }

    private void initTypeData() {
        List<Type> tmp = findAllType();
        for (Type t : tmp
                ) {
            listType.add(t.getType());
        }
    }

    public void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //状态栏的特殊处理
        UltimateBar ultimateBar = new UltimateBar(this);
        ultimateBar.setColorBar(ContextCompat.getColor(this, R.color.line_color));

        pie_cost_txt = (TextView) findViewById(R.id.pie_cost_txt);
        pie_save_txt = (TextView) findViewById(R.id.pie_save_txt);
        flag_date = (TextView) findViewById(R.id.flag_date);
        flag_date.setOnClickListener(this);

        barNoData = (LinearLayout) findViewById(R.id.bar_nodata);
        pieNoData = (LinearLayout) findViewById(R.id.pie_nodata);

        Intent intent = getIntent();
        month = intent.getStringExtra("currentMonth");
        day = intent.getStringExtra("currentDay");
        year = intent.getStringExtra("currentYear");

        //初始化选择器
        picker = new DateRangePicker(this, YEAR_MONTH_DAY, true);
        //选择器
        picker.setGravity(Gravity.CENTER);
        picker.setDateRangeStart(1997, 1, 1);
        picker.setDateRangeEnd(2030, 12, 30);
        picker.setTextSize(16);
        picker.setSelectedItem(getYear(), getMonth(), getDayOfMonth());
        picker.setSelectedSecondItem(getYear(), getMonth(), getDayOfMonth());
        picker.setOnDatePickListener(new DateRangePicker.OnYearMonthDayDoublePickListener() {
            @Override
            public void onDatePicked(String startYear, String startMonth, String startDay, String endYear, String endMonth, String endDay) {
                String dateStart = startYear + "-" + startMonth + "-" + startDay;
                String dateEnd = endYear + "-" + endMonth + "-" + endDay;
                if (compareDate(dateStart, dateEnd)) {
                    getRecordAround(dateStart, dateEnd);
                } else {
                    showToast(getBaseContext(), "截止日期不能小于起始日期");
                }
            }
        });
    }

    public void setPieVisibility(int i) {
        pieNoData.setVisibility(i);
    }

    public void setBarVisibility(int i) {
        barNoData.setVisibility(i);
    }

    public void updateFragment() {
        pie_cost_txt.setText("支出情况：-" + bigDecimalToString(cost));
        pie_save_txt.setText("收入情况：" + bigDecimalToString(save));

        if (costList.size() == 0) {
            setPieVisibility(View.VISIBLE);
        } else {
            setPieVisibility(View.GONE);
        }
        if (saveList.size() == 0) {
            setBarVisibility(View.VISIBLE);
        } else {
            setBarVisibility(View.GONE);
        }

        if (costList.size() == 0 && saveList.size() == 0) {
            return;
        }

        Bundle Bundle = new Bundle();
        Bundle.putSerializable("ListCost", (Serializable) listCostByType);
        Bundle.putSerializable("ListSave", (Serializable) listSaveByType);

        PieFragment pie = new PieFragment();
        pie.setArguments(Bundle);
        this.getFragmentManager().beginTransaction()
                .replace(R.id.pie_cost, pie)
                .commit();

        BarFragment bar = new BarFragment();
        bar.setArguments(Bundle);
        this.getFragmentManager().beginTransaction()
                .replace(R.id.bar_save, bar)
                .commit();


    }

    public void recordsByType() {
        cost = getRes(costList, PLUS);
        save = getRes(saveList, PLUS);
        //清空原来的
        isEmpty(listCostByType);
        isEmpty(listSaveByType);

        //定义未分类元素
        List<Record> undefinedCost = new ArrayList<>();
        List<Record> undefinedSave = new ArrayList<>();
        //按类别分类循环
        for (int i = 0; i < listType.size(); i++) {
            String type = listType.get(i);
            List<Record> tmpCost = new ArrayList<>();
            List<Record> tmpSave = new ArrayList<>();
            for (Record r : costList) {
                //判断是否存在该类别
                if (listType.contains(r.getType())) {
                    if (type.equals(r.getType())) {
                        tmpCost.add(r);
                    }
                } else {
                    //不存在则添加到未分类列表，同时需判断是否以及包含该元素，避免重复添加
                    if (undefinedCost.contains(r))
                        break;
                    undefinedCost.add(r);
                }
            }
            for (Record r : saveList) {
                if (listType.contains(r.getType())) {
                    if (type.equals(r.getType())) {
                        tmpSave.add(r);
                    }
                } else {
                    if (undefinedSave.contains(r))
                        break;
                    undefinedSave.add(r);
                }
            }
            listCostByType.add(tmpCost);
            listSaveByType.add(tmpSave);
        }
        listCostByType.add(undefinedCost);
        listSaveByType.add(undefinedSave);
    }

    public void getRecordByWeek() {
        //获取当前周的每天日期,按星期一到星期天排
        List<String> dateList = getWeekLists(day);
        costList = findWeekCostList(dateList);
        saveList = findWeekSaveList(dateList);
        flag_date.setText("本周");
        recordsByType();
    }


    public void getRecordByMonth() {
        costList = findMonthCostList(month);
        saveList = findMonthSaveList(month);
        flag_date.setText(year+"年"+month+"月");
        recordsByType();
    }

    public void getRecordByYear() {
        costList = findYearCostList(year);
        saveList = findYearSaveList(year);
        flag_date.setText(year+"年度");
        recordsByType();
    }

    public void getRecordAround(String startDate, String endDate) {
        costList = findCostAround(startDate, endDate);
        saveList = findSaveAround(startDate, endDate);
        flag_date.setText(startDate + "至" + endDate);
        recordsByType();
        updateFragment();
    }

    public void isEmpty(List<?> l) {
        if (!l.isEmpty()) {
            l.clear();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.share:
                View decorView = getWindow().getDecorView();
                ScreenShot.getInstance().shotNow(this, decorView);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //向下弹出
    public void showDownPop(View view) {
        if (popupWindow != null && popupWindow.isShowing()) return;
        popupWindow = new CommonPopupWindow.Builder(this)
                .setView(R.layout.popup_down)
                .setWidthAndHeight(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .setAnimationStyle(R.style.AnimDown)
                .setViewOnclickListener(this)
                .setOutsideTouchable(true)
                .create();
        popupWindow.showAsDropDown(view);
    }

    @Override
    public void getChildView(View view, int layoutResId) {
        switch (layoutResId) {
            case R.layout.popup_down:
                TextView pop_week = view.findViewById(R.id.pop_week);
                TextView pop_month = view.findViewById(R.id.pop_month);
                TextView pop_year = view.findViewById(R.id.pop_year);
                TextView pop_by = view.findViewById(R.id.pop_by);
                pop_week.setOnClickListener(this);
                pop_month.setOnClickListener(this);
                pop_year.setOnClickListener(this);
                pop_by.setOnClickListener(this);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.flag_date:
                showDownPop(view);
                break;
            case R.id.pop_week:
                getRecordByWeek();
                updateFragment();
                popupWindow.dismiss();
                break;
            case R.id.pop_month:
                getRecordByMonth();
                updateFragment();
                popupWindow.dismiss();
                break;
            case R.id.pop_year:
                getRecordByYear();
                updateFragment();
                popupWindow.dismiss();
                break;
            case R.id.pop_by:
                picker.show();
                popupWindow.dismiss();
                break;
        }
    }


}
