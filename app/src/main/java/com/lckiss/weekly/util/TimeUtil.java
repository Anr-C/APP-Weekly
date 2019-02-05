package com.lckiss.weekly.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by root on 17-7-7.
 * HH:mm:ss 随意
 * 星期日为一周的第一天	SUN	MON	TUE	WED	THU	FRI	SAT
 * DAY_OF_WEEK返回值	1	2	3	4	5	6	7
 * 星期一为一周的第一天	MON	TUE	WED	THU	FRI	SAT	SUN
 * DAY_OF_WEEK返回值	1	2	3	4	5	6	7
 * 所以Calendar.DAY_OF_WEEK需要根据本地化设置的不同而确定是否需要 “-1”
 */

public class TimeUtil {

    private static long M24HOURMS = 86400000;
    private static Calendar calendar = Calendar.getInstance();
    private static Calendar modCalendar = Calendar.getInstance();

    /**
     * 时间
     *
     * @return
     */
    public static String getTime() {
        DateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(new Date());
    }

    /**
     * 日期
     * @return yyyy-mm-dd
     */
    public static String getDate() {
        int year = calendar.get(Calendar.YEAR);
        int monthInt = calendar.get(Calendar.MONTH) + 1;
        int dayInt = calendar.get(Calendar.DAY_OF_MONTH);
        String month = monthInt < 10 ? "0" + monthInt : monthInt + "";
        String day = dayInt < 10 ? "0" + dayInt : dayInt + "";
        return year + "-" + month + "-" + day;
    }

    /**
     * 年
     *
     * @return yyyy
     */
    public static int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 月
     *
     * @return M
     */
    public static int getMonth() {
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 一月中的某天
     * @return dd
     */
    public static int getDayOfMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 月
     *
     * @return MM
     */
    public static String getMonthByDay(String day) {
        return day.substring(5, 7);
    }

    /**
     * 年
     * @return yyyy
     */
    public static String getYearByDay(String day) {
        return day.substring(0, 4);
    }


    /**
     * 转化日期为数字星期
     *
     * @return d
     */
    public static int getDayOfWeek() {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        day = day <= 1 ? 5 + day : day - 2;
        return day;
    }


    /**
     * 查出上一周的倍数的每天的日期
     *
     * @param multiple 当前周倍数
     * @return list<date>
     */
    public static List<String> getPreWeekLists(int multiple) {
        //初始化条件
        String formatSrt = "yyyy-MM-dd";
        String date = getDate();
//        Log.i("----------", "getPreWeekList: " + date);
        long dateMill = 0;
        try {
            // 获取date的毫秒值
            dateMill = getMillis(date, formatSrt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        modCalendar.setTimeInMillis(dateMill);
        // 本周的第几天
        int weekNumber = modCalendar.get(Calendar.DAY_OF_WEEK);
        weekNumber = weekNumber == 1 ? 7 : weekNumber - 1;
//        Log.e("本周第几天", weekNumber + "");
        // 获取n周一的毫秒值
        long mondayMill = dateMill - M24HOURMS * (weekNumber - 1) - M24HOURMS * 7 * multiple;

        //直接转化为yyyy-MM-dd日期的list
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            list.add(formatDate(mondayMill + M24HOURMS * i, "yyyy-MM-dd"));
        }
        return list;
    }

    /**
     * 根据传入的日期查出这一周的每天的日期
     * @param date 传入的日期
     * @return list<date>
     */
    public static ArrayList<String> getWeekLists(String date) {
        String formatSrt = "yyyy-MM-dd";
        long dateMill = 0;
        try {
            // 获取date的毫秒值
            dateMill = getMillis(date, formatSrt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Calendar
        modCalendar.setTimeInMillis(dateMill);
        // 本周的第几天
        int weekNumber = modCalendar.get(Calendar.DAY_OF_WEEK);
        //修正中国区时间
        weekNumber = weekNumber == 1 ? 7 : weekNumber - 1;
//        Log.e("本周第几天", weekNumber + "");
        // 获取本周一的毫秒值
        long mondayMill = dateMill - M24HOURMS * (weekNumber - 1);

        //直接转化为yyyy-MM-dd日期的list
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            list.add(formatDate(mondayMill + M24HOURMS * i, "yyyy-MM-dd"));
        }
        return list;
    }
    /**
     * 根据传入的日期查出在这一周中的位置
     * @param date 传入的日期
     * @return int
     */
    public static int getWeekNumber(String date) {
        String formatSrt = "yyyy-MM-dd";
        long dateMill = 0;
        try {
            // 获取date的毫秒值
            dateMill = getMillis(date, formatSrt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Calendar
        modCalendar.setTimeInMillis(dateMill);
        // 本周的第几天
        int weekNumber = modCalendar.get(Calendar.DAY_OF_WEEK);
        //修正中国区时间
        weekNumber = weekNumber == 1 ? 7 : weekNumber - 1;
//        Log.e("本周第几天", weekNumber + "");
        return weekNumber;
    }

    /**
     * 把格式化过的时间转换毫秒值
     *
     * @param time      时间
     * @param formatSrt 时间格式 如 yyyy-MM-dd
     * @return 当前日期的毫秒值
     */
    private static long getMillis(String time, String formatSrt) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat(formatSrt);
        return format.parse(time).getTime();
    }

    /**
     * 将毫秒值格转换为时间 yyyy-MM-dd HH:mm:ss 格式
     *
     * @param date
     * @param format 你要的时间格式 yyyy-MM-dd HH:mm:ss或者yyyy-MM-dd
     * @return 返回转换后的值
     */
    private static String formatDate(Long date, String format) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static boolean compareDate(String start,String end){
        //如果想比较日期则写成"yyyy-MM-dd"就可以了
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        //将字符串形式的时间转化为Date类型的时间
        Date a= null;
        Date b= null;
        try {
            a = format.parse(start);
            b = format.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Date类的一个方法，如果a早于b返回true，否则返回false
        assert a != null;
        return a.before(b);
    }
}
