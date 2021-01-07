package com.dreamfish.com.autocalc.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.dreamfish.com.autocalc.R;
import com.dreamfish.com.autocalc.config.ConstConfig;

import java.io.File;
import java.util.Base64;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

public class UpdaterUtils {

  public UpdaterUtils(Activity activity) {
    this.activity = activity;
    updaterUtils = this;
    initResources();
  }

  private static UpdaterUtils updaterUtils;

  public static UpdaterUtils getInstance() {
    return updaterUtils;
  }

  private Activity activity;
  private boolean checkingUpdateing = false;

  private void initResources() {
    resources = activity.getResources();
    text_check_update = resources.getString(R.string.text_check_update);
    text_check_update_failed = resources.getString(R.string.text_check_update_failed);
    text_latest = resources.getString(R.string.text_latest);
    text_new_ver = resources.getString(R.string.text_new_ver);
    text_downloading_update = resources.getString(R.string.text_new_ver);
  }

  private Resources resources;
  private String text_check_update;
  private String text_check_update_failed;
  private String text_new_ver;
  private String text_latest;
  private String text_downloading_update;

  private AlertDialog checkingUpdateDialog = null;
  private String checkingUpdateNewVer = "";
  private String checkingUpdateErr = "";
  private String checkingUpdateNewIntrod = "";
  private boolean checkingLatest = false;
  private boolean checkingIsAuto = false;

  /**
   * 检查网络状态
   */
  private void checkIsMobileNetwork() {

    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    if (activeNetworkInfo == null) {
      Toast.makeText(activity, R.string.text_no_network, Toast.LENGTH_LONG).show();
      checkingUpdateing = false;
      return;
    }
    if(activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
     doUpdate();
    } else if(!checkingIsAuto) {
      new AlertDialog.Builder(activity)
              .setMessage(R.string.text_in_mobile_network)
              .setPositiveButton(R.string.text_do_not_update, (dialog, which) -> {
                checkingUpdateing = false;
                dialog.dismiss();
              })
              .setNegativeButton(R.string.text_continue_update, (dialog, which) -> {
                doUpdate();
                dialog.dismiss();
              })
              .create()
              .show();
    } else checkingUpdateing = false;
  }

  /**
   * 检查更新
   */
  public void checkUpdate(boolean isAutoCheck) {
    checkingUpdateing = true;
    checkingIsAuto = isAutoCheck;
    checkIsMobileNetwork();
  }
  private void doUpdate() {

    checkingUpdateing = false;

    if(!checkingIsAuto) {
      checkingUpdateDialog = AlertDialogTool.buildLoadingDialog(activity, text_check_update, false);
      checkingUpdateDialog.show();
    }

    new Thread(() -> {
      boolean success = false;

      try {
        PackageInfo packageInfo = activity.getApplicationContext()
                .getPackageManager()
                .getPackageInfo(activity.getPackageName(), 0);

        Thread.sleep(900);
        JSONObject object = HttpUtils.httpGetJson(ConstConfig.URL_CHECK_UPDATE +
                packageInfo.versionCode);
        if(object != null) {
          if (object.getInteger("code") == 200) {
            if (!object.getBoolean("update")) checkingLatest = true;
            else {
              checkingLatest = false;
              checkingUpdateNewVer = object.getString("ver");
              checkingUpdateNewIntrod = Base64Utils.decode(object.getString("text"));
            }
            success = true;
          } else checkingUpdateErr = object.getString("message");
        } else checkingUpdateErr = "未知错误";
      } catch (Exception e) {
        e.printStackTrace();
        checkingUpdateErr = e.getMessage();
      }

      if(success) {
        activity.runOnUiThread(() -> {
          if(checkingLatest)
            Toast.makeText(activity, text_latest, Toast.LENGTH_LONG).show();
          else
            askForUpdate();
          if(!checkingIsAuto) checkingUpdateDialog.cancel();
        });
      }
      else {
        activity.runOnUiThread(() -> {
          Toast.makeText(activity, text_check_update_failed + " " + checkingUpdateErr, Toast.LENGTH_LONG).show();
          if(!checkingIsAuto) checkingUpdateDialog.cancel();
        });
      }
    }).start();
  }

  public void askForUpdate() {
    LayoutInflater inflater = LayoutInflater.from(activity);
    View v = inflater.inflate(R.layout.dialog_update, null);

    AlertDialog dialog = new AlertDialog.Builder(activity, R.style.WhiteRoundDialog)
            .setView(v)
            .setCancelable(false)
            .create();

    Window window = dialog.getWindow();
    window.setGravity(Gravity.CENTER);

    WindowManager.LayoutParams lp = window.getAttributes();
    lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

    window.setAttributes(lp);
    window.setWindowAnimations(R.style.DialogBottomPopup);

    StringBuilder sb = new StringBuilder(text_new_ver);
    sb.append(checkingUpdateNewVer);
    sb.append("\n");
    sb.append(checkingUpdateNewIntrod);

    ((TextView)v.findViewById(R.id.text_main)).setText(sb.toString());
    v.findViewById(R.id.btn_do_not_update).setOnClickListener(view -> dialog.dismiss());
    v.findViewById(R.id.btn_update).setOnClickListener(view -> {
      Toast.makeText(activity, text_downloading_update, Toast.LENGTH_LONG).show();
      requestUpdate();
      dialog.dismiss();
    });

    dialog.show();
  }

  //下载器
  private DownloadManager downloadManager;
  //下载的ID
  private long downloadId;
  private String downloadPathStr;

  /**
   * 下载更新
   */
  private void requestUpdate() {
    //创建下载任务
    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(ConstConfig.URL_DOWNLOAD_UPDATE));
    //移动网络情况下是否允许漫游
    request.setAllowedOverRoaming(false);
    //在通知栏中显示，默认就是显示的
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
    request.setTitle(resources.getString(R.string.text_downloading_update_title));
    request.setDescription(resources.getString(R.string.text_downloading_update));
    request.setVisibleInDownloadsUi(true);

    //设置下载的路径
    File file = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "AutoCalc.apk");
    request.setDestinationUri(Uri.fromFile(file));
    downloadPathStr = file.getAbsolutePath();
    //获取DownloadManager
    if (downloadManager == null)
      downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
    //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
    if (downloadManager != null) {
      downloadId = downloadManager.enqueue(request);
    }

    //注册广播接收者，监听下载状态
    activity.registerReceiver(receiver,
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

  }

  //广播监听下载的各个状态
  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      checkStatus();
    }
  };

  //检查下载状态
  private void checkStatus() {
    DownloadManager.Query query = new DownloadManager.Query();
    //通过下载的id查找
    query.setFilterById(downloadId);
    Cursor cursor = downloadManager.query(query);
    if (cursor.moveToFirst()) {
      int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
      switch (status) {
        //下载暂停
        case DownloadManager.STATUS_PAUSED:
          break;
        //下载延迟
        case DownloadManager.STATUS_PENDING:
          break;
        //正在下载
        case DownloadManager.STATUS_RUNNING:
          break;
        //下载完成
        case DownloadManager.STATUS_SUCCESSFUL:
          //下载完成安装APK
          installAPK();
          cursor.close();
          break;
        //下载失败
        case DownloadManager.STATUS_FAILED:
          Toast.makeText(activity, "下载失败", Toast.LENGTH_SHORT).show();
          cursor.close();
          activity.unregisterReceiver(receiver);
          break;
      }
    }
  }

  /**
   * 安装apk
   */
  private void installAPK() {

    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    if (Build.VERSION.SDK_INT >= 24) {
      File file = (new File(downloadPathStr));
      //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
      Uri apkUri = FileProvider.getUriForFile(activity, "com.dreamfish.com.fileprovider", file);
      //添加这一句表示对目标应用临时授权该Uri所代表的文件
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
    } else {
      intent.setDataAndType(Uri.fromFile(new File(Environment.DIRECTORY_DOWNLOADS, "AutoCalc.apk")), "application/vnd.android.package-archive");
    }
    activity.startActivity(intent);
  }



}
