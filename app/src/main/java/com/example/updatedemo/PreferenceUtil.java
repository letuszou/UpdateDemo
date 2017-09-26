package com.example.updatedemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class PreferenceUtil {

    /**
     * 获取本应用的SharePreferences
     *
     * @param mContext
     * @return
     */
    public static SharedPreferences getPreferences(Context mContext) {
        String preferenceName = "letuszou@126.com";
        return mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    /**
     * 设置float类型的值
     *
     * @param key      键
     * @param value    值
     * @param mContext
     */
    public static void setLongValue(String key, long value, Context mContext) {

        SharedPreferences preferences = getPreferences(mContext);
        Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * 获取int类型的值
     *
     * @param key      键
     * @param value    默认值
     * @param mContext
     */
    public static long getLongValue(String key, long value, Context mContext) {
        SharedPreferences preferences = getPreferences(mContext);
        return preferences.getLong(key, value);
    }


    /**
     * 设置String类型的值
     *
     * @param key      键
     * @param value    值
     * @param mContext
     */
    public static void setStringValue(String key, String value, Context mContext) {
        SharedPreferences preferences = getPreferences(mContext);
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 获取String类型的值
     *
     * @param key      键
     * @param value    默认值
     * @param mContext
     */
    public static String getStringValue(String key, String value, Context mContext) {
        SharedPreferences preferences = getPreferences(mContext);
        return preferences.getString(key, value);
    }


}
