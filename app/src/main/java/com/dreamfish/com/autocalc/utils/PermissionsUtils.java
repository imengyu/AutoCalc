package com.dreamfish.com.autocalc.utils;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.dreamfish.com.autocalc.dialog.CommonDialogs;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsUtils {

  /**
   * 当前应用权限
   */
  public static String[] permissions = new String[]{
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  /**
   * 是否提示用户开启
   */
  public static boolean showSystemSetting = true;

  private PermissionsUtils() {
  }

  private static PermissionsUtils permissionsUtils;
  private IPermissionsResult mPermissionsResult;

  public static PermissionsUtils getInstance() {
    if (permissionsUtils == null) {
      permissionsUtils = new PermissionsUtils();
    }
    return permissionsUtils;
  }

  public void chekPermissions(Activity context, String[] permissions, @NonNull IPermissionsResult permissionsResult) {
    mPermissionsResult = permissionsResult;

    //创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();
    //逐个判断你要的权限是否已经通过
    for (int i = 0; i < permissions.length; i++) {
      if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
        mPermissionList.add(permissions[i]);//添加还未授予的权限
      }
    }

    //申请权限
    if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
      ActivityCompat.requestPermissions(context, permissions, CommonDialogs.RESULT_REQUEST_PERMISSION);
    } else {
      //说明权限都已经通过，可以做你想做的事情去
      permissionsResult.passPermissons();
      return;
    }


  }

  /**
   * 请求权限后回调的方法
   * @param context
   * @param requestCode 是我们自己定义的权限请求码
   * @param permissions 是我们请求的权限名称数组
   * @param grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
   */
  public void onRequestPermissionsResult(Activity context, int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    boolean hasPermissionDismiss = false;//有权限没有通过
    if (CommonDialogs.RESULT_REQUEST_PERMISSION == requestCode) {
      for (int i = 0; i < grantResults.length; i++) {
        if (grantResults[i] == -1) {
          hasPermissionDismiss = true;
        }
      }
      //如果有权限没有被允许
      if (hasPermissionDismiss) {
        if (showSystemSetting) {
          showSystemPermissionsSettingDialog(context);//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
        } else {
          mPermissionsResult.forbitPermissons();
        }
      } else {
        //全部权限通过，可以进行下一步操作。。。
        mPermissionsResult.passPermissons();
      }
    }

  }

  /**
   * 不再提示权限时的展示对话框
   */
  AlertDialog mPermissionDialog;

  public void goSettingsPage(Activity context) {
    final String mPackName = context.getPackageName();
    Uri packageURI = Uri.parse("package:" + mPackName);
    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
    context.startActivity(intent);
  }

  private void showSystemPermissionsSettingDialog(final Activity context) {

    if (mPermissionDialog == null) {
      mPermissionDialog = new AlertDialog.Builder(context)
              .setMessage("您似乎禁用了软件的部分权限，这可能会导致应用部分功能不可用或不正常，是否继续？")
              .setPositiveButton("去设置开启", (dialog, which) -> {
                cancelPermissionDialog();
                goSettingsPage(context);
                context.finish();
              })
              .setNegativeButton("仍然继续", (dialog, which) -> {
                //关闭页面或者做其他操作
                cancelPermissionDialog();
                //mContext.finish();
                mPermissionsResult.forbitPermissons();
              })
              .create();
    }
    mPermissionDialog.show();
  }

  //关闭对话框
  private void cancelPermissionDialog() {
    if (mPermissionDialog != null) {
      mPermissionDialog.cancel();
      mPermissionDialog = null;
    }

  }

  public interface IPermissionsResult {
    void passPermissons();
    void forbitPermissons();
  }

}
