package com.fan.logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * <h3>日志工具类</h3>
 *
 * <p>主要封装了一些对日志的读写操作 ，注：写文件通过线程完成，读文件未启用线程。
 * <br>
 * <h5>日志记录:</h5>
 * 包含打印到控制台和写入文件两个方式
 * <h5>日志级别:</h5>
 * <p>目前定义了 info, debug, error三个级别。
 * <li><code>info</code>     该级别消息只会打印在控制台, RELEASE版本会被移除
 * <li><code>debug</code>    该级别消息即会打印在控制台, 也会写文件（通过开关控制，可写可不写）
 * <li><code>error</code>    该级别消息即会打印在控制台, 也会写文件（不可控制）
 *
 * <h5>TAG:</h5>
 * 可设置LogConfig中的默认tag, 也可直接调用传tag的方法
 *
 * @author fan
 */
public final class Logger {

    private final static LogManager logManager = LogManager.getLogger();

    private Logger() {
    }

    /**
     * 初始化日志配置.
     */
    public static void initLogConfig(LogConfig config) {
        logManager.initLogConfig(config);
    }

    /**
     * 在Logcat打印info级别信息.
     * <br/>
     * 默认为config中的TAG
     *
     * @param message 消息内容
     */
    public static void info(String message) {
        log(Level.INFO, null, message, null);
    }

    /**
     * 在Logcat打印info级别信息.
     *
     * @param pTag    TAG
     * @param message 消息内容
     */
    public static void info(String pTag, String message) {
        log(Level.INFO, pTag, message, null);
    }

    /**
     * 在Logcat打印debug级别信息, 在debug模式会将信息写入文件.
     * <br/>
     * 默认为config中的TAG
     *
     * @param message 消息内容
     */
    public static void debug(String message) {
        log(Level.DEBUG, null, message, null);
    }

    /**
     * 在Logcat打印debug级别信息, 在debug模式会将信息写入文件.
     *
     * @param pTag    TAG
     * @param message 消息内容
     */
    public static void debug(String pTag, String message) {
        log(Level.DEBUG, pTag, message, null);
    }

    /**
     * 在Logcat打印debug级别信息, 在debug模式会将信息写入文件.
     * <br/>
     * 默认为config中的TAG
     *
     * @param message 消息内容
     * @param e       异常
     */
    public static void debug(String message, Throwable e) {
        log(Level.DEBUG, null, message, e);
    }

    /**
     * 在Logcat打印error级别信息, 并将信息写入文件.
     * <br/>
     * 默认为config中的TAG
     *
     * @param message 消息内容
     */
    public static void error(String message) {
        log(Level.ERROR, null, message, null);
    }

    /**
     * 在Logcat打印error级别信息, 并将信息写入文件.
     * <br/>
     * 默认为config中的TAG
     *
     * @param pTag    TAG
     * @param message 消息内容
     */
    public static void error(String pTag, String message) {
        log(Level.ERROR, pTag, message, null);
    }

    /**
     * 在Logcat打印error级别信息, 并将信息写入文件.
     * <br/>
     * 默认为config中的TAG
     *
     * @param message 消息内容
     * @param e       异常
     */
    public static void error(String message, Throwable e) {
        log(Level.ERROR, null, message, e);
    }

    private static void log(Level level, String pTag, String message, Throwable e) {
        logManager.log(level, pTag, message, e);
    }

    /**
     * 读取指定的日志文件<br/>
     * 注：读文件操作在主线程中完成
     *
     * @param filePath 文件绝对路径
     * @return 日志文件中的内容
     */
    public static String readLog(String filePath) {
        return logManager.read(filePath);
    }

    /**
     * 获取客户日志目录下的日志文件列表.
     */
    public static List<String> getFileList() {
        return logManager.getFileList();
    }

    /**
     * 获取指定目录下的日志文件列表.
     *
     * @param dirPath 目录
     * @return 目录下的日志文件名
     */
    public static List<String> getFileList(String dirPath) {
        return logManager.getFileList(dirPath);
    }

    /**
     * 删除日志文件.
     *
     * @param fileName 文件名
     * @return 删除成功返回true
     */
    public static boolean delete(String fileName) {
        return logManager.delete(fileName);
    }

    /**
     * 删除默认日志目录.
     */
    public static boolean deleteDir() {
        return logManager.deleteDir();
    }

    /**
     * 获得日志目录.
     */
    public static String getDirPath() {
        return logManager.getLogDirectory();
    }

    /**
     * 自动清理日志.
     */
    public static void autoClear() {
        logManager.autoClear();
    }

    /**
     * 格式化Json字符串, 便于阅读.
     *
     * @param json 目标字符串
     * @return 格式化之后的字符串
     */
    public static String jsonFormat(String json) {
        if (TextUtils.isEmpty(json)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(4);
                sb.append(message);
                return sb.toString();
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(4);
                sb.append(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 格式化xml字符串，便于阅读.
     *
     * @param xml 目标字符串
     * @return 格式化之后的字符串
     */
    @NonNull
    public static String xmlFormat(@Nullable String xml) {
        if (TextUtils.isEmpty(xml)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            sb.append(xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
