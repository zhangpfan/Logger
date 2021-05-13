package com.example.logger;

import android.app.Application;

import com.fan.logger.crash.CrashWatchDog;

/**
 * @author fan
 * @date 2021/5/11 上午10:47
 */
public class LoggerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CrashWatchDog watchDog = new CrashWatchDog(this);
        watchDog.watch();
    }
}
