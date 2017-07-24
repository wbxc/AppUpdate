package com.hy.wb.update.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hy.wb.update.ApkUpdateUtil;

/**
 * Created by familylove on 2017/7/20.
 */

public class ApkUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:  // 下载完成
                long completeDownload = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                ApkUpdateUtil.getInstance(context).onNoticeComplete(completeDownload);
                break;
            case DownloadManager.ACTION_NOTIFICATION_CLICKED:
                break;
        }
    }


}
