package com.lckiss.weekly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lckiss.weekly.lib.datepicker.DateRangePicker;
import com.lckiss.weekly.lib.popupwindow.CommonPopupWindow;
import com.lckiss.weekly.adapter.CommListAdapter;
import com.lckiss.weekly.db.Record;

import org.zackratos.ultimatebar.UltimateBar;

import java.util.ArrayList;
import java.util.List;

import static com.lckiss.weekly.lib.datepicker.DateRangePicker.YEAR_MONTH_DAY;
import static com.lckiss.weekly.util.CSVUtil.exportCSV;
import static com.lckiss.weekly.util.Calculate.bigDecimalToString;
import static com.lckiss.weekly.util.Calculate.getRes;
import static com.lckiss.weekly.util.Constants.DAY;
import static com.lckiss.weekly.util.Constants.DEFAULT;
import static com.lckiss.weekly.util.Constants.MONTH;
import static com.lckiss.weekly.util.Constants.PLUS;
import static com.lckiss.weekly.util.Constants.YEAR;
import static com.lckiss.weekly.util.DataUtil.findCostAround;
import static com.lckiss.weekly.util.DataUtil.findMonthCostList;
import static com.lckiss.weekly.util.DataUtil.findMonthRecordList;
import static com.lckiss.weekly.util.DataUtil.findMonthSaveList;
import static com.lckiss.weekly.util.DataUtil.findRecordAround;
import static com.lckiss.weekly.util.DataUtil.findSaveAround;
import static com.lckiss.weekly.util.DataUtil.findWeekCostList;
import static com.lckiss.weekly.util.DataUtil.findWeekRecoedList;
import static com.lckiss.weekly.util.DataUtil.findWeekSaveList;
import static com.lckiss.weekly.util.DataUtil.findYearCostList;
import static com.lckiss.weekly.util.DataUtil.findYearRecordList;
import static com.lckiss.weekly.util.DataUtil.findYearSaveList;
import static com.lckiss.weekly.util.TimeUtil.compareDate;
import static com.lckiss.weekly.util.TimeUtil.getDayOfMonth;
import static com.lckiss.weekly.util.TimeUtil.getMonth;
import static com.lckiss.weekly.util.TimeUtil.getWeekLists;
import static com.lckiss.weekly.util.TimeUtil.getYear;
import static com.lckiss.weekly.widget.MyUtils.showToast;

public class EntryStatisticsActivity extends AppCompatActivity implements CommonPopupWindow.ViewInterface, View.OnClickListener {
    private List<Record> costList = new ArrayList<>();
    private List<Record> saveList = new ArrayList<>();
    private List<Record> records = new ArrayList<>();
    private CommonPopupWindow popupWindow;
    private CommListAdapter adapter;
    private TextView flag_date, entry_cost, entry_save, entry_fee;
    private Float ALLMONEY = 2000F;
    private View listview;
    private RelativeLayout no_data;

    //从MainActivity传过来的倍数以及月份
    private String month;
    private String day;
    private String year;


    private DateRangePicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_statistics);
        initView();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //状态栏的特殊处理
        UltimateBar ultimateBar = new UltimateBar(this);
        ultimateBar.setColorBar(ContextCompat.getColor(this, R.color.line_color));

        //获取每月默认金额
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String value = preferences.getString("budget", "");
        try {
            ALLMONEY = Float.parseFloat(value);
        } catch (Exception e) {
            Log.d("info:", "ALLMONEY : 未包含数据，使用默认数据");
        }

        Intent intent = getIntent();
        month = intent.getStringExtra("currentMonth");
        day = intent.getStringExtra("currentDay");
        year = intent.getStringExtra("currentYear");

        flag_date = (TextView) findViewById(R.id.flag_date);
        flag_date.setOnClickListener(this);
        entry_cost = (TextView) findViewById(R.id.entry_cost);
        entry_save = (TextView) findViewById(R.id.entry_save);
        entry_fee = (TextView) findViewById(R.id.entry_fee);
        listview=findViewById(R.id.comm_listview);
        no_data=(RelativeLayout) findViewById(R.id.no_data);
        getRecordByWeek();

        //listView的初始化
        ListView listview = (ListView) findViewById(R.id.entry_listview);
        adapter = new CommListAdapter(this, records);
        listview.setAdapter(adapter);

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
                    showToast(EntryStatisticsActivity.this, "截止日期不能小于起始日期");
                }
            }
        });
    }

    public void getRecordByWeek() {
        isEmpty(costList);
        isEmpty(saveList);
        isEmpty(records);
        //获取当前周的每天日期,按星期一到星期天排
        List<String> dateList = getWeekLists(day);
        records.addAll(findWeekRecoedList(dateList));
        costList.addAll(findWeekCostList(dateList));
        saveList.addAll(findWeekSaveList(dateList));
        updateText("本周",DAY);
    }

    public void getRecordByMonth() {
        isEmpty(costList);
        isEmpty(saveList);
        isEmpty(records);
        records.addAll(findMonthRecordList(month));
        costList.addAll(findMonthCostList(month));
        saveList.addAll(findMonthSaveList(month));
        updateText(year+"年"+month+"月",MONTH);
    }

    public void getRecordByYear() {
        isEmpty(costList);
        isEmpty(saveList);
        isEmpty(records);
        records.addAll(findYearRecordList(year));
        costList.addAll(findYearCostList(year));
        saveList.addAll(findYearSaveList(year));
        updateText(year+"年度",YEAR);
    }

    public void getRecordAround(String start, String end) {
        isEmpty(costList);
        isEmpty(saveList);
        isEmpty(records);
        records.addAll(findRecordAround(start, end));
        costList.addAll(findCostAround(start, end));
        saveList.addAll(findSaveAround(start, end));
        adapter.notifyDataSetChanged();
        updateText(start + "至" + end,DEFAULT);
    }

    public void isEmpty(List<?> l) {
        if (!l.isEmpty()) {
            l.clear();
        }
    }

    private void updateText(String flag,int type) {
        Float cost = getRes(costList, PLUS);
        Float save = getRes(saveList, PLUS);
        Float fee;
        switch (type) {
            case DAY:
                fee = ALLMONEY / 4 + save - cost;
                break;
            case MONTH:
                fee = ALLMONEY + save - cost;
                break;
            case YEAR:
                fee = ALLMONEY * 12 + save - cost;
                break;
            default:
                fee = save - cost;
                break;
        }
        flag_date.setText(flag);
        entry_cost.setText("支出:" + bigDecimalToString(cost));
        entry_save.setText("收入:" + bigDecimalToString(save));
        entry_fee.setText("结余:" + bigDecimalToString(fee));

        //为空时隐藏列表显示空布局
        if (records.size()==0){
            setViewVisible(true);
        }else {
            setViewVisible(false);
        }
    }

    private void setViewVisible(Boolean flag) {
        if (flag) {
            no_data.setVisibility(View.VISIBLE);
            listview.setVisibility(View.GONE);
        }else {
            no_data.setVisibility(View.GONE);
            listview.setVisibility(View.VISIBLE);
        }
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
        //得到button的左上角坐标
//        int[] positions = new int[2];
//        view.getLocationOnScreen(positions);
//        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.NO_GRAVITY, 0, positions[1] + view.getHeight());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.output_csv:
                exportByFlag();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportByFlag() {
        boolean res= exportCSV(records,flag_date.getText()+"账目导出");
        if (res){
            showToast(this,"导出成功");
        }else {
            showToast(this,"导出失败,请检查是否授予存储权限");
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
                adapter.notifyDataSetChanged();
                popupWindow.dismiss();
                break;
            case R.id.pop_month:
                getRecordByMonth();
                adapter.notifyDataSetChanged();
                popupWindow.dismiss();
                break;
            case R.id.pop_year:
                getRecordByYear();
                adapter.notifyDataSetChanged();
                popupWindow.dismiss();
                break;
            case R.id.pop_by:
                picker.show();
                popupWindow.dismiss();
                break;
        }
    }

}
