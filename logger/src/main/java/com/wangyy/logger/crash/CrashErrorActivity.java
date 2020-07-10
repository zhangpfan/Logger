package com.wangyy.logger.crash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wangyy.logger.LoggerPreferences;
import com.wangyy.logger.R;
import com.wangyy.logger.Utility;

import java.util.Date;

/**
 * 崩溃时用于展示的界面
 */
public class CrashErrorActivity extends Activity implements View.OnClickListener {

    private Button btnShowCrash, btnRestartApplication, btnExitApplication, btnBack;
    private ViewGroup layoutOptions, layoutErrorReport;
    private TextView tvType, tvClassName, tvMethodName, tvLineNumber, tvCause, tvStackTrace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_error);

        layoutOptions = (ViewGroup) findViewById(R.id.layout_options);
        layoutErrorReport = (ViewGroup) findViewById(R.id.layout_error_report);

        tvType = (TextView) findViewById(R.id.tv_type);
        tvClassName = (TextView) findViewById(R.id.tv_class_name);
        tvMethodName = (TextView) findViewById(R.id.tv_method_name);
        tvLineNumber = (TextView) findViewById(R.id.tv_line_number);
        tvCause = (TextView) findViewById(R.id.tv_cause);
        tvStackTrace = (TextView) findViewById(R.id.tv_stack_trace);

        btnShowCrash = (Button) findViewById(R.id.btn_show_crash_message);
        btnRestartApplication = (Button) findViewById(R.id.btn_restart_application);
        btnExitApplication = (Button) findViewById(R.id.btn_exit_application);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnShowCrash.setOnClickListener(this);
        btnRestartApplication.setOnClickListener(this);
        btnExitApplication.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        setupErrorReport();

        // release模式如果发生未连续崩溃现象会直接重启应用，让客户少做一步操作
        if (!Utility.isDebugable(this)) {
            LoggerPreferences sp = new LoggerPreferences(this);
            // 设置crash, 提示应用发生过异常
            sp.setCrash(true);

            long curTime = new Date().getTime();
            long lastTime = sp.getLastCrashTime();
            sp.setLastCrashTime(curTime);

            // 如果两次崩溃的间隔时间超过3秒就认为不是连续崩溃
            if (curTime - lastTime > 3_000) {
                restartApp();
            }
        }
    }


    private void setupErrorReport() {
        CrashWatchDog.ExceptionData exceptionData = CrashWatchDog.getStackTraceFromIntent(getIntent());
        tvType.setText(exceptionData.type);
        tvClassName.setText(exceptionData.className);
        tvMethodName.setText(exceptionData.methodName);
        tvLineNumber.setText(String.format("LineNumber:%s", exceptionData.lineNumber));
        tvCause.setText(exceptionData.cause);
        tvStackTrace.setText(exceptionData.stackTrace);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnShowCrash)) {
            layoutOptions.setVisibility(View.GONE);
            layoutErrorReport.setVisibility(View.VISIBLE);
        } else if (v.equals(btnRestartApplication)) {
            restartApp();
        } else if (v.equals(btnExitApplication)) {
            exitApp();
        } else if (v.equals(btnBack)) {
            layoutOptions.setVisibility(View.VISIBLE);
            layoutErrorReport.setVisibility(View.GONE);
        }
    }

    private void restartApp() {
        Class<? extends Activity> restartActivity = CrashWatchDog.getRestartActivityFromIntent(getIntent());
        if (restartActivity == null) {
            Toast.makeText(CrashErrorActivity.this, "restartActivity is null", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.restart_app, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, restartActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void exitApp() {
        Intent i = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        finish();
        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }
}
