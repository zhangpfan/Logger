package com.wangyy.logger;

import android.os.Environment;
import android.support.annotation.IntRange;
import android.text.TextUtils;

import java.io.File;

/**
 * 日志环境配置
 * 主要用来做一些配置，如tag，log文件路径等
 *
 * @author zhangpfan.
 */
public final class LogConfig {
    /**
     * 如未设置tag则采用该参数, Logger
     */
    private static final String DEFAULT_TAG = "Logger";

    /**
     * 如未设置文件路径则采用该参数, sd下Logger/目录.
     */
    private static final String DEFAULT_FILE_PATH_PREFIX = Environment
            .getExternalStorageDirectory() + File.separator + "Logger";
    private String fileDir;
    private String tag;
    /**
     * 该标记用来控制debug是否需要写文件<br>
     * 为true时会写文件，默认为false
     */
    private boolean debug = false;
    /**
     * 日志格式化输出.
     */
    private LogFormat logFormat = new SimpleLogFormat();
    /**
     * 日志的自动清理天数.
     */
    private int autoClearDays = 15;

    private LogConfig() {
    }

    public static class Builder {
        private final LogConfig config;

        public Builder() {
            config = new LogConfig();
        }

        /**
         * 设置日志tag
         *
         * @param tag
         */
        public Builder tag(String tag) {
            config.tag = tag;
            return this;
        }

        /**
         * 设置日志绝对路径
         */
        public Builder logDir(String dir) {
            config.fileDir = dir;
            return this;
        }

        /**
         * 设置日志文件夹名称（sd卡根目录/Logger/dirName）
         */
        public Builder dirName(String dirName) {
            config.fileDir = DEFAULT_FILE_PATH_PREFIX + File.separator + dirName;
            return this;
        }

        /**
         * 开启debug.
         * 为true时会写文件，默认为false
         */
        public Builder debug() {
            config.debug = true;
            return this;
        }

        /**
         * 设置自动清理日志天数.
         */
        public Builder autoClearDays(@IntRange(from = 0, to = 365) int days) {
            config.autoClearDays = days;
            return this;
        }

        /**
         * 设置日志输出格式.
         */
        public Builder setLogFormat(LogFormat format) {
            config.logFormat = format;
            return this;
        }

        public LogConfig build() {
            return config;
        }
    }

    public String getTag() {
        return TextUtils.isEmpty(tag) ? DEFAULT_TAG : tag;
    }

    public String getLogFileDirectory() {
        return TextUtils.isEmpty(fileDir) ? DEFAULT_FILE_PATH_PREFIX : fileDir;
    }

    public LogFormat getLogFormat() {
        return logFormat;
    }

    public boolean isDebug() {
        return debug;
    }

    int getAutoClearDays() {
        return autoClearDays < -1 ? -1 : autoClearDays;
    }

}
