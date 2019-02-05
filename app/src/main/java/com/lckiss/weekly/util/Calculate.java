package com.lckiss.weekly.util;

import android.util.Log;

import com.lckiss.weekly.db.Record;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.lckiss.weekly.util.Constants.ALL;
import static com.lckiss.weekly.util.Constants.PLUS;
import static com.lckiss.weekly.util.Constants.WEEKSUM;
import static com.lckiss.weekly.util.DataUtil.findMonthCostList;
import static com.lckiss.weekly.util.DataUtil.findMonthSaveList;
import static com.lckiss.weekly.util.DataUtil.findWeekCostLists;
import static com.lckiss.weekly.util.DataUtil.findWeekRecordLists;
import static com.lckiss.weekly.util.DataUtil.findWeekSaveLists;
import static com.lckiss.weekly.util.DataUtil.findYearCostList;
import static com.lckiss.weekly.util.DataUtil.findYearSaveList;

/**
 * Created by root on 17-7-6.
 * 随便算,爱咋咋的
 */

public class Calculate {

    /**
     * 表达式求值
     *
     * @param expression
     * @return
     */
    public static Float doRes(String expression) {
        char[] array = expression.toCharArray();
        Float result = 0F;
        String num = "0";
        char op = '+';
        for (char ch : array) {
            if (ch == '+' || ch == '-') {
                result += op == '+' ? Float.parseFloat(num) : -Float.parseFloat(num);
                op = ch;
                num = "";
            } else {
                num += ch;
            }
        }
        result += op == '+' ? Float.parseFloat(num) : -Float.parseFloat(num);
        return result;
    }


    public static String bigDecimalToString(Float res) {
        return new BigDecimal(res).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    public static Float bigDecimal(Float res) {
        return Float.valueOf(new BigDecimal(res).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
    }


    /**
     * 传入一个消费列表,计算总收支
     *
     * @param list
     * @return
     */
    public static Float getRes(List<Record> list, int flag) {
        Float res = 0F;
        switch (flag) {
            case ALL:
                for (Record r : list) {
                    res += "0".equals(r.getClasses()) ? Float.parseFloat(r.getCost()) : -Float.parseFloat(r.getCost());
                }
                break;
            case PLUS:
                for (Record r : list) {
                    res +=Float.parseFloat(r.getCost());
                }
                break;
        }
        return bigDecimal(res);
    }

    /**
     * @param weekList
     * @return 一周的每天消费列表
     */
    public static ArrayList<Float> getWeekMoneyList(List<String> weekList) {
        List<List<Record>> rList= findWeekRecordLists(weekList);
        ArrayList<Float> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.add(getRes(rList.get(i), ALL));
        }
        return list;
    }

    /**
     *
     * @param weekList
     * @return 一周的支出金额列表
     */
    public static ArrayList<Float> getWeekCostList(List<String> weekList) {
        List<List<Record>> rList= findWeekCostLists(weekList);
        ArrayList<Float> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.add(getRes(rList.get(i), PLUS));
        }
        return list;
    }

    /**
     *
     * @param weekList
     * @return 一周的收入金额列表
     */
    public static ArrayList<Float> getWeekSaveList(List<String> weekList) {
        List<List<Record>> rList= findWeekSaveLists(weekList);
        ArrayList<Float> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.add(getRes(rList.get(i), PLUS));
        }
        return list;
    }


    /**
     * @param month
     * @return
     */
    public static Float getMonthCost(String month) {
        List<Record> l=findMonthCostList(month);
        return getRes(l,PLUS);
    }


    /**
     * @param month
     * @return
     */
    public static Float getMonthSave(String month) {
        List<Record> l=findMonthSaveList(month);
        return getRes(l,PLUS);
    }

    /**
     * @param year
     * @return
     */
    public static Float getYearCost(String year) {
        List<Record> l=findYearCostList(year);
        return getRes(l,PLUS);
    }

    /**
     * @param year
     * @return
     */
    public static Float getYearSave(String year) {
        List<Record> l=findYearSaveList(year);
        return getRes(l,PLUS);
    }

}
