package com.wangyy.logger;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 工具类
 *
 * @author zhangpfan.
 */
public final class Utility {

    private static final ThreadLocal<SimpleDateFormat> defaultDateTimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        }
    };

    private static final ThreadLocal<SimpleDateFormat> defaultDateFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        }

    };

    private Utility() {
    }

    public static String dateTimeFormat() {
        return defaultDateTimeFormat.get().format(new Date());
    }

    public static String dateFormat() {
        return defaultDateFormat.get().format(new Date());
    }

    public static String dateFormat(Date date) {
        return defaultDateFormat.get().format(date);
    }

    public static Date addDays(@NonNull Date date, int diff) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, diff);
        return cal.getTime();
    }

    /**
     * 删除指定目录中特定的文件
     *
     * @param dir    指定要删除的目录
     * @param filter 文件名过滤条件
     */
    public static void delete(String dir, FilenameFilter filter) {
        if (TextUtils.isEmpty(dir))
            return;
        File file = new File(dir);
        if (!file.exists())
            return;
        if (file.isFile())
            file.deleteOnExit();
        if (!file.isDirectory())
            return;

        File[] lists = null;
        if (filter != null) {
            lists = file.listFiles(filter);
        } else {
            lists = file.listFiles();
        }

        if (lists == null)
            return;
        for (File f : lists) {
            if (f.isFile()) {
                f.deleteOnExit();
            }
        }
    }

    /**
     * 删除指定文件或指定目录内的所有文件
     *
     * @param path 文件或目录的绝对路径
     * @return 路径为空或空白字符串，返回true；文件不存在，返回true；文件删除返回true；
     * 文件删除异常返回false
     */
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }
        return deleteFile(new File(path));
    }

    /**
     * 删除指定文件或指定目录内的所有文件
     *
     * @param file 要删除的文件
     * @return 路径为空或空白字符串，返回true；文件不存在，返回true；文件删除返回true；
     * 文件删除异常返回false
     */
    private static boolean deleteFile(File file) {
        if (file == null)
            throw new NullPointerException("file is null");
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }

        File[] files = file.listFiles();
        if (files == null)
            return true;
        for (File f : files) {
            if (f.isFile()) {
                f.deleteOnExit();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * 获取某个目录下的文件名
     *
     * @param dirPath    目录
     * @param fileFilter 过滤器
     * @return 某个目录下的所有文件名
     */
    public static List<String> getFileNameList(String dirPath,
                                               FilenameFilter fileFilter) {
        if (fileFilter == null)
            return getFileNameList(dirPath);
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);

        File[] files = dir.listFiles(fileFilter);
        if (files == null)
            return Collections.emptyList();

        List<String> conList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    public static List<String> getFileNameList(String dirPath) {
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null)
            return Collections.emptyList();
        List<String> conList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    public static List<String> getFileNameList(String dirPath,
                                        final String... extensions) {
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);
        File[] files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                for (String extension : extensions) {
                    if (filename.indexOf("." + extension) > 0)
                        return true;
                }
                return false;
            }
        });
        if (files == null)
            return Collections.emptyList();
        List<String> conList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    public static String getFileNameWithoutExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(".");
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0,
                    extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1,
                extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * 读取文件的内容
     * <br>
     * 默认utf-8编码
     *
     * @param filePath 文件路径
     * @return 字符串
     * @throws IOException
     */
    public static String readFile(String filePath) throws IOException {
        return readFile(filePath, "utf-8");
    }

    /**
     * 读取文件的内容
     *
     * @param filePath    文件目录
     * @param charsetName 字符编码
     * @return String字符串
     */
    public static String readFile(String filePath, String charsetName)
            throws IOException {
        if (TextUtils.isEmpty(filePath))
            return null;
        if (TextUtils.isEmpty(charsetName))
            charsetName = "utf-8";
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (!file.isFile())
            return null;
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\r\n");
            }
            return fileContent.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 用nio的方式写文件
     */
    public static boolean writeFileByNio(String filePath, String content,
                                  boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath))
            return false;
        if (TextUtils.isEmpty(content))
            return false;

        FileOutputStream outputStream = null;
        FileChannel channel = null;
        try {
            createFile(filePath);
            outputStream = new FileOutputStream(filePath, append);
            channel = outputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.wrap(content.getBytes(Charset.defaultCharset()));
            channel.write(buffer);
            outputStream.flush();
            buffer.clear();
            return true;
        } finally {
            close(channel);
            close(outputStream);
        }
    }

    private static boolean createFile(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        return createFile(new File(path));
    }

    private static boolean createFile(File file) {
        if (file == null || !makeDirs(getFolderName(file.getAbsolutePath())))
            return false;
        if (!file.exists())
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        return false;
    }

    private static boolean makeDirs(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File folder = new File(filePath);
        return (folder.exists() && folder.isDirectory()) || folder
                .mkdirs();
    }

    private static String getFolderName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 收集一些设备信息.
     */
    public static Map<String, String> collectDeviceInfo(Context ctx) {
        Map<String, String> info = new HashMap<>();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName + "";
                String versionCode = pi.versionCode + "";
                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                info.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return info;
    }

    /**
     * 保存crash日志文件.
     *
     * @param dir     保存路径-相对路径名称
     * @param ex      异常
     * @param content 已收集到的设备信息
     * @throws IOException
     */
    public static void saveCrashInfoFile(String dir, Throwable ex, Map<String, String> content) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("\r\n").append(dateTimeFormat()).append("\n");
            for (Map.Entry<String, String> entry : content.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            sb.append(Log.getStackTraceString(ex));
            writeCrashLog(dir, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            sb.append("an error occurred while writing file...\r\n");
            writeCrashLog(dir, sb.toString());
        }
    }

    /**
     * 将crash信息写到SD卡上.
     *
     * @throws IOException
     */
    public static void writeCrashLog(String dir, String content) throws IOException {
        String fileName = "crash-" + dateFormat() + ".txt";
        writeFileByNio(dir + File.separator + fileName, content, true);
    }

    /**
     * 获取Crash根目录.
     */
    public static String getCrashGlobalPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "crash" + File.separator;
    }

    public static boolean isDebugable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
