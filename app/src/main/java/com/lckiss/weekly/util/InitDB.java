package com.lckiss.weekly.util;

import com.lckiss.weekly.db.Type;

/**
 * Created by root on 17-7-5.
 */

public class InitDB {
    private static String[] types = {"food", "drink", "cloth", "kanbing", "room",
            "shop", "study", "transport", "yule", "yundong", "banknote",
            "wallet", "movie", "yashua", "hobby", "save", "message", "business", "rent"
            , "gift", "mucle", "gas", "phone", "card"};
    private static String[] describes = {"日常饮食", "朋友聚会", "服装衣物", "健康医疗"
            , "房屋住宿", "日常购物", "工作学习", "出行交通", "娱乐休闲", "运动健身", "存款薪水"
            , "日常零用", "电影", "卫生清洁", "个人爱好", "存款", "通讯社交", "商务应酬"
            , "借贷", "礼物人情", "健身", "加油", "数码硬件", "信用卡"};
    private static String[] colors = {"6eb93c", "f97272", "f9ab72", "f16556", "4a85b8", "4ab864", "4cb0aa", "5dae3d",
            "8e5cd8", "3edd54", "5ae3c4", "67a161", "5682f2", "e4b33e", "f04a6a", "4acabe", "414661", "c0741a",
            "44c7bb", "f14f4f", "367438", "c03712", "a11db9", "1f6ba0"};

    public static void initTypeDB() {
        for (int i = 0; i < types.length; i++) {
            Type type = new Type();
            type.setType(String.valueOf(i));
            type.setImage_id(types[i]);
            type.setDescribe(describes[i]);
            type.setColor(colors[i]);
            type.setStatus(null);
            type.save();
        }
    }

    public static String getTypes(int i) {
        return types[i];
    }

    public static String getDescribes(int i) {
        return describes[i];
    }


    public static String getColors(int i) {
        return colors[i];
    }
}
