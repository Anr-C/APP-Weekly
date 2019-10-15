package com.lckiss.weekly;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lckiss.weekly.lib.slidingmenu.SlidingMenu;
import com.lckiss.weekly.adapter.CommListAdapter;
import com.lckiss.weekly.adapter.MyListViewAdapter;
import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.util.ScreenShot;
import com.lckiss.weekly.widget.LineView;

import org.zackratos.ultimatebar.UltimateBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.view.View.inflate;
import static com.lckiss.weekly.util.Calculate.bigDecimalToString;
import static com.lckiss.weekly.util.Calculate.getRes;
import static com.lckiss.weekly.util.Calculate.getWeekCostList;
import static com.lckiss.weekly.util.Calculate.getWeekMoneyList;
import static com.lckiss.weekly.util.Calculate.getWeekSaveList;
import static com.lckiss.weekly.util.Constants.PLUS;
import static com.lckiss.weekly.util.Constants.WEEKSUM;
import static com.lckiss.weekly.util.Constants.defaultNUm;
import static com.lckiss.weekly.util.DataUtil.findMonthCostList;
import static com.lckiss.weekly.util.DataUtil.findMonthSaveList;
import static com.lckiss.weekly.util.DataUtil.findRecordByDay;
import static com.lckiss.weekly.util.DataUtil.findWeekRecordLists;
import static com.lckiss.weekly.util.ResUtil.getTxtColor;
import static com.lckiss.weekly.util.InitDB.initTypeDB;
import static com.lckiss.weekly.util.TimeUtil.getDate;
import static com.lckiss.weekly.util.TimeUtil.getDayOfWeek;
import static com.lckiss.weekly.util.TimeUtil.getMonthByDay;
import static com.lckiss.weekly.util.TimeUtil.getPreWeekLists;
import static com.lckiss.weekly.util.TimeUtil.getWeekLists;
import static com.lckiss.weekly.util.TimeUtil.getWeekNumber;
import static com.lckiss.weekly.util.TimeUtil.getYearByDay;
import static com.lckiss.weekly.widget.MyUtils.showToast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "info";
    private SlidingMenu slidingMenu;
    private TextView slideMonth, slideBudget_number, slideMonth_budget_expend, slideMonth_budget_leave;
    private TextView slideMonth_save, slideMonth_expend;
    private TextView slideMonth_balance;

    private LineView lineView;
    private TextView date_txt, cost_txt, week_save, week_cost;
    private ProgressBar main_progress_bar, slide_progress_bar;

    private ArrayList<View> viewList;
    private static ViewPager viewPager;

    private static List<String> dateList;
    private static int currentItem;

    private static String currentDay;
    private static String currentMonth;
    private static String currentYear;
    private static String tmpMonth;

    private Intent extraIntent;
    private Intent intent;

    private List<List<Record>> recordList;
    private ArrayList<Float> weekMoneyList;
    private ArrayList<Float> weekCostList;
    private ArrayList<Float> weekSaveList;

    private List<MyListViewAdapter> adapterList = new ArrayList<>();

    private List<Record> costListByMonth;
    private List<Record> saveListByMonth;
    private List<Record> tmpDialogList;

    private Float ALLMONEY = 0F;
    private int multiple = 0;

    //判断是否为线程更新数据
    private boolean isThread = false;
    //第一次线程更新不需要刷新viewpager
    private boolean isFirstLoad = false;
    private Float costByMonth = 0F;
    private Float saveByMonth = 0F;

    private Float weekCost = 0F;
    private Float weekSave = 0F;
    private Float currentMoney = 0F;

    private ActionBarDrawerToggle drawerToggle;

    private ExecutorService cachedThreadPool;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    //删除成功后更新当天
                    String day = (String) msg.obj;
                    updateDate(0, day);
                    break;
                case 200:
                    //删除失败提醒
                    showToast(MainActivity.this, "操作失败，稍后再试");
                    break;
                case 300:
                    //修改的跳转
                    Record preRecord = (Record) msg.obj;
                    intent = new Intent(MainActivity.this, AddRecordActivity.class);
                    intent.putExtra("Record", preRecord);
                    startActivityForResult(intent, 2);
                    break;
                case 500:
                    //线程计算完数据后更新控件
                    isThread = (boolean) msg.obj;
                    updateCostTxt();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化线程池
        cachedThreadPool = Executors.newCachedThreadPool();
        initDate();
        initAndriodChats();
        initView();
        initViewPager();
    }

    private void initDate() {
        currentDay = getDate();
        //获取当前周的每天日期,按星期一到星期天排
        dateList = getWeekLists(currentDay);
        //第一次初始化时存储当前月份,当月份改变时,刷新数据
        tmpMonth = getMonthByDay(currentDay);
        //一周中的某天,用于指定viewpager的位置
        currentItem = getDayOfWeek();
        //更新当前年月日
        updateCurrent();
        recordList = findWeekRecordLists(dateList);
        updateInThread();
        //更新检测
        new UpdateManager(MainActivity.this,false);
    }

    private void initAndriodChats() {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //周消费列表
                weekCostList = getWeekCostList(dateList);
                //initAndriodChats
                lineView = (LineView) findViewById(R.id.line_view);
                String[] date = {"一", "二", "三", "四", "五", "六", "日"};
                ArrayList<String> myWeek = new ArrayList<>();
                myWeek.addAll(Arrays.asList(date).subList(0, 7));
                lineView.setDrawDotLine(true); //optional
                lineView.setShowPopup(currentItem); //optional
                lineView.setBottomTextList(myWeek);
                lineView.setColorArray(new int[]{getTxtColor(MainActivity.this, R.color.reduce_color)});
                lineView.setFloatDataList(weekCostList);
                //检查第一次，初始化数据
                SharedPreferences setting = getSharedPreferences("checkFirst", MODE_PRIVATE);
                Boolean user_first = setting.getBoolean("isFirst", true);
                if (user_first) {
                    setting.edit().putBoolean("isFirst", false).apply();
                    initTypeDB();
                }
                //获取每月默认金额
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String value = preferences.getString("budget", defaultNUm.toString());
                try {
                    ALLMONEY = Float.parseFloat(value);
                } catch (Exception e) {
                    Log.d(TAG, "ALLMONEY : 未包含数据，使用默认数据");
                }
            }
        });
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //状态栏的特殊处理
        UltimateBar ultimateBar = new UltimateBar(this);
        ultimateBar.setColorBar(ContextCompat.getColor(this, R.color.line_color));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(MainActivity.this, AddRecordActivity.class);
                intent.putExtra("today", currentDay);
                startActivityForResult(intent, 1);
            }
        });
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       getSupportActionBar().setHomeButtonEnabled(true);
        /*-------已实现slidingmenu配合drawerToggle联动,但动画时长太短-------*/
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.dr);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
        /*--------------------------------------------------*/
        //initSlidingMenu
        slidingMenu = new SlidingMenu(this);//创建对象
        slidingMenu.setMode(SlidingMenu.LEFT);//设定模式，SlidingMenu在左边
        slidingMenu.setBehindOffsetRes(R.dimen.sliding_menu_offset);//配置slidingmenu偏移出来的尺寸
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);//设置为只边缘，打开slidingmenu
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);//附加到当前的activity上去
        View leftView = inflate(this, R.layout.drawable_tab, null);
        slidingMenu.setMenu(leftView);//也可以直接跟layout id


       slidingMenu.setOnCloseListener(new SlidingMenu.OnCloseListener() {
           @Override
           public void onClose() {
               drawerToggle.onDrawerClosed(drawerLayout);
           }
       });
       slidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
           @Override
           public void onOpen() {
               drawerToggle.onDrawerOpened(drawerLayout);
           }
       });

        //控件初始化
        date_txt = (TextView) findViewById(R.id.date_txt);
        cost_txt = (TextView) findViewById(R.id.cost_txt);
        week_save = (TextView) findViewById(R.id.week_cost);
        week_cost = (TextView) findViewById(R.id.week_save);

        ImageView previousWeek = (ImageView) findViewById(R.id.previousWeek);
        previousWeek.setOnClickListener(this);
        ImageView nextWeek = (ImageView) findViewById(R.id.nextWeek);
        nextWeek.setOnClickListener(this);

        main_progress_bar = (ProgressBar) findViewById(R.id.main_progress_bar);
        slide_progress_bar = leftView.findViewById(R.id.slide_progress_bar);

        //sliding控件
        slideMonth = leftView.findViewById(R.id.month);
        slideBudget_number = leftView.findViewById(R.id.budget_number);
        slideMonth_budget_expend = leftView.findViewById(R.id.month_budget_expend);
        slideMonth_budget_leave = leftView.findViewById(R.id.month_budget_leave);
        slideMonth_save = leftView.findViewById(R.id.month_save);
        slideMonth_expend = leftView.findViewById(R.id.month_expend);
        slideMonth_balance = leftView.findViewById(R.id.month_balance);

        //设置点击事件
        LinearLayout setting_layout = leftView.findViewById(R.id.setting_layout);
        setting_layout.setOnClickListener(this);
        //收入
        RelativeLayout monthSaveRelative = leftView.findViewById(R.id.month_save_relative);
        monthSaveRelative.setOnClickListener(this);
        //支出
        RelativeLayout monthExpendRelative = leftView.findViewById(R.id.month_expend_relative);
        monthExpendRelative.setOnClickListener(this);
    }

    private static void updateCurrent() {
        currentDay = dateList.get(currentItem);
        currentMonth = getMonthByDay(currentDay);
        currentYear = getYearByDay(currentDay);
    }

    private void updateCostTxt() {
        if (isFirstLoad){
            lineView.setFloatDataList(weekCostList);
        }
        if (isThread) {
            for (int i = 0; i < WEEKSUM; i++) {
                adapterList.get(i).notifyDataSetChanged();
            }
            viewPager.setCurrentItem(currentItem);
            isFirstLoad=isThread;
        }

        String sMoney = bigDecimalToString(currentMoney);
        String sCost = bigDecimalToString(weekCost);
        String sSave = bigDecimalToString(weekSave);


        date_txt.setText(currentDay);
        cost_txt.setText(sMoney);
        cost_txt.setTextColor(currentMoney <= 0 ? getTxtColor(this, R.color.reduce_color) : getTxtColor(this, R.color.rent));
        week_cost.setText(sCost);
        week_cost.setTextColor(getTxtColor(this, R.color.reduce_color));
        week_save.setText(sSave);
        week_save.setTextColor(getTxtColor(this, R.color.rent));

        int slideProMax = (int) ((ALLMONEY+saveByMonth)/ 100F * 100);
        int mainProMax = slideProMax / 4;
        main_progress_bar.setMax(mainProMax);
        slide_progress_bar.setMax(slideProMax);

        slide_progress_bar.setProgress((int) (costByMonth / 100 * 100));
        main_progress_bar.setProgress((int) (weekCost / 100 * 100));


        slideMonth.setText(Integer.parseInt(currentMonth) + "月概况");
        slideBudget_number.setText(ALLMONEY+saveByMonth + "");
        //消费百分比
        Float percentage;
        try{
            percentage= Float.valueOf(bigDecimalToString(costByMonth / slideProMax * 100));
        }
        catch (Exception e){
            Log.d(TAG, "Mainactivty...updateCostTxt: 0作为被除数错误");
            percentage= 0F;
        }

        slideMonth_budget_expend.setText("已支出" + costByMonth + "(" + percentage + "%)");
        slideMonth_budget_leave.setText(ALLMONEY - costByMonth + "");
        slideMonth_save.setText("+" + saveByMonth);
        slideMonth_expend.setText("-" + costByMonth);
        slideMonth_balance.setText("结余:" + (ALLMONEY + saveByMonth - costByMonth));
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.content_viewpager);
        viewList = new ArrayList<>();
        //7天,7页,7个view
        for (int i = 0; i < 7; i++) {
            View v = getLayoutInflater().inflate(R.layout.list_item, null);
            viewList.add(v);
        }

        viewPager.setAdapter(new MyPagerAdapter());

        for (int i = 0; i < dateList.size(); i++) {
            ListView listView = viewList.get(i).findViewById(R.id.content_listview);
            adapterList.add(new MyListViewAdapter(handler, listView, this, recordList.get(i)));
            listView.setAdapter(adapterList.get(i));
        }
//       viewPager.setCurrentItem(currentItem);
        //缓存3个页面
//        viewPager.setOffscreenPageLimit(3);
        // 设置ViewPager的监听事件

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int isSelected) {
                // 当当天条目是0的时候，设置可以在任意位置拖拽出SlidingMenu
                if (isSelected == 0) {
                    slidingMenu.setTouchModeAbove(
                            SlidingMenu.TOUCHMODE_FULLSCREEN);
                } else {
                    // 当在其他位置的时候，设置不可以拖拽出来(SlidingMenu.TOUCHMODE_NONE)，或只有在边缘位置才可以拖拽出来TOUCHMODE_MARGIN
                    slidingMenu.setTouchModeAbove(
                            SlidingMenu.TOUCHMODE_MARGIN);
                }
                currentItem = isSelected;
                lineView.setShowPopup(currentItem);
                updateCurrent();
                updatePager();
                //判断月份是否改变,改变则更新全文
                if (!tmpMonth.equals(currentMonth)) {
                    updateInThread();
                    tmpMonth = currentMonth;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void updateDate(int mul, String days) {
        if (days == null) {
            dateList = getPreWeekLists(mul);
        } else {
            //获取当前周的每天日期,按星期一到星期天排
            dateList = getWeekLists(days);
            currentItem = getWeekNumber(days) - 1;
        }
        //更新当前年月日
        updateCurrent();
        //子线程更新数据
        updateInThread();
    }

    private void buildDialog(String classes) {
        if (tmpDialogList.size()==0){
            Record record=new Record();
            record.setClasses(classes);
            record.setCost("0");
            record.setComment("当前暂无数据");
            tmpDialogList.add(record);
        }
        //侧边栏弹出dialog初始化
        View dialogView = View.inflate(MainActivity.this, R.layout.comm_listview, null);//填充ListView布局
        ListView dialogListView = dialogView.findViewById(R.id.entry_listview);//初始化ListView控件
        dialogListView.setAdapter(new CommListAdapter(MainActivity.this, tmpDialogList));//ListView设置适配器
        AlertDialog listDialog = new AlertDialog.Builder(MainActivity.this)
                .setView(dialogView)//在这里把写好的这个listview的布局加载dialog中
                .create();
        listDialog.show();
    }

    public static void setCurrentItem(int currentItem) {
        viewPager.setCurrentItem(currentItem);
    }

    private Intent getExtraIntent(Class ActClass) {
        extraIntent = new Intent(this, ActClass);
        extraIntent.putExtra("currentDay", currentDay);
        extraIntent.putExtra("currentMonth", currentMonth);
        extraIntent.putExtra("currentYear", currentYear);
        return extraIntent;
    }

    private void updateInThread() {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //侧边栏列表
                costListByMonth = findMonthCostList(currentYear, currentMonth);
                saveListByMonth = findMonthSaveList(currentYear, currentMonth);
                if (isThread) {
                    for (int i = 0; i < WEEKSUM; i++) {
                        recordList.get(i).clear();
                        recordList.get(i).addAll(findRecordByDay(dateList.get(i)));
                    }
                }
                weekMoneyList = getWeekMoneyList(dateList);
                weekCostList = getWeekCostList(dateList);
                weekSaveList = getWeekSaveList(dateList);
                //每个月的消费
                costByMonth = getRes(costListByMonth, PLUS);
                saveByMonth = getRes(saveListByMonth, PLUS);

                //初始化数据
                weekCost = 0F;
                weekSave = 0F;
                for (int i = 0; i < WEEKSUM; i++) {
                    weekCost += weekCostList.get(i);
                    weekSave += weekSaveList.get(i);
                }
                currentMoney = weekMoneyList.get(currentItem);

                //线程数据更新完毕
                Message msg = new Message();
                msg.what = 500;
                msg.obj = true;
                handler.sendMessage(msg);
            }
        });
    }

    private void updatePager() {
        date_txt.setText(dateList.get(currentItem));
        cost_txt.setText(weekMoneyList.get(currentItem).toString());
        cost_txt.setTextColor(weekMoneyList.get(currentItem) < 0 ? getTxtColor(this, R.color.reduce_color) : getTxtColor(this, R.color.rent));
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = viewList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewList.get(position));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previousWeek:
                multiple++;
                updateDate(multiple, null);
                break;
            case R.id.nextWeek:
                multiple--;
                updateDate(multiple, null);
                break;
            case R.id.setting_layout:
                intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, 3);
                break;
            case R.id.month_save_relative:
                tmpDialogList = saveListByMonth;
                buildDialog("0");
                break;
            case R.id.month_expend_relative:
                tmpDialogList = costListByMonth;
                buildDialog("-1");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                slidingMenu.toggle();
                break;
            case R.id.lck_setting:
                intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, 3);
                break;
            case R.id.type_edit:
                intent = new Intent(this, CategoryEditActivity.class);
                startActivityForResult(intent,4);
                break;
            case R.id.back_now:
                multiple = 0;
                currentItem = getDayOfWeek();
                updateDate(multiple, getDate());
                break;
            case R.id.lck_equalizer:
                extraIntent = getExtraIntent(PieBarActivity.class);
                startActivity(extraIntent);
                break;
            case R.id.lck_subjec:
                extraIntent = getExtraIntent(EntryStatisticsActivity.class);
                startActivity(extraIntent);
                break;
            case R.id.exit:
                finish();
                break;
            case R.id.share:
                View decorView = getWindow().getDecorView();
                ScreenShot.getInstance().shotNow(this,decorView);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            switch (requestCode) {
                case 1:
                    String addRes = data.getStringExtra("status");
                    String addDay = data.getStringExtra("date");
                    Log.d(TAG, "onActivityResult: "+addDay+"---"+addRes);
                    if ("ture".equals(addRes)) {
                        updateDate(0, addDay);
                    } else {
                        showToast(this, "操作失败，请重新尝试");
                    }
                    break;
                case 2:
                    String modRes = data.getStringExtra("status");
                    String modDay = data.getStringExtra("date");
                    if ("ture".equals(modRes)) {
                        updateDate(0, modDay);
                    } else {
                        showToast(this, "操作失败，请重新尝试");
                    }
                    break;
                case 3:
                    //更新每月预算
                    ALLMONEY = data.getFloatExtra("budget", 0F);
//                    updateCostTxt();
                    updateInThread();
                    break;
                case 4:
                    //修改类别后更新数据
                    updateInThread();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cachedThreadPool.shutdown();
    }
}
