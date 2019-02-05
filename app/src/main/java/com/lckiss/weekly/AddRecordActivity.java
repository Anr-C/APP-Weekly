package com.lckiss.weekly;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.db.Type;
import com.lckiss.weekly.lib.datepicker.DatePicker;
import com.lckiss.weekly.widget.CustomKeyboard;
import com.lckiss.weekly.widget.GridViewPager;
import com.lckiss.weekly.widget.MyNumberTextView;

import org.zackratos.ultimatebar.UltimateBar;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lckiss.weekly.util.Calculate.bigDecimalToString;
import static com.lckiss.weekly.util.Calculate.doRes;
import static com.lckiss.weekly.util.Constants.bigCostNum;
import static com.lckiss.weekly.util.DataUtil.findAllType;
import static com.lckiss.weekly.util.DataUtil.newUUID;
import static com.lckiss.weekly.util.DataUtil.updateRecordByUUID;
import static com.lckiss.weekly.util.ResUtil.getImageId;
import static com.lckiss.weekly.util.TimeUtil.getDayOfMonth;
import static com.lckiss.weekly.util.TimeUtil.getMonth;
import static com.lckiss.weekly.util.TimeUtil.getTime;
import static com.lckiss.weekly.util.TimeUtil.getYear;
import static com.lckiss.weekly.widget.MyUtils.showToast;

public class AddRecordActivity extends AppCompatActivity implements
        View.OnClickListener {
    private List<Type> typeList;
    private EditText description_edit, money_edit, date_edit;
    private DatePicker picker;
    private String type;
    private int status;
    private TextView tips_text;
    //供修改使用
    private int flag = 0;
    private Record r;

    private View oldView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        initView();
        initGridViewPager();
        initData();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        //状态栏的特殊处理
        UltimateBar ultimateBar = new UltimateBar(this);
        ultimateBar.setColorBar(ContextCompat.getColor(this, R.color.line_color));

        tips_text = (TextView) findViewById(R.id.tips_text);
        description_edit = (EditText) findViewById(R.id.description_edit);
        money_edit = (EditText) findViewById(R.id.money_edit);
        money_edit.setOnClickListener(this);
        date_edit = (EditText) findViewById(R.id.date_edit);
        date_edit.setOnClickListener(this);

        MyNumberTextView ok = (MyNumberTextView) findViewById(R.id.equal);
        ok.setOnClickListener(this);
        //始终无键盘
        date_edit.setInputType(InputType.TYPE_NULL);
        money_edit.setInputType(InputType.TYPE_NULL);
        //键盘初始化
        CustomKeyboard keyboard = new CustomKeyboard(this);
        keyboard.setParentView(getWindow().getDecorView());
        //初始化选择器
        picker = new DatePicker(this);
        //选择器
        picker.setGravity(Gravity.CENTER);
        picker.setRangeStart(1997,1,1);
        picker.setRangeEnd(2030,12,30);
        picker.setTextSize(16);
        picker.setSelectedItem(getYear(), getMonth(), getDayOfMonth());
        picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
            @Override
            public void onDatePicked(String year, String month, String day) {
                date_edit.setText(year + "-" + month + "-" + day);
            }
        });
    }

    private void initGridViewPager() {
        //初始类别
        typeList = findAllType();
        //GridViewPager
        GridViewPager mGridPager = (GridViewPager) findViewById(R.id.gvp);
        mGridPager.setAdapter(new GridPagerAdapter());
    }


    private void initData() {
        try {
            Intent intent = getIntent();
            r = (Record) intent.getSerializableExtra("Record");
            if (r != null) {
                flag = 1;
                type = r.getType();
                try {
                    status=Integer.parseInt(r.getStatus());
                }catch (Exception e){
                    status=0;
                }
                date_edit.setText(r.getDate());
                money_edit.setText(r.getCost());
                description_edit.setText(r.getComment());
            } else {
                Type t = typeList.get(0);
                type = t.getType();
                description_edit.setHint(t.getDescribe());
                date_edit.setText(intent.getStringExtra("today"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.date_edit:
                picker.show();
                break;
            case R.id.equal:
                equalNumber();
                break;
            case R.id.money_edit:
                //隐藏输入法
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING); //  不改变布局，隐藏键盘，emojiView弹出
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(money_edit.getApplicationWindowToken(), 0);
                break;
        }
    }

    private void equalNumber() {
        String res = money_edit.getText().toString().trim();
        try {
            Pattern pattern = Pattern.compile("((\\-|\\+)?\\d+(\\.\\d+)?)*");
            Matcher matcher = pattern.matcher(res);
            boolean b = matcher.matches();
            if (b) {
                Float resLast = doRes(res);
                if (resLast <= 0) {
                    shakeX("请填写正确的数据或者表达式");
                } else if (resLast >= bigCostNum) {
                    shakeX("大佬,您钱太多了,一次输出过亿的身家还是请会计吧.");
                } else {
                    //正常返回计算值
                    getRecordData(bigDecimalToString(resLast));
                }
            } else {
                shakeX("请填写正确的数据或者表达式");
            }
        } catch (Exception e) {
            e.printStackTrace();
            shakeX("请填写数据");
        }
    }

    private void shakeX(String msg) {
        showToast(this, msg);
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_x);
        money_edit.startAnimation(shake);
    }

    public void getRecordData(String moneyRes) {
        String date = date_edit.getText().toString().trim();
        String comment = description_edit.getText().toString().trim();
        if (comment.equals("")) {
            comment = description_edit.getHint().toString().trim();
        }
        String tips = tips_text.getText().toString().trim();
        int classes = tips.equals("支出") ? -1 : 0;
        Intent intentBack = new Intent();
        if (flag != 1) {
            Record record = new Record();
            record.setType(type);
            record.setUUID(newUUID());
            record.setCost(moneyRes);
            record.setClasses(String.valueOf(classes));
            record.setComment(comment);
            record.setDate(date);
            record.setTime(getTime());
            record.setStatus("0");
            record.save();
            intentBack.putExtra("status", "ture");
            intentBack.putExtra("date", date);
            //不同的result标志码 和另一个案例进行区分
            setResult(1, intentBack);
        } else {
            ContentValues values = new ContentValues();
            values.put("classes", classes);
            values.put("type", type);
            values.put("cost", moneyRes);
            values.put("comment", comment);
            values.put("date", date);
            values.put("time", getTime());
            values.put("status",status+1);
            if (updateRecordByUUID(values, r.getUUID())) {
                intentBack.putExtra("status", "ture");
                intentBack.putExtra("date", date);
                setResult(2, intentBack);
            } else {
                intentBack.putExtra("status", "false");
                setResult(2, intentBack);
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.ic_clear:
                money_edit.setText("");
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private class GridPagerAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return typeList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder=null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_gvp, null);
                viewHolder=new ViewHolder(convertView,position);
                viewHolder.gvpImage = convertView.findViewById(R.id.gvp_image);
                viewHolder.gvpTxt=convertView.findViewById(R.id.gvp_text);
                convertView.setTag(viewHolder);
            }else {
                viewHolder=(ViewHolder)convertView.getTag();
            }
            Type t = typeList.get(position);
            int gvpImageID = getImageId(AddRecordActivity.this, t.getImage_id());
            //动态设置图标
            viewHolder.gvpImage.setImageResource(gvpImageID);
            //动态设置文字
            viewHolder.gvpTxt.setText(t.getDescribe());
            return convertView;
        }

    }

    private class ViewHolder{
        ImageView gvpImage;
        TextView gvpTxt;

         ViewHolder(final View convertView, final int position){
             convertView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     convertView.setBackgroundResource(R.drawable.list_selected_bg);
                     if (oldView==null){
                         oldView = convertView;
                     }else if (oldView!=convertView) {
                         oldView.setBackground(null);
                         //保存上次状态
                         oldView = convertView;
                     }
                     description_edit.setHint(typeList.get(position).getDescribe());
                     //设置type
                     type =  typeList.get(position).getType();
                 }
             });
        }
    }
}
