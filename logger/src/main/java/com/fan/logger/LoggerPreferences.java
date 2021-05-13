package com.fan.logger;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * logger本地文件设置
 *
 * @author fan
 */
public class LoggerPreferences {

    private static final String FILE_NAME = "logger.pref";
    private static final String CRASH = "crash";
    private static final String CRASH_AUTO_CLEAR = "crash_auto_clear";
    private static final String LAST_CRASH_TIME = "last_crash_time";
    private static final String CRASH_EXIT_PASSWORD = "crash_exit_password";

    /**
     * 默认自动清理天数.
     */
    private static final int DEFAULT_CLEAR_DAYS = 7;

    private final SharedPreferences sp;

    public LoggerPreferences(Context context) {
        sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 是否崩溃过
     * 崩溃后会app将其值改成true
     * 上传掉崩溃日志后改成false
     */
    public void setCrash(boolean isCrash) {
        sp.edit().putBoolean(CRASH, isCrash).apply();
    }

    /**
     * 用于判断是否需要上传崩溃日志.
     */
    public boolean hasCrash() {
        return sp.getBoolean(CRASH, false);
    }

    /**
     * 设置crash文件自动清理天数.
     */
    public void setCrashAutoClearDays(int days) {
        sp.edit().putInt(CRASH_AUTO_CLEAR, days).apply();
    }

    /**
     * 获得crash自动清理天数.
     */
    public int getCrashAutoClearDays() {
        return sp.getInt(CRASH_AUTO_CLEAR, DEFAULT_CLEAR_DAYS);
    }

    /**
     * 保存最近崩溃时间(long值).
     */
    public void setLastCrashTime(long date) {
        sp.edit().putLong(LAST_CRASH_TIME, date).apply();
    }

    /**
     * 获得最近崩溃时间.
     */
    public long getLastCrashTime() {
        return sp.getLong(LAST_CRASH_TIME, 0);
    }
}
