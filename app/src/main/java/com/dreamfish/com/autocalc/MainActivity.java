package com.dreamfish.com.autocalc;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamfish.com.autocalc.core.AutoCalc;
import com.dreamfish.com.autocalc.core.AutoCalcException;
import com.dreamfish.com.autocalc.database.CalcHistoryDatabaseHelper;
import com.dreamfish.com.autocalc.database.CalcHistoryDbSchema;
import com.dreamfish.com.autocalc.utils.AlertDialogTool;
import com.dreamfish.com.autocalc.utils.PixelTool;
import com.dreamfish.com.autocalc.widgets.AutofitTextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private final int RESULT_SETTING_ACTIVITY = 0;
    private final String TAG = "MainActivity";

    private final int PAD_MODE_NORMAL = 0;
    private final int PAD_MODE_SCIENCE = 1;
    private final int PAD_MODE_PROGRAMMER = 2;

    private final int BC_MODE_DEC = 0;
    private final int BC_MODE_BIN = 1;
    private final int BC_MODE_HEX = 2;
    private final int BC_MODE_OCT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSettings();
        initHistory();

        layout_root = findViewById(R.id.layout_root);
        layout_top = findViewById(R.id.layout_top);
        LinearLayout layout_bottom = findViewById(R.id.layout_bottom);

        autoCalc = new AutoCalc();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        resources = this.getResources();
        text_error = resources.getString(R.string.text_error);
        text_auto_bc_error = (String) resources.getText(R.string.text_auto_bc_error);

        layout_top.setVerticalFadingEdgeEnabled(true);
        layout_top.setFadingEdgeLength(50);

        layout_root.post(() -> {
            initControls();
            initLayout();

            update2rnd();
            updateDegRad();
            updateBinaryConversionMode();
            updateCalcSettings();

            loadHistory();

            layout_top.postDelayed(() -> {
                layout_top.fullScroll(ScrollView.FOCUS_DOWN);
                inited = true;
            }, 400);
        });
    }

    private boolean inited = false;

    private LinearLayout layout_root;
    private ScrollView layout_top;
    private LinearLayout layout_history;
    private LinearLayout layout_binary_conversion;

    private Resources resources;

    private String text_error;
    private String text_auto_bc_error;

    private Button btn_pad_ac;
    private Button btn_pad_dot;
    private Button btn_pad_sqrt;
    private Button btn_sin;
    private Button btn_2nd;
    private Button btn_cos;
    private Button btn_tan;

    private Button btn_deg_rad;

    private AutofitTextView text_main;
    private AutofitTextView text_main_pre_result;

    private TextView text_oct;
    private TextView text_bin;
    private TextView text_hex;
    private TextView text_dec;

    private RadioButton radio_binary_conversion_hex;
    private RadioButton radio_binary_conversion_bin;
    private RadioButton radio_binary_conversion_oct;
    private RadioButton radio_binary_conversion_dec;

    private Vibrator vibrator;
    private AutoCalc autoCalc;

    private List<String> calcHistory = new ArrayList<>();
    private int calcHistoryMaxCount = 0;
    private int calcScale = 8;
    private int padMode = PAD_MODE_NORMAL;
    private int bcMode = BC_MODE_DEC;
    private boolean is2rnd = false;
    private boolean recordStep = false;
    private boolean isdeg = true;
    private boolean useTouchVibrator = true;

    private void initControls() {
        final AnimationDrawable frame_animation_btn_switch = (AnimationDrawable)resources.getDrawable(R.drawable.frame_animation_btn_switch, null);
        final Drawable btn_pad_switch = resources.getDrawable(R.drawable.btn_pad_switch, null);

        Button btn_pad_expand_collapse = findViewById(R.id.btn_pad_expand_collapse);
        btn_pad_expand_collapse.setOnClickListener(v -> {

            if(padMode == PAD_MODE_NORMAL) padMode = PAD_MODE_SCIENCE;
            else if(padMode == PAD_MODE_SCIENCE) padMode = PAD_MODE_NORMAL;
            else if(padMode == PAD_MODE_PROGRAMMER) padMode = PAD_MODE_NORMAL;

            initLayout();
            clearText();
            setBinaryConversionMode(BC_MODE_DEC);

            btn_pad_expand_collapse.setForeground(frame_animation_btn_switch);
            frame_animation_btn_switch.start();
            btn_pad_expand_collapse.postDelayed(() -> {
                frame_animation_btn_switch.stop();
                btn_pad_expand_collapse.setForeground(btn_pad_switch);
            }, 2000);
        });

        layout_binary_conversion = findViewById(R.id.layout_binary_conversion);
        layout_history = findViewById(R.id.layout_history);

        text_main = findViewById(R.id.text_main);
        text_main_pre_result = findViewById(R.id.text_main_pre_result);
        text_main.setLines(1);
        text_main.setMaxLines(3);
        text_main.setSizeToFit(true);
        text_main_pre_result.setLines(1);
        text_main_pre_result.setMaxLines(5);
        text_main_pre_result.setSizeToFit(true);

        text_oct = findViewById(R.id.text_oct);
        text_bin = findViewById(R.id.text_bin);
        text_hex = findViewById(R.id.text_hex);
        text_dec = findViewById(R.id.text_dec);

        radio_binary_conversion_hex = findViewById(R.id.radio_binary_conversion_hex);
        radio_binary_conversion_bin = findViewById(R.id.radio_binary_conversion_bin);
        radio_binary_conversion_oct = findViewById(R.id.radio_binary_conversion_oct);
        radio_binary_conversion_dec = findViewById(R.id.radio_binary_conversion_dec);

        radio_binary_conversion_hex.setOnClickListener((v) -> {
            bcMode = BC_MODE_HEX;
            updateBinaryConversionMode();
        });
        radio_binary_conversion_bin.setOnClickListener((v) -> {
            bcMode = BC_MODE_BIN;
            updateBinaryConversionMode();
        });
        radio_binary_conversion_oct.setOnClickListener((v) -> {
            bcMode = BC_MODE_OCT;
            updateBinaryConversionMode();
        });
        radio_binary_conversion_dec.setOnClickListener((v) -> {
            bcMode = BC_MODE_DEC;
            updateBinaryConversionMode();
        });

        btn_deg_rad = findViewById(R.id.btn_deg_rad);
        btn_pad_dot = findViewById(R.id.btn_pad_dot);
        btn_pad_sqrt = findViewById(R.id.btn_sqrt);
        btn_pad_ac = findViewById(R.id.btn_pad_ac);
        btn_sin = findViewById(R.id.btn_sin);
        btn_cos = findViewById(R.id.btn_cos);
        btn_tan = findViewById(R.id.btn_tan);
        btn_2nd = findViewById(R.id.btn_2nd);

        findViewById(R.id.btn_pad_number_0).setOnClickListener(v -> writeText("0", false, false));
        findViewById(R.id.btn_pad_number_1).setOnClickListener(v -> writeText("1", false, false));
        findViewById(R.id.btn_pad_number_2).setOnClickListener(v -> writeText("2", false, false));
        findViewById(R.id.btn_pad_number_3).setOnClickListener(v -> writeText("3", false, false));
        findViewById(R.id.btn_pad_number_4).setOnClickListener(v -> writeText("4", false, false));
        findViewById(R.id.btn_pad_number_5).setOnClickListener(v -> writeText("5", false, false));
        findViewById(R.id.btn_pad_number_6).setOnClickListener(v -> writeText("6", false, false));
        findViewById(R.id.btn_pad_number_7).setOnClickListener(v -> writeText("7", false, false));
        findViewById(R.id.btn_pad_number_8).setOnClickListener(v -> writeText("8", false, false));
        findViewById(R.id.btn_pad_number_9).setOnClickListener(v -> writeText("9", false, false));
        findViewById(R.id.btn_pad_number_A).setOnClickListener(v -> writeText("A", false, false));
        findViewById(R.id.btn_pad_number_B).setOnClickListener(v -> writeText("B", false, false));
        findViewById(R.id.btn_pad_number_C).setOnClickListener(v -> writeText("C", false, false));
        findViewById(R.id.btn_pad_number_D).setOnClickListener(v -> writeText("D", false, false));
        findViewById(R.id.btn_pad_number_E).setOnClickListener(v -> writeText("E", false, false));
        findViewById(R.id.btn_pad_number_F).setOnClickListener(v -> writeText("F", false, false));

        findViewById(R.id.btn_and).setOnClickListener(v -> writeText(" and ", true, false));
        findViewById(R.id.btn_or).setOnClickListener(v -> writeText(" or ", true, false));
        findViewById(R.id.btn_xor).setOnClickListener(v -> writeText(" xor ", true, false));
        findViewById(R.id.btn_not).setOnClickListener(v -> writeText(" not ", true, true));

        findViewById(R.id.btn_negate).setOnClickListener(v -> writeText("negate(", true, true));

        findViewById(R.id.btn_left_shift).setOnClickListener(v -> writeText(" << ", true, false));
        findViewById(R.id.btn_right_shift).setOnClickListener(v -> writeText(" >> ", true, false));
        findViewById(R.id.btn_right_shift_unsigned).setOnClickListener(v -> writeText(" >>> ", true, false));

        findViewById(R.id.btn_rpc).setOnClickListener(v -> writeText("1/", true, true));
        findViewById(R.id.btn_pad_del).setOnClickListener(v -> delText());
        findViewById(R.id.btn_pad_minus).setOnClickListener(v -> writeText("-", true, false));
        findViewById(R.id.btn_pad_div).setOnClickListener(v -> writeText("÷", true, false));
        findViewById(R.id.btn_pad_mul).setOnClickListener(v -> writeText("×", true, false));
        findViewById(R.id.btn_pad_plus).setOnClickListener(v -> writeText("+", true, false));
        findViewById(R.id.btn_pad_percent).setOnClickListener(v -> writeText(padMode == PAD_MODE_PROGRAMMER ? " mod " : "%", true, false));


        findViewById(R.id.btn_fac).setOnClickListener(v -> writeText("!", true, false));
        findViewById(R.id.btn_pi).setOnClickListener(v -> writeText("π", false, false));
        findViewById(R.id.btn_e).setOnClickListener(v -> writeText("е", false, false));
        findViewById(R.id.btn_pow).setOnClickListener(v -> writeText("^", true, false));
        findViewById(R.id.btn_lg).setOnClickListener(v -> writeText("lg(", true, true));
        findViewById(R.id.btn_ln).setOnClickListener(v -> writeText("ln(", true, true));
        findViewById(R.id.btn_left_p).setOnClickListener(v -> writeText("(", true, true));
        findViewById(R.id.btn_right_p).setOnClickListener(v -> writeText(")", false, false));
        findViewById(R.id.btn_left_p_2).setOnClickListener(v -> writeText("(", true, true));
        findViewById(R.id.btn_right_p_2).setOnClickListener(v -> writeText(")", false, false));

        btn_pad_sqrt.setOnClickListener(v -> writeText(is2rnd ? "∛" : "√", true, true));
        btn_2nd.setOnClickListener(v -> switch2rnd());
        btn_pad_ac.setOnClickListener(v -> clearTextOrLog());
        btn_deg_rad.setOnClickListener(v -> switchDegRad());

        btn_sin.setOnClickListener(v -> { if(is2rnd) writeText("arcsin(", true, true); else writeText("sin(", true, true); });
        btn_cos.setOnClickListener(v -> { if(is2rnd) writeText("arccos(", true, true); else writeText("cos(", true, true); });
        btn_tan.setOnClickListener(v -> { if(is2rnd) writeText("arctan(", true, true); else writeText("tan(", true, true); });
        btn_pad_dot.setOnClickListener(v -> writeText(is2rnd ? "," : ".", false, true));

        findViewById(R.id.btn_pad_equal).setOnClickListener(v -> { vibratorVibrate(); doCalc(); });
    }
    private void initLayout() {

        layout_root.measure(0,0);
        int width = layout_root.getWidth();
        int height = layout_root.getHeight();

        int colCount = 4, rowCount = 5;

        if(padMode == PAD_MODE_SCIENCE || padMode == PAD_MODE_PROGRAMMER) {
            colCount = 5;
            rowCount = 7;
        }

        updateBinaryConversionMode();

        layout_binary_conversion.setVisibility(padMode == PAD_MODE_PROGRAMMER ? View.VISIBLE : View.GONE);

        findViewById(R.id.layout_row_1_programmer).setVisibility(padMode == PAD_MODE_PROGRAMMER ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_row_2_programmer).setVisibility(padMode == PAD_MODE_PROGRAMMER ? View.VISIBLE : View.GONE);

        findViewById(R.id.layout_row_1).setVisibility(padMode == PAD_MODE_SCIENCE ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_row_2).setVisibility(padMode == PAD_MODE_SCIENCE ? View.VISIBLE : View.GONE);

        findViewById(R.id.btn_pad_dot).setVisibility(padMode == PAD_MODE_PROGRAMMER ? View.GONE : View.VISIBLE);
        findViewById(R.id.btn_pad_number_E).setVisibility(padMode == PAD_MODE_PROGRAMMER ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_pad_number_F).setVisibility(padMode == PAD_MODE_PROGRAMMER ? View.VISIBLE : View.GONE);

        int margin = padMode == PAD_MODE_SCIENCE || padMode == PAD_MODE_PROGRAMMER ? 20 : 26;
        int btnSize = width / colCount - margin * 2;

        LinearLayout []rows = new LinearLayout[9];
        rows[0] = findViewById(R.id.layout_row_1);
        rows[1] = findViewById(R.id.layout_row_2);
        rows[2] = findViewById(R.id.layout_row_3);
        rows[3] = findViewById(R.id.layout_row_4);
        rows[4] = findViewById(R.id.layout_row_5);
        rows[5] = findViewById(R.id.layout_row_6);
        rows[6] = findViewById(R.id.layout_row_7);
        rows[7] = findViewById(R.id.layout_row_1_programmer);
        rows[8] = findViewById(R.id.layout_row_2_programmer);

        for (int i = 0; i < rows.length; i++) {
            LinearLayout.LayoutParams lp3 = (LinearLayout.LayoutParams) rows[i].getLayoutParams();
            lp3.width = LinearLayout.LayoutParams.MATCH_PARENT;
            lp3.height = btnSize;
            rows[i].setLayoutParams(lp3);
            for (int j = 0; j < rows[i].getChildCount(); j++) {
                View b = rows[i].getChildAt(j);
                if (j == 0 && i < 7)
                    b.setVisibility(padMode == PAD_MODE_SCIENCE ? View.VISIBLE : View.GONE);
                else if (j == 1 && i >= 2 && i < 6)
                    b.setVisibility(padMode == PAD_MODE_PROGRAMMER ? View.VISIBLE : View.GONE);

                lp3 = (LinearLayout.LayoutParams) b.getLayoutParams();
                lp3.width = btnSize;
                lp3.height = ViewGroup.LayoutParams.MATCH_PARENT;
                lp3.setMargins(margin, 0, margin, 0);

                b.setLayoutParams(lp3);
            }
        }

        ViewGroup.LayoutParams lp = layout_top.getLayoutParams();
        lp.height = (height - rowCount * btnSize);
        layout_top.setLayoutParams(lp);
        layout_history.setMinimumHeight(lp.height - PixelTool.dpToPx(this, 80));

        layout_root.requestLayout();

        //Log.d(TAG, "padWidth = " + padWidth + ",padHeight = " + padHeight);
        //Log.d(TAG, "btnWidth = " + btnWidth + ",btnHeight = " + btnHeight);
    }

    //Text
    private StringBuilder textBuffer = new StringBuilder("0");
    private boolean isCalced = false;
    private boolean isCalcAndError = false;

    private void vibratorVibrate() {
        if(inited && useTouchVibrator) vibrator.vibrate(30);
    }

    //switch modes
    private void switch2rnd() {
        is2rnd = !is2rnd;
        vibratorVibrate();
        update2rnd();
    }
    private void switchDegRad() {
        isdeg = !isdeg;
        vibratorVibrate();
        updateDegRad();

        //提示
        if(isdeg) Toast.makeText(this, resources.getText(R.string.text_use_deg), Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, resources.getText(R.string.text_use_rad), Toast.LENGTH_SHORT).show();
    }

    //Text input control
    private void clearText() {
        isCalcAndError = false;
        textBuffer = new StringBuilder("0");
        writeText("0", false, false);
    }
    private void clearTextOrLog() {
        if(textBuffer.toString().equals("0")) clearCalcLog();
        else clearText();
    }
    private void writeText(String s, boolean isOp, boolean isLeft) {
        vibratorVibrate();
        if(textBuffer.toString().equals("0")
                || isCalcAndError
                || (isCalced && !isOp)) textBuffer = new StringBuilder(s);
        else {
            if(isLeft && autoCalc.testIsNumber(textBuffer.toString())) {
                textBuffer.insert(0, s);
                if(s.endsWith("("))
                    textBuffer.append( ")");
            }
            else textBuffer.append(s);
        }
        isCalcAndError = false;
        isCalced = false;
        updateText();
    }
    private void delText() {
        vibratorVibrate();
        if(textBuffer.length() > 0)
            textBuffer.deleteCharAt(textBuffer.length() - 1);
        if(textBuffer.length() == 0)
            textBuffer.append("0");
        updateText();
    }

    //update ui
    private void updateText() {

        if(textBuffer.toString().equals("0"))
            btn_pad_ac.setForeground(resources.getDrawable(R.drawable.btn_pad_ac, null));
        else
            btn_pad_ac.setForeground(resources.getDrawable(R.drawable.btn_pad_c, null));

        preCalc();

        text_main.setText(textBuffer.toString());

        if(padMode == PAD_MODE_PROGRAMMER)
            updateBinaryConversionTexts();

        layout_top.postDelayed(() -> layout_top.fullScroll(ScrollView.FOCUS_DOWN), 300);
    }
    private void update2rnd() {
        if(is2rnd) {
            btn_sin.setForeground(resources.getDrawable(R.drawable.arcsin, null));
            btn_cos.setForeground(resources.getDrawable(R.drawable.arccos, null));
            btn_tan.setForeground(resources.getDrawable(R.drawable.arctan, null));
            btn_pad_dot.setForeground(resources.getDrawable(R.drawable.comma, null));
            btn_pad_sqrt.setForeground(resources.getDrawable(R.drawable.btn_cbrt, null));
            btn_2nd.setForeground(resources.getDrawable(R.drawable.btn_2nd_active, null));
        }else{
            btn_sin.setForeground(resources.getDrawable(R.drawable.sin, null));
            btn_cos.setForeground(resources.getDrawable(R.drawable.cos, null));
            btn_tan.setForeground(resources.getDrawable(R.drawable.tan, null));
            btn_pad_dot.setForeground(resources.getDrawable(R.drawable.btn_pad_dot, null));
            btn_pad_sqrt.setForeground(resources.getDrawable(R.drawable.btn_sqrt, null));
            btn_2nd.setForeground(resources.getDrawable(R.drawable.btn_2nd, null));
        }
    }
    private void updateDegRad() {
        autoCalc.setUseDegree(isdeg);
        if(isdeg)
            btn_deg_rad.setForeground(resources.getDrawable(R.drawable.deg, null));
        else
            btn_deg_rad.setForeground(resources.getDrawable(R.drawable.rad, null));
    }
    private void updateBinaryConversionTexts() {
        String text = textBuffer.toString();
        if(autoCalc.testIsNumber(text)) {
            try {
                BigDecimal n = autoCalc.getTools().strToNumber(text);
                if (autoCalc.getTools().checkNumberRange(n, "long")) {
                    text_bin.setText(Long.toBinaryString(n.longValue()));
                    text_oct.setText(Long.toOctalString(n.longValue()));
                    text_hex.setText(Long.toHexString(n.longValue()));
                    text_dec.setText(String.valueOf(n.longValue()));
                } else {
                    text_bin.setText(text_auto_bc_error);
                    text_oct.setText(text_auto_bc_error);
                    text_hex.setText(text_auto_bc_error);
                    text_dec.setText(text_auto_bc_error);
                }
            }catch (Exception e) {
                text_bin.setText(text_auto_bc_error);
                text_oct.setText(text_auto_bc_error);
                text_hex.setText(text_auto_bc_error);
                text_dec.setText(text_auto_bc_error);
            }
        }else {
            text_bin.setText("");
            text_oct.setText("");
            text_hex.setText("");
            text_dec.setText("");
        }
    }
    private void updateBinaryConversionMode() {
        if(padMode == PAD_MODE_PROGRAMMER) {

            clearText();
            autoCalc.setBcMode(bcMode);

            radio_binary_conversion_bin.setChecked(false);
            radio_binary_conversion_oct.setChecked(false);
            radio_binary_conversion_dec.setChecked(false);
            radio_binary_conversion_hex.setChecked(false);

            findViewById(R.id.btn_pad_number_2).setEnabled(bcMode != BC_MODE_BIN);
            findViewById(R.id.btn_pad_number_3).setEnabled(bcMode != BC_MODE_BIN);
            findViewById(R.id.btn_pad_number_4).setEnabled(bcMode != BC_MODE_BIN);
            findViewById(R.id.btn_pad_number_5).setEnabled(bcMode != BC_MODE_BIN);
            findViewById(R.id.btn_pad_number_6).setEnabled(bcMode != BC_MODE_BIN);
            findViewById(R.id.btn_pad_number_7).setEnabled(bcMode != BC_MODE_BIN);
            findViewById(R.id.btn_pad_number_8).setEnabled(bcMode != BC_MODE_BIN && bcMode != BC_MODE_OCT);
            findViewById(R.id.btn_pad_number_9).setEnabled(bcMode != BC_MODE_BIN && bcMode != BC_MODE_OCT);

            findViewById(R.id.btn_pad_number_A).setEnabled(bcMode == BC_MODE_HEX);
            findViewById(R.id.btn_pad_number_B).setEnabled(bcMode == BC_MODE_HEX);
            findViewById(R.id.btn_pad_number_C).setEnabled(bcMode == BC_MODE_HEX);
            findViewById(R.id.btn_pad_number_D).setEnabled(bcMode == BC_MODE_HEX);
            findViewById(R.id.btn_pad_number_E).setEnabled(bcMode == BC_MODE_HEX);
            findViewById(R.id.btn_pad_number_F).setEnabled(bcMode == BC_MODE_HEX);

            switch (bcMode) {
                case BC_MODE_BIN:
                    radio_binary_conversion_bin.setChecked(true);
                    break;
                case BC_MODE_OCT:
                    radio_binary_conversion_oct.setChecked(true);
                    break;
                case BC_MODE_DEC:
                    radio_binary_conversion_dec.setChecked(true);
                    break;
                case BC_MODE_HEX:
                    radio_binary_conversion_hex.setChecked(true);
                    break;
            }
        }
        else {
            findViewById(R.id.btn_pad_number_2).setEnabled(true);
            findViewById(R.id.btn_pad_number_3).setEnabled(true);
            findViewById(R.id.btn_pad_number_4).setEnabled(true);
            findViewById(R.id.btn_pad_number_5).setEnabled(true);
            findViewById(R.id.btn_pad_number_6).setEnabled(true);
            findViewById(R.id.btn_pad_number_7).setEnabled(true);
            findViewById(R.id.btn_pad_number_8).setEnabled(true);
            findViewById(R.id.btn_pad_number_9).setEnabled(true);
        }
    }
    private void updateCalcSettings() {
        autoCalc.setRecordStep(recordStep);
        autoCalc.setNumberScale(calcScale);
        autoCalc.setUseDegree(isdeg);
    }

    private void setBinaryConversionMode(int mode) {
        bcMode = mode;
        autoCalc.setBcMode(bcMode);
        updateBinaryConversionMode();
    }

    //calc and log
    private void clearCalcLog() {
        layout_history.removeAllViews();
        layout_history.invalidate();

        //claer db
        calcHistorySqLiteDatabaseWrite.execSQL("DELETE FROM " + CalcHistoryDbSchema.CalcHistoryDTable.NAME + ";");

        calcHistory.clear();
        vibratorVibrate();

        //Toast
        Toast.makeText(this, resources.getText(R.string.text_history_cleared), Toast.LENGTH_SHORT).show();
    }
    private void addCalcLog(String formula) {
        if(calcHistory.size() > calcHistoryMaxCount) {
            calcHistory.remove(0);

            //Delete in db
            calcHistorySqLiteDatabaseWrite.delete(CalcHistoryDbSchema.CalcHistoryDTable.NAME,"formula = ?",new String []{ formula });
        }
        int index = calcHistory.indexOf(formula);
        if(index != calcHistory.size() - 1) {
            calcHistory.add(formula);

            //Add to db
            ContentValues contentValues = new ContentValues(); contentValues.put("formula", formula);
            calcHistorySqLiteDatabaseWrite.insert(CalcHistoryDbSchema.CalcHistoryDTable.NAME,null, contentValues);

            TextView textView = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams);
            textView.setPadding(8, 0, 8, 3);
            textView.setText(formula);
            textView.setTextColor(getResources().getColor(R.color.show_screen_history_color, null));
            textView.setTextSize(18);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            textView.setTextIsSelectable(true);

            layout_history.addView(textView);
        }
    }

    private String preSolveParentheses(boolean isPre) {
        StringBuilder textBufferNew = isPre ? new StringBuilder(textBuffer) : textBuffer;
        String formula = textBufferNew.toString();
        int leftPos = formula.indexOf('(');
        if(leftPos >= 0 && leftPos < formula.length() - 1 && !formula.contains(")")) {
            String formulaRight = formula.substring(leftPos + 1);
            if(autoCalc.testIsNumber(formulaRight)) {
                textBufferNew.append(')');
                formula = textBufferNew.toString();
            }
        }
        return formula;
    }
    private void preCalc() {
        String formula = preSolveParentheses(true);
        if(!formula.equals("") && !formula.equals("0")) {
            if (!(autoCalc.isOperator(formula.charAt(formula.length() - 1), AutoCalc.OP_TYPE_BOTH)
                || autoCalc.isOperator(formula.charAt(formula.length() - 1), AutoCalc.OP_TYPE_START))
                    && formula.charAt(formula.length() - 1) != '(') {
                text_main_pre_result.setVisibility(View.VISIBLE);
                String result = autoCalc.calc(formula);
                StringBuilder sb = new StringBuilder("=");

                if (autoCalc.isLastSuccess()) sb.append(result);
                else {
                    if (!autoCalc.isOperatorOrParentheses(formula.charAt(formula.length() - 1))
                            && formula.length() >= 3) sb.append(text_error);
                    else sb.deleteCharAt(0);
                }

                text_main_pre_result.setText(sb.toString());
                autoCalc.resetLastSuccess();
            }else text_main_pre_result.setText("");
        }else text_main_pre_result.setVisibility(View.GONE);
    }
    private void doCalc() {
        String result;
        String formula = preSolveParentheses(false);

        if(formula.equals("0")) return;
        if(isCalcAndError) return;

        result = autoCalc.calc(formula);
        addCalcLog(formula + "=" + (autoCalc.isLastSuccess() ? result : text_error));
        textBuffer = new StringBuilder(result);
        text_main_pre_result.setVisibility(View.GONE);
        text_main.setText(result);
        updateBinaryConversionTexts();

        isCalcAndError = !autoCalc.isLastSuccess();
        isCalced = true;

        if(isCalcAndError) {
            Exception e = autoCalc.getLastException();
            if(e != null && !(e instanceof AutoCalcException))
                showException(e);
        }
    }

    //主列表数据\适配器
    class FunctionsListViewHolder {
        TextView textViewTitle;
        TextView textViewExplain;
    }
    class FunctionsListItem {
        String title;
        String explain;

        FunctionsListItem(String title, String explain) {
            this.title = title;
            this.explain = explain;
        }
    }
    class FunctionsListAdapter extends ArrayAdapter<FunctionsListItem> {

        private int layoytId;

        FunctionsListAdapter(Context context, int layoutId, List<FunctionsListItem> list) {
            super(context, layoutId, list);
            this.layoytId = layoutId;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final FunctionsListItem item = getItem(position);

            FunctionsListViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(layoytId, parent, false);
                viewHolder = new FunctionsListViewHolder();
                viewHolder.textViewTitle = convertView.findViewById(R.id.text_title);
                viewHolder.textViewExplain = convertView.findViewById(R.id.text_explan);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (FunctionsListViewHolder) convertView.getTag();
            }

            if(item != null) {
                viewHolder.textViewExplain.setText(item.explain);
                viewHolder.textViewTitle.setText(item.title);
            }

            return convertView;
        }
    }

    //show dialogs
    private void showAbout()  {

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View v = inflater.inflate(R.layout.dialog_about, null);

        AlertDialog dialog = AlertDialogTool.buildCustomBottomPopupDialog(this, v);
        dialog.show();

        v.findViewById(R.id.btn_ok).setOnClickListener(view -> dialog.dismiss());
    }
    private void chooseMode() {

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View v = inflater.inflate(R.layout.dialog_choose_mode, null);

        AlertDialog dialog = AlertDialogTool.buildCustomBottomPopupDialog(this, v);

        Button btn_close = v.findViewById(R.id.btn_cancel);
        Button btn_mode_normal = v.findViewById(R.id.btn_mode_normal);
        Button btn_mode_expanded = v.findViewById(R.id.btn_mode_expanded);
        Button btn_mode_programmer = v.findViewById(R.id.btn_mode_programmer);

        switch (padMode) {
            case PAD_MODE_NORMAL:
                btn_mode_normal.setBackground(resources.getDrawable(R.drawable.btn_calculate, null));
                btn_mode_normal.setTextColor(Color.WHITE);
                break;
            case PAD_MODE_SCIENCE:
                btn_mode_expanded.setBackground(resources.getDrawable(R.drawable.btn_calculate, null));
                btn_mode_expanded.setTextColor(Color.WHITE);
                break;
            case PAD_MODE_PROGRAMMER:
                btn_mode_programmer.setBackground(resources.getDrawable(R.drawable.btn_calculate, null));
                btn_mode_programmer.setTextColor(Color.WHITE);
                break;
        }

        btn_close.setOnClickListener(view -> dialog.dismiss());
        btn_mode_normal.setOnClickListener(view -> {
            dialog.dismiss();
            padMode = PAD_MODE_NORMAL;
            setBinaryConversionMode(BC_MODE_DEC);
            initLayout();
        });
        btn_mode_expanded.setOnClickListener(view -> {
            dialog.dismiss();
            padMode = PAD_MODE_SCIENCE;
            setBinaryConversionMode(BC_MODE_DEC);
            initLayout();
        });
        btn_mode_programmer.setOnClickListener(view -> {
            dialog.dismiss();
            padMode = PAD_MODE_PROGRAMMER;
            initLayout();
        });

        dialog.show();
    }
    private void showCustomerView() {

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View v = inflater.inflate(R.layout.dialog_customer_input, null);

        AlertDialog dialog = AlertDialogTool.buildCustomBottomPopupDialog(this, v);

        EditText edit_inpurt = v.findViewById(R.id.edit_inpurt);
        edit_inpurt.setText(textBuffer.toString());

        v.findViewById(R.id.btn_cancel).setOnClickListener(view -> dialog.dismiss());
        v.findViewById(R.id.btn_ok).setOnClickListener(view -> {
            dialog.dismiss();
            textBuffer = new StringBuilder(edit_inpurt.getText());
            doCalc();
        });
        v.findViewById(R.id.btn_clear).setOnClickListener(view -> edit_inpurt.setText(""));

        dialog.show();
    }
    private void showAllFunctionsHelp() {

        final List<FunctionsListItem> functionsListItems = new ArrayList<>();
        final FunctionsListAdapter functionsListAdapter = new FunctionsListAdapter(this, R.layout.item_function, functionsListItems);

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View v = inflater.inflate(R.layout.dialog_funs_list, null);

        AlertDialog dialog = AlertDialogTool.buildCustomBottomPopupDialog(this, v);

        ListView list_all_functions = v.findViewById(R.id.list_all_functions);

        //初始化所有函数信息
        String[] functions_help_names = resources.getStringArray(R.array.functions_help_names);
        String[] functions_help_texts = resources.getStringArray(R.array.functions_help_texts);
        for (int i = 0, c = functions_help_names.length; i < c; i++)
            functionsListItems.add(new FunctionsListItem(functions_help_names[i], functions_help_texts[i]));

        functionsListAdapter.notifyDataSetChanged();

        list_all_functions.setAdapter(functionsListAdapter);
        list_all_functions.setOnItemClickListener((parent, view, position, id) -> {
            String name = functionsListItems.get(position).title;
            writeText(name.substring(0, name.indexOf("(") + 1), true, true);
            dialog.dismiss();
        });

        v.findViewById(R.id.btn_cancel).setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }
    private void showException(Exception e) {
        AlertDialog dialog = AlertDialogTool.buildBottomPopupDialogBuilder(this)
                .setTitle(R.string.title_calc_exception)
                .setMessage(e.toString())
                .setPositiveButton(R.string.text_i_new, (dialog1, which) -> dialog1.dismiss())
                .create();

        dialog.show();
    }
    private void showCalcStep() {
        if(!recordStep) Toast.makeText(this,
                resources.getString(R.string.text_please_open_record_step), Toast.LENGTH_LONG).show();
        else {
            final List<FunctionsListItem> functionsListItems = new ArrayList<>();
            final FunctionsListAdapter functionsListAdapter = new FunctionsListAdapter(this, R.layout.item_function, functionsListItems);

            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View v = inflater.inflate(R.layout.dialog_calc_step_list, null);

            AlertDialog dialog = AlertDialogTool.buildCustomBottomPopupDialog(this, v);

            TextView text_current_formula = v.findViewById(R.id.text_current_formula);
            TextView text_empty = v.findViewById(R.id.text_empty);
            ListView list_all_functions = v.findViewById(R.id.list_all_functions);

            //初始化所有函数信息
            List<String> steps = autoCalc.getLastCalcSteps();
            for(String step : steps)
                functionsListItems.add(new FunctionsListItem(step, ""));

            functionsListAdapter.notifyDataSetChanged();

            list_all_functions.setEmptyView(text_empty);
            list_all_functions.setAdapter(functionsListAdapter);
            text_current_formula.setText(autoCalc.getLastFormula());

            v.findViewById(R.id.btn_cancel).setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        }
    }

    //退出提示
    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //首先判断用户有没有按下返回键
        if (keyCode== KeyEvent.KEYCODE_BACK){
            //判断用户按下的时间是不是大于2秒，如果大于2秒则认为是失误操作
            if ((System.currentTimeMillis()-mExitTime)>2000){
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime=System.currentTimeMillis();//记住当前时间，下次再按返回键时做对比
            }else {
                finish();
            }
            return true;//返回true不在往下运行
        }
        return super.onKeyDown(keyCode, event);
    }
    //菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                finish();
                break;
            case R.id.action_help:
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
                break;
            case R.id.action_settings:
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), RESULT_SETTING_ACTIVITY);
                break;
            case R.id.action_about:
                showAbout();
                break;
            case R.id.action_show_functions:
                showAllFunctionsHelp();
                break;
            case R.id.action_choose_mode:
                chooseMode();
                break;
            case R.id.action_custom_input:
                showCustomerView();
                break;
            case R.id.action_show_calc_step:
                showCalcStep();
                break;
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SETTING_ACTIVITY) {
            loadSettings();
            updateCalcSettings();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //关闭
    @Override
    protected void onDestroy() {
        saveHistory();
        saveSettings();
        super.onDestroy();
    }

    //设置
    private void loadSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        recordStep = prefs.getBoolean("calc_record_step", false);
        padMode = prefs.getInt("calc_pad_expand", PAD_MODE_NORMAL);
        bcMode = prefs.getInt("calc_bc_mode", BC_MODE_DEC);
        is2rnd = prefs.getBoolean("calc_use_2rnd", false);
        isdeg = prefs.getBoolean("calc_use_deg", true);
        useTouchVibrator = prefs.getBoolean("calc_use_vibrator", true);
        calcHistoryMaxCount = prefs.getInt("calc_history_count", 20);
        calcScale = prefs.getInt("calc_computation_accuracy", 8);
    }
    private void saveSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("calc_pad_expand", padMode);
        editor.putInt("calc_bc_mode", bcMode);
        editor.putBoolean("calc_use_2rnd", is2rnd);
        editor.putBoolean("calc_use_deg", isdeg);
        editor.putInt("calc_history_count", calcHistoryMaxCount);

        editor.apply();
    }

    //存储数据 历史记录

    private SQLiteDatabase calcHistorySqLiteDatabaseRead;
    private SQLiteDatabase calcHistorySqLiteDatabaseWrite;

    private void initHistory() {
        CalcHistoryDatabaseHelper calcHistoryDatabaseHelper = new CalcHistoryDatabaseHelper(this);
        calcHistorySqLiteDatabaseRead = calcHistoryDatabaseHelper.getReadableDatabase();
        calcHistorySqLiteDatabaseWrite = calcHistoryDatabaseHelper.getWritableDatabase();
    }
    private void loadHistory() {
        Cursor cursor = calcHistorySqLiteDatabaseRead.query(CalcHistoryDbSchema.CalcHistoryDTable.NAME,
                CalcHistoryDbSchema.CalcHistoryDTable.COLS,null,
                null,null,null,null);
        try{
            while (cursor != null && cursor.moveToNext()) {
                addCalcLog(cursor.getString(cursor.getColumnIndex(CalcHistoryDbSchema.CalcHistoryDTable.Cols.FORMULA)));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Log.e(TAG,"queryData exception", e);
        }
    }
    private void saveHistory() {

    }
}
