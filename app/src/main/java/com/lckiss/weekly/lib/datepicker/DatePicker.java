package com.lckiss.weekly.lib.datepicker;

import android.app.Activity;

/**
 * 日期选择器
 *
 */
public class DatePicker extends DateRangePicker {

    public DatePicker(Activity activity) {
        this(activity, YEAR_MONTH_DAY);
    }

    /**
     * @see #YEAR_MONTH_DAY
     * @see #YEAR_MONTH
     * @see #MONTH_DAY
     */
    public DatePicker(Activity activity, @DateMode int mode) {
        super(activity, mode,false);
    }

    /**
     * 设置年月日的单位
     */
    public void setLabel(String yearLabel, String monthLabel, String dayLabel) {
        super.setLabel(yearLabel, monthLabel, dayLabel, "", "");
    }

    /**
     * 设置范围：开始的年月日
     */
    public void setRangeStart(int startYear, int startMonth, int startDay) {
        super.setDateRangeStart(startYear, startMonth, startDay);
    }

    /**
     * 设置范围：结束的年月日
     */
    public void setRangeEnd(int endYear, int endMonth, int endDay) {
        super.setDateRangeEnd(endYear, endMonth, endDay);
    }

    /**
     * 设置范围：开始的年月日
     */
    public void setRangeStart(int startYearOrMonth, int startMonthOrDay) {
        super.setDateRangeStart(startYearOrMonth, startMonthOrDay);
    }

    /**
     * 设置范围：结束的年月日
     */
    public void setRangeEnd(int endYearOrMonth, int endMonthOrDay) {
        super.setDateRangeEnd(endYearOrMonth, endMonthOrDay);
    }

    /**
     * 设置默认选中的年月日
     */
    public void setSelectedItem(int year, int month, int day) {
        super.setSelectedItem(year, month, day);
    }

    /**
     * 设置默认选中的年月或者月日
     */
    public void setSelectedItem(int yearOrMonth, int monthOrDay) {
        super.setSelectedItem(yearOrMonth, monthOrDay);
    }


    public void setOnDatePickListener(final OnDatePickListener listener) {
        if (null == listener) {
            return;
        }
        if (listener instanceof OnYearMonthDayPickListener) {
            super.setOnDatePickListener(new OnYearMonthDayRangePickListener() {
                @Override
                public void onDatePicked(String year, String month, String day) {
                    ((OnYearMonthDayPickListener) listener).onDatePicked(year, month, day);
                }
            });
        } else if (listener instanceof OnYearMonthPickListener) {
            super.setOnDatePickListener(new OnYearMonthRangePickListener() {
                @Override
                public void onDatePicked(String year, String month) {
                    ((OnYearMonthPickListener) listener).onDatePicked(year, month);
                }
            });
        } else if (listener instanceof OnMonthDayPickListener) {
            super.setOnDatePickListener(new OnMonthDayRangePickListener() {
                @Override
                public void onDatePicked(String month, String day) {
                    ((OnMonthDayPickListener) listener).onDatePicked(month, day);
                }
            });
        }
    }

    protected interface OnDatePickListener {

    }

    public interface OnYearMonthDayPickListener extends OnDatePickListener {

        void onDatePicked(String year, String month, String day);

    }

    public interface OnYearMonthPickListener extends OnDatePickListener {

        void onDatePicked(String year, String month);

    }

    public interface OnMonthDayPickListener extends OnDatePickListener {

        void onDatePicked(String month, String day);

    }


}
