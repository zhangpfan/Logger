package com.fan.logger;

/**
 * 日志实体
 * <br>
 * 包含日志级别、tag、message、异常
 *
 * @author fan
 */
public class LogInfo {

    private Level level;
    private String tag;
    private String message;
    private String throwableInfo;

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThrowableInfo() {
        return throwableInfo;
    }

    public void setThrowableInfo(String throwableInfo) {
        this.throwableInfo = throwableInfo;
    }

    @Override
    public String toString() {
        return "LogInfo{" +
                "level=" + level +
                ", tag='" + tag + '\'' +
                ", message='" + message + '\'' +
                ", throwableInfo='" + throwableInfo + '\'' +
                '}';
    }
}
