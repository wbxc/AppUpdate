package com.hy.wb.update.imp;

/**
 * Created by familylove on 2017/7/20.
 */

public interface ApkUpdateCallBack {


    public void onSuccessDownload(long downloadId) ;

    public void onErrorDownload(int status) ;

    public void onDownloading(long downloadSize , long totalSize) ;

    public void onDownloadProgress(int progress) ;
}
