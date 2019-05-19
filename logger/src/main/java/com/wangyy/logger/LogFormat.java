package com.wangyy.logger;

/**
 * 格式化输出日志
 *
 * @author zhangpfan.
 */
public interface LogFormat {

    /**
     * 格式化控制台输出.
     *
     * @param logInfo 日志信息
     */
    String formatConsole(LogInfo logInfo);

    /**
     * 格式化输出, 可以是普通文本，可以是xml.
     *
     * @param logInfo 日志信息
     */
    String formatOutput(LogInfo logInfo);
}
