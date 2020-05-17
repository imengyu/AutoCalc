package com.dreamfish.com.autocalc.dialog;

import android.app.Activity;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dreamfish.com.autocalc.AboutActivity;
import com.dreamfish.com.autocalc.HelpActivity;
import com.dreamfish.com.autocalc.MainActivity;
import com.dreamfish.com.autocalc.R;
import com.dreamfish.com.autocalc.SettingsActivity;
import com.dreamfish.com.autocalc.utils.AlertDialogTool;

import androidx.appcompat.app.AlertDialog;

public class CommonDialogs {

  public static final int RESULT_SETTING_ACTIVITY = 0;
  public static final int RESULT_REQUEST_PERMISSION = 101;

  public static void showHelp(Activity activity) {
    activity.startActivity(new Intent(activity, HelpActivity.class));
  }
  public static void showAbout(Activity activity) {
    activity.startActivity(new Intent(activity, AboutActivity.class));
  }
  public static void showSettings(Activity activity) {
    activity.startActivityForResult(new Intent(activity, SettingsActivity.class), RESULT_SETTING_ACTIVITY);
  }

  public interface OnAgreementCloseListener {
    void onAgreementClose(boolean allowed);
  }

  public static void showPrivacyPolicyAndAgreement(Activity activity, OnAgreementCloseListener onAgreementCloseListener) {
    LayoutInflater inflater = LayoutInflater.from(activity);
    View v = inflater.inflate(R.layout.dialog_argeement, null);

    AlertDialog dialog = AlertDialogTool.buildCustomStylePopupDialogGravity(activity, v, Gravity.BOTTOM, R.style.DialogBottomPopup, false);
    dialog.show();

    Button btn_ok = v.findViewById(R.id.btn_ok);
    Button btn_close = v.findViewById(R.id.btn_close);
    Button btn_cancel = v.findViewById(R.id.btn_cancel);
    TextView text_base = v.findViewById(R.id.text_base);
    TextView text_title = v.findViewById(R.id.text_title);

    if(onAgreementCloseListener!=null) {
      btn_ok.setOnClickListener(view -> { dialog.dismiss(); onAgreementCloseListener.onAgreementClose(true); });
      btn_cancel.setOnClickListener(view -> { dialog.dismiss(); onAgreementCloseListener.onAgreementClose(false); });
      text_base.setText(R.string.text_agreement_base_welecome);
    }else {
      btn_ok.setVisibility(View.GONE);
      btn_cancel.setVisibility(View.GONE);
      btn_close.setVisibility(View.VISIBLE);
      btn_close.setOnClickListener(view -> dialog.dismiss());
      text_base.setText(R.string.text_agreement_base);
      text_title.setText(R.string.settings_key_privacy_policy);
    }

    text_base.setMovementMethod(LinkMovementMethod.getInstance()); ;

  }

}
