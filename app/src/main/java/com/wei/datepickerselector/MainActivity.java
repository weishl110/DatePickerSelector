package com.wei.datepickerselector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wsl.datepickerlib.CustomDate;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv_click = (TextView) findViewById(R.id.tv_click);
        tv_click.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date(System.currentTimeMillis()));
        CustomDate customDate = new CustomDate(this, new CustomDate.ResultHandler() {
            @Override
            public void handle(String time) {
                Toast.makeText(MainActivity.this, time, Toast.LENGTH_SHORT).show();
            }
        }, "1970-01-01", currentDate, CustomDate.MODE.YMD);
        customDate.setIsLoop(false);
        customDate.setCurrentDate("2000-01-01");
        customDate.show();
    }
}
