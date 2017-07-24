package com.xc.exam;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.hy.wb.update.ApkUpdateConfig;
import com.hy.wb.update.ApkUpdateUtil;
import com.hy.wb.update.imp.ApkUpdateCallBack;

public class MainActivity extends AppCompatActivity {

    private String apkDownloadUrl = "http://download.fir.im/v2/app/install/56a9da5c00fc740be800000c?download_token=ef9de8e878f6cbc6e7d3b6a10d78ef34&source=update" ;


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
                        .setFileUrl(apkDownloadUrl)   // 服务器的应用下载连接
                        .setUpdateMessage("1、版本升级\n2、Bug修复")
                        .setSerVerCode(2)
                        .setContext(MainActivity.this)
                        .setFilename("test.apk");

                ApkUpdateUtil.getInstance(MainActivity.this).showDialog(MainActivity.this, builder.build());
            }
        });


        Button btnCallBackDownload = (Button)findViewById(R.id.btnCallBackDownload) ;
        btnCallBackDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApkUpdateConfig.Builder builder = new ApkUpdateConfig.Builder(MainActivity.this);
                builder.setCanMediaScanner(true)
                        .setFileUrl(apkDownloadUrl)   // 服务器的应用下载连接
                        .setSerVerCode(2)
                        .setContext(MainActivity.this)
                        .setFilename("test.apk");

                ApkUpdateUtil.getInstance(MainActivity.this).startApkDownload(builder.build(),mCallBack);
            }
        });
    }

    private ApkUpdateCallBack  mCallBack = new ApkUpdateCallBack() {
        @Override
        public void onSuccessDownload(long downloadId) {

        }

        @Override
        public void onErrorDownload(int status) {

        }

        @Override
        public void onDownloading(long downloadSize, long totalSize) {


            Log.e("HyPOCT","downloadSize-->"+downloadSize+">>>>"+"totalSize-->"+totalSize) ;
        }

        @Override
        public void onDownloadProgress(int progress) {

        }
    } ;
}
