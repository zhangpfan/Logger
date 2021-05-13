package com.example.logger;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author fan
 * @date 2021/5/11 上午10:42
 */
public class MainActivity extends AppCompatActivity {

    TextView tvHelloWorld;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvHelloWorld = findViewById(R.id.tvHelloWorld);
        tvHelloWorld.setText(0);
    }
}
