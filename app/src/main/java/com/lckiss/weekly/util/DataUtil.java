package com.lckiss.weekly.util;

import android.content.ContentValues;
import android.util.Log;

import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.db.Type;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static com.lckiss.weekly.util.Constants.WEEKSUM;

/**
 * Created by root on 17-7-7.
 * 数据库的操作,该干嘛的干嘛
 */

public class DataUtil {

    /**
     * 查找所有的type类型
     *
     * @return 所有的类型列表
     */
    public static List<Type> findAllType() {
        return DataSupport.findAll(Type.class);
    }

    /**
     * 查找所有的Record
     *
     * @return 所有的Record
     */
    public static List<Record> findAllRecord() {
        return DataSupport.findAll(Record.class);
    }

    /**
     * 找出最大的type值，在此基础上+1作为新的type值，避免与之前的重复
     *
     * @return
     */
    public static int maxType() {
        return Integer.parseInt(DataSupport.findAll(Type.class).get(0).getType());
    }

    /**
     * 清空并重新插入type类型
     */
    public static void updateAllType(List<Type> types) {
        DataSupport.deleteAll(Type.class);
        for (int i = 0; i < types.size(); i++) {
            Type type = new Type();
            type.setId(i);
            type.setType(types.get(i).getType());
            type.setImage_id(types.get(i).getImage_id());
            type.setDescribe(types.get(i).getDescribe());
            type.setColor(types.get(i).getColor());
            type.setStatus(types.get(i).getStatus());
            type.save();
        }
    }

    /**
     * @param type 类型
     * @return 结果
     */
    public static boolean deleteTypeByType(String type) {
        int deleteCount = DataSupport.deleteAll(Type.class, "type = ?", type);
        return deleteCount > 0;
    }

    /**
     * @param id 类型id
     * @return 结果
     */
    public static boolean updateTypeByID(ContentValues values, int id) {
        int updateCount = DataSupport.updateAll(Type.class, values, "id = ?", String.valueOf(id));
        return updateCount > 0;
    }

    /**
     * @param type type
     * @return type
     */
    public static Type findType(String type) {
        Type res = new Type();
        try {
            res = DataSupport.where("type=?", type).find(Type.class).get(0);
        } catch (Exception e) {
            Log.d("info", "DataUtil...findType: " + e);
            res.setImage_id("undifine");
            res.setDescribe("未定义");
            res.setColor("FF9DAAB4");
        }

        return res;
    }
    /**
     * @param describe describe
     * @return type
     */
    public static Type findTypeByComment(String describe) {
        Type res = new Type();
        try {
            res = DataSupport.where("describe=?", describe).find(Type.class).get(0);
        } catch (Exception e) {
            Log.d("info", "DataUtil...findType: " + e);
            res.setImage_id("undifine");
            res.setDescribe("未定义");
            res.setColor("FF9DAAB4");
        }

        return res;
    }

    /**
     * @return 类目总数
     */
    public static int typeCount() {
        return DataSupport.count(Type.class);
    }


    /**
     * @param date 日期
     * @return 消费列表
     */
    public static List<Record> findRecordByDay(String date) {
        return DataSupport.where("date=?", date).order("time desc").find(Record.class);
    }

    /**
     * @param weekList 周列表
     * @return 一个包括每天的消费列表的列表
     */
    public static List<List<Record>> findWeekRecordLists(List<String> weekList) {
        List<List<Record>> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.add(DataSupport.where("date=?", weekList.get(i)).order("time desc").find(Record.class));
        }
        return list;
    }

    /**
     * @param weekList 周列表
     * @return 一个包括每天的支出列表的列表
     */
    public static List<List<Record>> findWeekCostLists(List<String> weekList) {
        List<List<Record>> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.add(DataSupport.where("date=? and classes=-1", weekList.get(i)).order("time desc").find(Record.class));
        }
        return list;
    }

    /**
     * @param weekList 周列表
     * @return 一个包括每天的支出列表的列表
     */
    public static List<List<Record>> findWeekSaveLists(List<String> weekList) {
        List<List<Record>> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.add(DataSupport.where("date=? and classes=0", weekList.get(i)).order("time desc").find(Record.class));
        }
        return list;
    }

    /**
     * @param weekList 周列表
     * @return 一个包括每天的消费金额的列表
     */
    public static List<Record> findWeekRecoedList(List<String> weekList) {
        List<Record> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.addAll(DataSupport.where("date=?", weekList.get(i)).order("time desc").find(Record.class));
        }
        return list;
    }

    /**
     * @param weekList 周列表
     * @return 一个包括每天的支出金额的列表
     */
    public static List<Record> findWeekCostList(List<String> weekList) {
        List<Record> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.addAll(DataSupport.where("date=? and classes=-1", weekList.get(i)).order("time desc").find(Record.class));
        }
        return list;
    }

    /**
     * @param weekList 周列表
     * @return 一个包括每天的收入列表的列表
     */
    public static List<Record> findWeekSaveList(List<String> weekList) {
        List<Record> list = new ArrayList<>();
        for (int i = 0; i < WEEKSUM; i++) {
            list.addAll(DataSupport.where("date=? and classes=0", weekList.get(i)).order("time desc").find(Record.class));
        }
        return list;
    }

    /**
     * 起始位置为1,不是0
     * substr(字段,起始位置,长度)
     *
     * @param month 月份
     * @return 该月的消费列表
     */
    public static List<Record> findMonthRecordList(String month) {
        return DataSupport.where("substr(date,6,2)=?", month).order("date desc").find(Record.class);
    }

    /**
     * 起始位置为1,不是0
     * substr(字段,起始位置,长度)
     *
     * @param month 月份
     * @return 该月的支出列表
     */
    public static List<Record> findMonthCostList(String month) {
        return DataSupport.where("substr(date,6,2)=? and classes=-1", month).order("date desc").find(Record.class);
    }

    /**
     * 起始位置为1,不是0
     * substr(字段,起始位置,长度)
     *
     * @param month 月份
     * @return 该月的收入列表
     */
    public static List<Record> findMonthSaveList(String month) {
        return DataSupport.where("substr(date,6,2)=?  and classes=0", month).order("date desc").find(Record.class);
    }

    /**
     * 起始位置为1,不是0
     * substr(字段,起始位置,长度)
     *
     * @param year 年份
     * @return 该年的消费列表
     */
    public static List<Record> findYearRecordList(String year) {
        return DataSupport.where("substr(date,1,4)=?", year).order("date desc").find(Record.class);
    }

    /**
     * 起始位置为1,不是0
     * substr(字段,起始位置,长度)
     *
     * @param year 年份
     * @return 该年的支出列表
     */
    public static List<Record> findYearCostList(String year) {
        return DataSupport.where("substr(date,1,4)=? and classes=-1", year).order("date desc").find(Record.class);
    }

    /**
     * 起始位置为1,不是0
     * substr(字段,起始位置,长度)
     *
     * @param year 年份
     * @return 该年的收入列表
     */
    public static List<Record> findYearSaveList(String year) {
        return DataSupport.where("substr(date,1,4)=? and classes=0", year).order("date desc").find(Record.class);
    }

    /**
     * 根据唯一的UUID删除条目
     *
     * @param uuid uuid
     * @return 结果
     */
    public static boolean deleteRecordByUUID(String uuid) {
        int deleteCount = DataSupport.deleteAll(Record.class, "uuid = ?", uuid);
        return deleteCount > 0;
    }

    /**
     * 根据唯一的UUID更新条目
     *
     * @param uuid uuid
     * @return 结果
     */
    public static boolean updateRecordByUUID(ContentValues values, String uuid) {
        int updateCount = DataSupport.updateAll(Record.class, values, "uuid = ?", uuid);
        return updateCount > 0;
    }

    /**
     * 生成唯一的UUID
     *
     * @return uuid
     */
    public static String newUUID() {
        return java.util.UUID.randomUUID().toString();
    }


    /**
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间范围内的消费数据
     */
    public static List<Record> findRecordAround(String startTime, String endTime) {
        return DataSupport.where("date>='" + startTime + "' and date<='" + endTime + "'").order("date desc").find(Record.class);
    }

    /**
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间范围内的支出数据
     */
    public static List<Record> findCostAround(String startTime, String endTime) {
        return DataSupport.where("date>='" + startTime + "' and date<='" + endTime + "' and classes=-1").order("date desc").find(Record.class);
    }

    /**
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间范围内的收入数据
     */
    public static List<Record> findSaveAround(String startTime, String endTime) {
        return DataSupport.where("date>='" + startTime + "' and date<='" + endTime + "' and classes=0").order("date desc").find(Record.class);
    }

    /**
     * 删除所有消费数据
     */
    public static void deleteAllRecord() {
        DataSupport.deleteAll(Record.class);
    }
    /**
     * 重置所有分类数据
     */
    public static void deleteAllType() {
        DataSupport.deleteAll(Type.class);
    }


}
