package com.fan.logger.crash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.widget.Toast;

import com.fan.logger.LoggerPreferences;
import com.fan.logger.R;
import com.fan.logger.Utility;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

/**
 * 用于监听未捕获的异常消息
 *
 * @author fan
 */
public class CrashWatchDog {

    private static final String EXTRA_RESTART_ACTIVITY_CLASS = "CrashWatchDog.EXTRA_RESTART_ACTIVITY_CLASS";
    private static final String EXTRA_STACK_TRACE_MESSAGE = "CrashWatchDog.EXTRA_STACK_TRACE_MESSAGE";

    private final Application application;
    /**
     * 自定义重启的Activity
     */
    private Class<? extends Activity> mRestartActivity;
    /**
     * 自定义ERROR Activity 默认 CrashErrorActivity
     */
    private Class<? extends Activity> mErrorActivity;
    /**
     * 自定义日志文件夹名
     */
    private static String tagName;
    private Callback callback;
    /**
     * 是否保存Crash日志到本地开关
     */
    private boolean writeCrashLog = true;

    public CrashWatchDog(Application application) {
        this.application = application;
    }

    /**
     * 监听crash异常.
     */
    public void watch() {
        watch(null);
    }

    /**
     * 监听crash异常.
     *
     * @param callback 回调，用来补充日志信息
     */
    public void watch(Callback callback) {
        this.callback = callback;
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
    }

    public interface Callback {
        /**
         * 发生异常时，添加其他设备信息(如唯一码)
         */
        void appendContent(Map<String, String> content);
    }

    /**
     * 处理未捕获的异常.
     */
    private class CrashHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable ex) {
            try {
                // release版提示Toast
                if (!Utility.isDebuggable(application)) {
                    // 使用Toast来显示异常信息
                    new Thread() {

                        @Override
                        public void run() {
                            Looper.prepare();
                            Toast.makeText(application, R.string.alert_crash_occurred,
                                    Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }.start();

                    Thread.sleep(1000);
                }
                // 记录日志
                if (Utility.checkOperateSDCardPermission(application) && writeCrashLog) {
                    writeCrashLog(ex);
                }
                // 跳转到Crash界面，此界面在另一个进程中
                navigateToCrashView(ex);
                killCurrentProcess();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void navigateToCrashView(Throwable ex) {
            Intent intent = new Intent(application, getErrorActivity());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(EXTRA_RESTART_ACTIVITY_CLASS, getRestartActivity());
            intent.putExtra(EXTRA_STACK_TRACE_MESSAGE, buildExceptionData(ex));
            application.startActivity(intent);
        }
    }

    private ExceptionData buildExceptionData(Throwable e) {
        ExceptionData data = new ExceptionData();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        data.stackTrace = sw.toString();
        data.cause = e.getMessage();

        Throwable rootTr = e;
        while (e.getCause() != null) {
            e = e.getCause();
            if (e.getStackTrace().length > 0) {
                rootTr = e;
            }
            String msg = e.getMessage();
            if (!TextUtils.isEmpty(msg)) {
                data.cause = msg;
            }
        }

        data.type = rootTr.getClass().getName();

        if (rootTr.getStackTrace().length > 0) {
            StackTraceElement trace = rootTr.getStackTrace()[0];
            data.className = trace.getClassName();
            data.methodName = trace.getMethodName();
            data.lineNumber = trace.getLineNumber();
        } else {
            data.className = "unknown";
            data.methodName = "unknown";
            data.lineNumber = 0;
        }
        return data;
    }

    /**
     * 记录crash日志.
     */
    private void writeCrashLog(Throwable ex) throws Exception {
        // 收集设备参数信息
        Map<String, String> content = Utility.collectDeviceInfo(application);
        if (callback != null)
            callback.appendContent(content);

        String dir = Utility.getCrashGlobalPath();
        if (tagName != null)
            dir += tagName;

        // 保存日志文件
        Utility.saveCrashInfoFile(dir, ex, content);
    }

    public CrashWatchDog setTagName(@NonNull String name) {
        tagName = name;
        return this;
    }

    @NonNull
    public String getTagName() {
        return tagName;
    }

    public boolean isWriteCrashLog() {
        return writeCrashLog;
    }

    public CrashWatchDog setWriteCrashLog(boolean writeCrashLog) {
        this.writeCrashLog = writeCrashLog;
        return this;
    }

    /**
     * 自动清理crash日志.
     */
    public CrashWatchDog autoClear(final Context context) {
        Utility.delete(getLogPath(), new FilenameFilter() {

            @Override
            public boolean accept(File file, String filename) {
                String s = Utility.getFileNameWithoutExtension(filename);
                int autoClearDay = new LoggerPreferences(context).getCrashAutoClearDays();
                int day = autoClearDay < 0 ? autoClearDay : -1 * autoClearDay;
                String date = "crash-" + Utility.dateFormat(Utility.addDays(new Date(), day));
                return date.compareTo(s) >= 0;
            }
        });
        return this;
    }

    /**
     * 获取crash文件根目录.
     */
    @NonNull
    public static String getLogPath() {
        return TextUtils.isEmpty(tagName) ? Utility.getCrashGlobalPath()
                : Utility.getCrashGlobalPath() + tagName + File.separator;
    }

    /**
     * 设置错误提醒的activity
     */
    public CrashWatchDog setErrorActivity(Class<? extends Activity> errorActivity) {
        this.mErrorActivity = errorActivity;
        return this;
    }

    public Class<? extends Activity> getErrorActivity() {
        return mErrorActivity == null ? CrashErrorActivity.class : mErrorActivity;
    }

    public CrashWatchDog setRestartActivity(Class<? extends Activity> restartActivity) {
        this.mRestartActivity = restartActivity;
        return this;
    }

    public Class<? extends Activity> getRestartActivity() {
        if (mRestartActivity != null) {
            return mRestartActivity;
        }
        return getLauncherActivity(application);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getLauncherActivity(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            try {
                return (Class<? extends Activity>) Class.forName(intent.getComponent().getClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Activity> getRestartActivityFromIntent(Intent intent) {
        Serializable serializedClass = intent.getSerializableExtra(EXTRA_RESTART_ACTIVITY_CLASS);

        if (serializedClass instanceof Class) {
            return (Class<? extends Activity>) serializedClass;
        } else {
            return null;
        }
    }

    public static ExceptionData getStackTraceFromIntent(Intent intent) {
        return (ExceptionData) intent.getSerializableExtra(EXTRA_STACK_TRACE_MESSAGE);
    }

    static class ExceptionData implements Serializable {
        String type;
        String className;
        String methodName;
        int lineNumber;
        String cause;
        String stackTrace;
    }

}

