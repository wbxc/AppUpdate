package com.hy.wb.update;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hy.wb.update.imp.ApkUpdateCallBack;

import java.io.File;
import java.util.List;

/**
 * Created by familylove on 2017/7/20.
 */

public class ApkUpdateUtil {

    private static final String KEY_DOWNLOAD_ID = "downloadId";
    private static Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    private static ApkUpdateUtil instance;
    private DownloadManager mDownloadManager;
    private ApkUpdateCallBack mCallBack;
    private ApkUpdateContentObserver mObserver;
    private Context mContext;

    private Dialog mShowSelectApkUpdateDialog;
    private TextView tvUpdateMessage;
    private Dialog mShowProgressDialog;
    private ApkUpdateConfig mApkUpdateConfig;
    private Handler mHandler;
    //显示下载进度Dialog
    private ProgressBar pbUpdateProgress;


    private ApkUpdateUtil(Context context) {
        this.mContext = context;
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static ApkUpdateUtil getInstance(Context context) {

        if (instance == null)
            instance = new ApkUpdateUtil(context);
        return instance;
    }

    /**
     * 调用定义好的Dialog
     *
     * @param context
     * @param apkUpdateConfig
     */
    public void showDialog(final Context context, ApkUpdateConfig apkUpdateConfig) {

        if (!compare(context, apkUpdateConfig.getSerVerCode())) {  // 不需要升级
            return;
        }
        this.mApkUpdateConfig = apkUpdateConfig;
        if (mShowSelectApkUpdateDialog == null) {
            mShowSelectApkUpdateDialog = new Dialog(context, R.style.Dialog_Style);
            View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_update_log, null);
            tvUpdateMessage = (TextView) view.findViewById(R.id.tvUpdateMessage);
            Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
            Button btnDownload = (Button) view.findViewById(R.id.btnDownload);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mShowSelectApkUpdateDialog.isShowing())
                        mShowSelectApkUpdateDialog.dismiss();
                }
            });

            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mShowSelectApkUpdateDialog.isShowing())
                        mShowSelectApkUpdateDialog.dismiss();

                    showProgressDialog(context);
                    startApkDownload(mApkUpdateConfig);
                }
            });
            mShowSelectApkUpdateDialog.setContentView(view);
            mShowSelectApkUpdateDialog.setCanceledOnTouchOutside(false);
            mShowSelectApkUpdateDialog.setCancelable(true);
        }

        if (!mShowSelectApkUpdateDialog.isShowing()) {
            mShowSelectApkUpdateDialog.show();
        }
        if (tvUpdateMessage != null && !TextUtils.isEmpty(apkUpdateConfig.getUpdateMessage()))
            tvUpdateMessage.setText(apkUpdateConfig.getUpdateMessage());
    }


    private void showProgressDialog(final Context context) {

        if (mShowProgressDialog == null) {
            mShowProgressDialog = new Dialog(context, R.style.Dialog_Style);

            View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_update, null);
            pbUpdateProgress = (ProgressBar) view.findViewById(R.id.pbUpdateProgress);
            Button btnDownloadCancel = (Button) view.findViewById(R.id.btnDownloadCancel);
            btnDownloadCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mShowProgressDialog.isShowing())
                        mShowProgressDialog.dismiss();
                    //取消下载应用
                    if (mDownloadManager != null)
                        mDownloadManager.remove(getLocalDownloadId(context));
                }
            });

            Button btnStartDownload = (Button) view.findViewById(R.id.btnStartDownload);
            btnStartDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //重新下载应用
                    startApkDownload(mApkUpdateConfig);
                }
            });
            mShowProgressDialog.setContentView(view);
            mShowProgressDialog.setCancelable(false);
            mShowProgressDialog.setCanceledOnTouchOutside(false);
        }
        if (!mShowProgressDialog.isShowing())
            mShowProgressDialog.show();
    }

    //下载
    private void startApkDownload(ApkUpdateConfig config) {

        //1、判断网络组件是否可用
        if (!checkDownloadState(config.getContext())) {
            showDownloadSetting(config.getContext());
            return;
        }
        long downloadId = getLocalDownloadId(config.getContext());
        int status = getDownloadStatus(config.getContext(), downloadId);
        switch (status) {
            case DownloadManager.STATUS_SUCCESSFUL:
                //以前下载成功了
                //比较当前的版本和已经下载的版本\
                Uri uri = getDownloadUri(downloadId);
                if (uri != null) {
                    if (compare(config.getContext(), uri.getPath())) {  // 当前版本小于下载的版本
                        startInstall(config.getContext(), uri);
                        return;
                    } else {
                        mDownloadManager.remove(downloadId);
                    }
                }
                startDownload(config);
                break;
            case DownloadManager.STATUS_PAUSED:
                startDownload(config);
                break;
            case DownloadManager.STATUS_RUNNING:
                startDownload(config);
                break;
            case DownloadManager.STATUS_FAILED:
                startDownload(config);
                break;
            case -1:  // 没有存储数据
                startDownload(config);
                break;
        }
    }

    /**
     * 开始Apk应用下载
     *
     * @param config
     * @param callBack
     */
    public void startApkDownload(ApkUpdateConfig config, ApkUpdateCallBack callBack) {

        if (!compare(config.getContext(), config.getSerVerCode())) {  // 不需要升级
            return;
        }

        this.mCallBack = callBack;
        //1、判断网络组件是否可用
        if (!checkDownloadState(config.getContext())) {
            showDownloadSetting(config.getContext());
            return;
        }
        long downloadId = getLocalDownloadId(config.getContext());
        int status = getDownloadStatus(config.getContext(), downloadId);
        switch (status) {
            case DownloadManager.STATUS_SUCCESSFUL:
                //以前下载成功了
                //比较当前的版本和已经下载的版本\
                Uri uri = getDownloadUri(downloadId);

                if (uri != null) {
                    if (compare(config.getContext(), uri.getPath())) {  // 当前版本小于下载的版本
                        startInstall(config.getContext(), uri);
                        return;
                    } else {
                        mDownloadManager.remove(downloadId);
                    }
                }
                startDownload(config);
                break;
            case DownloadManager.STATUS_PAUSED:
                startDownload(config);
                break;
            case DownloadManager.STATUS_RUNNING:
                break;
            case DownloadManager.STATUS_FAILED:
                startDownload(config);
                break;
        }
    }


    //下载完成通知
    public void onNoticeComplete(long downloadId) {

        if (mObserver != null)
            mContext.getContentResolver().unregisterContentObserver(mObserver);

        if (mCallBack != null) {
            mCallBack.onSuccessDownload(downloadId);
        }

        if (mShowProgressDialog != null && mShowProgressDialog.isShowing())
            mShowProgressDialog.dismiss();

        if (mDownloadManager != null)
            startInstall(mContext, downloadId);
    }


    class ApkUpdateContentObserver extends ContentObserver {

        public ApkUpdateContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (mDownloadManager != null) {
                int[] tempArray = getBytesAndStatus(mDownloadManager, getLocalDownloadId(mContext));
                if (tempArray != null) {
                    //tempArray[0]  当前下载的数据量
                    //tempArray[1]  总数据
                    //tempArray[2]  当前状态
                    int tempProgress = tempArray[0] * 100 / tempArray[1];
                    if (mCallBack != null) {
                        mCallBack.onDownloading(tempArray[0], tempArray[1]);
                        mCallBack.onDownloadProgress(tempProgress);
                    }
                    if (mShowProgressDialog != null) {
                        if (pbUpdateProgress != null)
                            pbUpdateProgress.setProgress(tempProgress);
                    }
                }
            }
        }
    }

    /**
     * 获取下载信息
     *
     * @param downloadManager
     * @param downloadId
     * @return
     */
    private int[] getBytesAndStatus(DownloadManager downloadManager, long downloadId) {
        int[] bytesAndStatus = new int[]{-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return bytesAndStatus;
    }


    //APK 下载
    private void startDownload(ApkUpdateConfig updaterConfig) {
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(updaterConfig.getFileUrl()));
        req.setAllowedNetworkTypes(updaterConfig.getAllowedNetworkTypes());
        //移动网络是否允许下载
        req.setAllowedOverRoaming(updaterConfig.isAllowedOverRoaming());
        if (updaterConfig.isCanMediaScanner()) {
            //能够被MediaScanner扫描
            req.allowScanningByMediaScanner();
        }
        //是否显示状态栏下载UI
        req.setNotificationVisibility(updaterConfig.isIsNotificationVisibility());
        //点击正在下载的Notification进入下载详情界面，如果设为true则可以看到下载任务的进度，如果设为false，则看不到我们下载的任务
        req.setVisibleInDownloadsUi(updaterConfig.isShowDownloadUI());
        //设置文件的保存的位置[三种方式]
        //第一种
        //file:///storage/emulated/0/Android/data/your-package/files/Download/update.apk
        if (TextUtils.isEmpty(updaterConfig.getFilename())) {
            req.setDestinationInExternalFilesDir(updaterConfig.getContext(), Environment.DIRECTORY_DOWNLOADS, "update.apk");
        } else {
            req.setDestinationInExternalFilesDir(updaterConfig.getContext(), Environment.DIRECTORY_DOWNLOADS, updaterConfig.getFilename());
        }

        //第二种
        //file:///storage/emulated/0/Download/update.apk
        //req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk");

        //第三种 自定义文件路径
        //req.setDestinationUri()
        // 设置一些基本显示信息
        req.setTitle(updaterConfig.getTitle());
        req.setDescription(updaterConfig.getDescription());
        long id = mDownloadManager.enqueue(req);
        //把DownloadId保存到本地
        setDownloadId(updaterConfig.getContext(), id);
        mObserver = new ApkUpdateContentObserver(null);
        updaterConfig.getContext().getContentResolver().registerContentObserver(CONTENT_URI, true, mObserver);
    }

    /**
     * 系统的下载组件是否可用
     *
     * @return boolean
     */
    private boolean checkDownloadState(Context context) {
        try {
            int state = context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void showDownloadSetting(Context context) {
        String packageName = "com.android.providers.downloads";
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        if (intentAvailable(context, intent)) {
            context.startActivity(intent);
        }
    }

    /**
     * 要启动的intent是否可用
     *
     * @return boolean
     */
    private boolean intentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    /**
     * 获取下载状态
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return int
     * @see DownloadManager#STATUS_PENDING
     * @see DownloadManager#STATUS_PAUSED
     * @see DownloadManager#STATUS_RUNNING
     * @see DownloadManager#STATUS_SUCCESSFUL
     * @see DownloadManager#STATUS_FAILED
     */
    private int getDownloadStatus(Context context, long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = mDownloadManager.query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                }
            } finally {
                c.close();
            }
        }
        return -1;
    }


    /**
     * 获取apk程序信息[packageName,versionName...]
     *
     * @param context Context
     * @param path    apk path
     */
    private PackageInfo getApkInfo(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            return info;
        }
        return null;
    }

    /**
     * 下载的apk和当前程序版本比较
     *
     * @param context Context 当前运行程序的Context
     * @param path    apk file's location
     * @return 如果当前应用版本小于apk的版本则返回true
     */
    private boolean compare(Context context, String path) {

        PackageInfo apkInfo = getApkInfo(context, path);
        if (apkInfo == null) {
            return false;
        }
        String localPackage = context.getPackageName();
        try {
            //当前版本信息
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(localPackage, 0);
            if (apkInfo.packageName.equals(localPackage)) {
                if (apkInfo.versionCode > packageInfo.versionCode) {
                    return true;
                }
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 服务器版本与本地版本比较
     *
     * @param context
     * @param serVerCode
     * @return
     */
    private boolean compare(Context context, int serVerCode) {

        String localPackage = context.getPackageName();
        try {
            //当前版本信息
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(localPackage, 0);

            if (serVerCode > packageInfo.versionCode) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 根据下载downloadId
     *
     * @param downloadId
     * @return 获取下载路径
     */
    private Uri getDownloadUri(long downloadId) {

        String uriString = "";

        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cur = mDownloadManager.query(query);

            if (cur != null) {
                if (cur.moveToFirst()) {
                    uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    Log.e("HyPOCT", "" + uriString);
                }
                cur.close();
                cur = null;
            }
        }

        if (TextUtils.isEmpty(uriString)) {
            return null;
        } else {
            return Uri.parse(uriString);
        }
    }

    /**
     * 安装应用
     *
     * @param context
     * @param uri
     */
    private void startInstall(Context context, Uri uri) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(install);
    }

    /**
     * 安装应用
     *
     * @param context
     */
    public void startInstall(Context context, long downloadId) {

        String uriString = "";
        DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cur = downloader.query(query);

            if (cur != null) {
                if (cur.moveToFirst()) {
                    uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                }
                cur.close();
            }
        }

        if (!TextUtils.isEmpty(uriString)) {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.parse(uriString), "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(install);
        }
    }


    //获取下载App的downloadId
    public static long getLocalDownloadId(Context context) {
        SharedPreferences spf = context.getSharedPreferences("download_sp", Context.MODE_PRIVATE);
        return spf.getLong(KEY_DOWNLOAD_ID, -1L);
    }

    //保存downloadId
    public static void setDownloadId(Context context, long downloadId) {

        SharedPreferences spf = context.getSharedPreferences("download_sp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putLong(KEY_DOWNLOAD_ID, downloadId);
        editor.commit();
    }
}
