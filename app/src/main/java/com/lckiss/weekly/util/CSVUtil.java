package com.lckiss.weekly.util;

import android.os.Environment;
import android.util.Log;

import com.lckiss.weekly.db.Record;
import com.lckiss.weekly.db.Type;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.lckiss.weekly.util.DataUtil.deleteAllType;
import static com.lckiss.weekly.util.DataUtil.findTypeByComment;
import static com.lckiss.weekly.util.DataUtil.newUUID;
import static com.lckiss.weekly.util.InitDB.initTypeDB;
import static com.lckiss.weekly.util.TimeUtil.getDate;

/**
 * Created by root on 17-8-18.
 */

public class CSVUtil {
    private static final String TAG = "info";
    private static final int RECORD_CLASSES = 0;
    private static final int RECORD_TYPE = 1;
    private static final int RECORD_COST = 2;
    private static final int RECORD_COMMENT = 3;
    private static final int RECORD_DATE = 4;
    private static final int RECORD_TIME = 5;
    private static final String DEFAULT_STATUS = "0";
    private static final int ERROR = -1;

    /**
     * 导出为备份文件
     *
     * @return true or false
     */
    public static boolean backup(File file, List datas, int dataType) {

        BufferedWriter bufferedWriter = null;
        try {
            //true为是否为追加方式
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            switch (dataType) {
                case 0:
                    List<Record> record = datas;
                    if (record.size() != 0) {
                        for (Record r : record) {
                            bufferedWriter.append(r.toBackupString());
                            bufferedWriter.newLine();
                        }
                    }
                    break;
                case 1:
                    List<Type> type = datas;
                    if (type.size() != 0) {
                        for (Type t : type) {
                            bufferedWriter.append(t.toString());
                            bufferedWriter.newLine();
                        }
                    }
                    break;
            }
            bufferedWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 导入到数据库
     *
     * @param file 文件来源
     * @return 错误或者导入条数
     */
    public static boolean restore(String file) {
        File csv = new File(file);
        if (!csv.exists()) {
            Log.d(TAG, "CSVUtil restore: file not found");
            return false;
        }
        //清空类目
        deleteAllType();
        //导入开始
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(csv));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                String[] datas = line.split(",");
                switch (datas.length) {
                    case 6:
                        Record record = new Record();
                        record.setClasses(datas[RECORD_CLASSES]);
                        record.setType(datas[RECORD_TYPE]);
                        record.setCost(datas[RECORD_COST]);
                        record.setComment(datas[RECORD_COMMENT]);
                        record.setDate(datas[RECORD_DATE]);
                        record.setTime(datas[RECORD_TIME]);
                        record.setStatus(DEFAULT_STATUS);
                        record.setUUID(newUUID());
                        record.save();
                        break;
                    case 5:
                        Type type = new Type();
                        type.setType(datas[RECORD_CLASSES]);
                        type.setImage_id(datas[RECORD_TYPE]);
                        type.setDescribe(datas[RECORD_COST]);
                        type.setColor(datas[RECORD_COMMENT]);
                        type.setStatus(datas[RECORD_DATE]);
                        type.save();
                        break;
                    default:
                        return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //清空类目
            deleteAllType();
            //重新初始化
            initTypeDB();
            return false;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    /**
     * 导出为CSV文件
     *
     * @param record 对象集合
     * @param msg    文件名
     * @return true or false
     */
    public static boolean exportCSV(List<Record> record, String msg) {
        File file = new File(Environment.getExternalStorageDirectory() + "/Weekly/exportData/" + getDate() + msg + ".csv");
        if (!file.exists()) {
            boolean mkdir = file.getParentFile().mkdirs();
            Log.d(TAG, "CSVUtil exportCSV mkdir: " + mkdir);
        } else {
            boolean delete = file.delete();
            Log.d(TAG, "CSVUtil exportCSV delete: " + delete);
        }
        BufferedWriter bufferedWriter = null;
        try {
            //true为是否为追加方式
            bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.append("收支,类别,金额,备注,日期,时间");
            bufferedWriter.newLine();
            for (Record r : record) {
                bufferedWriter.append(r.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 导入到数据库
     *
     * @param file 文件来源
     * @return 错误或者导入条数
     */
    public static int importCSV(String file) {
        File csv = new File(file);
        if (!csv.exists()) {
            Log.d(TAG, "CSVUtil importCSV: file not found");
            return ERROR;
        }
        int count = -1;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(csv));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                String[] datas = line.split(",");
                if (datas.length != 6) {
                    return ERROR;
                }
                if (count >= 0) {
                    Record record = new Record();
                    String cls = datas[RECORD_CLASSES].equals("收入") ? "0" : "-1";
                    record.setClasses(cls);
                    String type = findTypeByComment(datas[RECORD_TYPE]).getType();
                    record.setType(type);
                    record.setCost(datas[RECORD_COST]);
                    record.setComment(datas[RECORD_COMMENT]);
                    record.setDate(datas[RECORD_DATE]);
                    record.setTime(datas[RECORD_TIME]);
                    record.setStatus(DEFAULT_STATUS);
                    record.setUUID(newUUID());
                    record.save();
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }
}
