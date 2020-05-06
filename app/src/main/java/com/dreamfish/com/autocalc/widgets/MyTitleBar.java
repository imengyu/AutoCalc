package com.dreamfish.com.autocalc.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dreamfish.com.autocalc.R;

import androidx.constraintlayout.widget.ConstraintLayout;

public class MyTitleBar extends ConstraintLayout {

  private Button ivBack;
  private TextView tvTitle;
  private TextView tvMore;
  private Button ivMore;

  public MyTitleBar(Context context, AttributeSet attrs) {
    super(context, attrs);

    initView(context,attrs);
  }

  //初始化视图
  private void initView(final Context context, AttributeSet attributeSet) {
    View inflate = LayoutInflater.from(context).inflate(R.layout.layout_titlebar, this);
    ivBack = inflate.findViewById(R.id.btn_back);
    tvTitle = inflate.findViewById(R.id.text_title);
    tvMore = inflate.findViewById(R.id.text_more);
    ivMore = inflate.findViewById(R.id.btn_more);

    init(context,attributeSet);
  }

  //初始化资源文件
  public void init(Context context, AttributeSet attributeSet){
    TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MyTitleBar);
    String title = typedArray.getString(R.styleable.MyTitleBar_title);//标题
    int leftIcon = typedArray.getResourceId(R.styleable.MyTitleBar_left_icon, R.drawable.ic_back);//左边图片
    int rightIcon = typedArray.getResourceId(R.styleable.MyTitleBar_right_icon, R.drawable.ic_more);//右边图片
    String rightText = typedArray.getString(R.styleable.MyTitleBar_right_text);//右边文字
    int titleBarType = typedArray.getInt(R.styleable.MyTitleBar_titlebar_type, 10);//标题栏类型,默认为10

    //赋值进去我们的标题栏
    tvTitle.setText(title);
    ivBack.setBackgroundResource(leftIcon);
    tvMore.setText(rightText);
    ivMore.setBackgroundResource(rightIcon);

    //可以传入type值,可自定义判断值
    if (titleBarType == 10) {//不传入,默认为10,显示更多 文字,隐藏更多图标按钮
      ivMore.setVisibility(View.GONE);
      tvMore.setVisibility(View.VISIBLE);
    } else if(titleBarType == 11) {//传入11,显示更多图标按钮,隐藏更多 文字
      tvMore.setVisibility(View.GONE);
      ivMore.setVisibility(View.VISIBLE);
    } else {
      tvMore.setVisibility(View.GONE);
      ivMore.setVisibility(View.VISIBLE);
    }
  }

  //左边图片点击事件
  public void setLeftIconOnClickListener(OnClickListener l){
    ivBack.setOnClickListener(l);
  }

  //右边图片点击事件
  public void setRightIconOnClickListener(OnClickListener l){
    ivBack.setOnClickListener(l);
  }

  //右边文字点击事件
  public void setRightTextOnClickListener(OnClickListener l){
    ivBack.setOnClickListener(l);
  }

  public void setTitle(String s) {
    this.tvTitle.setText(s);
  }
  public void setTitle(CharSequence s) {
    this.tvTitle.setText(s);
  }
}