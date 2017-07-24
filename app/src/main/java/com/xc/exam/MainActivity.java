package com.xc.exam;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hy.wb.update.ApkUpdateConfig;
import com.hy.wb.update.ApkUpdateUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDownload = (Button) findViewById(R.id.btnDownload);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApkUpdateConfig.Builder builder = new ApkUpdateConfig.Builder(MainActivity.this);
                builder.setCanMediaScanner(true)
                        .setFileUrl("")
                        .setSerVerCode(2)
                        .setContext(MainActivity.this)
                        .setFilename("test.apk");

                ApkUpdateUtil.getInstance(MainActivity.this).showDialog(MainActivity.this, builder.build());
            }
        });
    }
}
