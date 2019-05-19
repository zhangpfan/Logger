package com.wangyy.logger;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 日志管理类
 * <br>
 * 管理日志读写
 *
 * @author zhangpfan.
 */
public final class LogManager {

    /**
     * StackTrace的偏移量，主要用于定位日志调用的信息.
     */
    private static final int STACK_OFFSET = 4;
    private static volatile LogManager instance;

    private LogConfig config;
    private LogFormat format;

    public static LogManager getLogger() {
        if (instance == null) {
            synchronized (LogManager.class) {
                if (instance == null)
                    instance = new LogManager();
            }
        }
        return instance;
    }

    public void initLogConfig(LogConfig config) {
        this.config = config;
        this.format = config.getLogFormat();
    }

    /**
     * 具体的打印日志方法
     *
     * @param level   日志级别
     * @param tag     tag
     * @param message 具体的文本消息
     * @param error   异常信息
     */
    public void log(Level level, String tag, String message, Throwable error) {
        LogInfo data = new LogInfo();
        data.setLevel(level);
        data.setTag(TextUtils.isEmpty(tag) ? config.getTag() : tag);
        data.setMessage(message);
        data.setThrowableInfo(getStackTraceString(error, message));
        printByLevel(level, data);
    }

    private String getStackTraceString(@Nullable Throwable error, @Nullable String msg) {
        if (TextUtils.isEmpty(msg)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        if (error == null) {
            Throwable tr = new Throwable(msg);
            StackTraceElement element = tr.getStackTrace()[STACK_OFFSET];
            sb.append("  \tat ");
            sb.append(element.getClassName());
            sb.append(".");
            sb.append(element.getMethodName());
            sb.append("(");
            sb.append(element.getFileName());
            sb.append(":");
            sb.append(element.getLineNumber());
            sb.append(")");
        } else {
            String trace = "";
            StringWriter sw = new StringWriter();
            try {
                PrintWriter pw = new PrintWriter(sw);
                error.printStackTrace(pw);
                trace = sw.getBuffer().toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    sw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return trace;
        }
        return sb.toString();
    }

    private void printByLevel(Level level, LogInfo data) {
        switch (level) {
            case INFO:
                Log.i(data.getTag(), format.formatConsole(data));
                break;
            case DEBUG:
                Log.d(data.getTag(), format.formatConsole(data));
                if (config.isDebug()) {
                    write(format.formatOutput(data));
                }
                break;
            case ERROR:
                Log.e(data.getTag(), format.formatConsole(data));
                write(format.formatOutput(data));
                break;
            default:
                Log.d(data.getTag(), format.formatConsole(data));
                break;
        }
    }

    public final synchronized void write(final String content) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(config.getLogFileDirectory());
            sb.append(File.separator);
            sb.append(Utility.dateFormat());
            sb.append(".txt");
            String file = sb.toString();

            Utility.writeFileByNio(file, content, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final synchronized String read(String path) {
        try {
            return Utility.readFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public final String getLogDirectory() {
        return config.getLogFileDirectory();
    }

    public final List<String> getFileList() {
        return getFileList(config.getLogFileDirectory());
    }

    /**
     * 文件名倒叙排列
     */
    public List<String> getFileList(String dir) {
        List<String> fileList = Utility.getFileNameList(dir, "txt", "log", "md");
        Collections.sort(fileList);
        Collections.reverse(fileList);
        return fileList;
    }

    public final boolean delete(String fileName) {
        return Utility.deleteFile(config.getLogFileDirectory() + File.separator + fileName);
    }

    public final boolean deleteDir() {
        return Utility.deleteFile(config.getLogFileDirectory());
    }

    /**
     * 自动清理日志.
     */
    public final void autoClear() {
        final int autoClearDay = config.getAutoClearDays();
        Utility.delete(config.getLogFileDirectory(), new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                String s = Utility.getFileNameWithoutExtension(filename);
                int day = autoClearDay < 0 ? autoClearDay : -1 * autoClearDay;
                String date = Utility.dateFormat(Utility.addDays(new Date(), day));
                return date.compareTo(s) >= 0;
            }

        });
    }
}
