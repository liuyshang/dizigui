package com.anl.wxb.dzg.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anl.wxb.dzg.R;

/**
 * Created by admin on 2015/9/1.
 */
public class TextGroupView extends RelativeLayout{

    private TextView pinyin1;
    private TextView hanzi1;

    private TextView pinyin2;
    private TextView hanzi2;

    private TextView pinyin3;
    private TextView hanzi3;

    private TextView pinyin4;
    private TextView hanzi4;

    private TextView pinyin5;
    private TextView hanzi5;

    private TextView pinyin6;
    private TextView hanzi6;

    private TextView pinyin7;
    private TextView hanzi7;

    private TextView pinyin8;
    private TextView hanzi8;

    private TextView pinyin9;
    private TextView hanzi9;

    private TextView pinyin10;
    private TextView hanzi10;

    private TextView pinyin11;
    private TextView hanzi11;

    private TextView pinyin12;
    private TextView hanzi12;


    public TextGroupView(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TextGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.dzg_textct,this);

        pinyin1 = (TextView) findViewById(R.id.pinyin1);
        hanzi1 = (TextView) findViewById(R.id.hanzi1);

        pinyin2 = (TextView) findViewById(R.id.pinyin2);
        hanzi2 = (TextView) findViewById(R.id.hanzi2);

        pinyin3 = (TextView) findViewById(R.id.pinyin3);
        hanzi3 = (TextView) findViewById(R.id.hanzi3);

        pinyin4 = (TextView) findViewById(R.id.pinyin4);
        hanzi4 = (TextView) findViewById(R.id.hanzi4);

        pinyin5 = (TextView) findViewById(R.id.pinyin5);
        hanzi5 = (TextView) findViewById(R.id.hanzi5);

        pinyin6 = (TextView) findViewById(R.id.pinyin6);
        hanzi6 = (TextView) findViewById(R.id.hanzi6);

        pinyin7 = (TextView) findViewById(R.id.pinyin7);
        hanzi7 = (TextView) findViewById(R.id.hanzi7);

        pinyin8 = (TextView) findViewById(R.id.pinyin8);
        hanzi8 = (TextView) findViewById(R.id.hanzi8);

        pinyin9 = (TextView) findViewById(R.id.pinyin9);
        hanzi9 = (TextView) findViewById(R.id.hanzi9);

        pinyin10 = (TextView) findViewById(R.id.pinyin10);
        hanzi10 = (TextView) findViewById(R.id.hanzi10);

        pinyin11 = (TextView) findViewById(R.id.pinyin11);
        hanzi11 = (TextView) findViewById(R.id.hanzi11);

        pinyin12 = (TextView) findViewById(R.id.pinyin12);
        hanzi12 = (TextView) findViewById(R.id.hanzi12);

    }
    public void setPinyin(String pinyin){
        String[] array_pinyin = pinyin.split("\\s+");

        pinyin1.setText(array_pinyin[0]);
        pinyin2.setText(array_pinyin[1]);
        pinyin3.setText(array_pinyin[2]);
        pinyin4.setText(array_pinyin[3]);
        pinyin5.setText(array_pinyin[4]);
        pinyin6.setText(array_pinyin[5]);
        pinyin7.setText(array_pinyin[6]);
        pinyin8.setText(array_pinyin[7]);
        pinyin9.setText(array_pinyin[8]);
        pinyin10.setText(array_pinyin[9]);
        pinyin11.setText(array_pinyin[10]);
        pinyin12.setText(array_pinyin[11]);
    }

    public void setHanzi(String hanzi){
        String[] array_hanzi = hanzi.split("\\s+");

        hanzi1.setText(array_hanzi[0]);
        hanzi2.setText(array_hanzi[1]);
        hanzi3.setText(array_hanzi[2]);
        hanzi4.setText(array_hanzi[3]);
        hanzi5.setText(array_hanzi[4]);
        hanzi6.setText(array_hanzi[5]);
        hanzi7.setText(array_hanzi[6]);
        hanzi8.setText(array_hanzi[7]);
        hanzi9.setText(array_hanzi[8]);
        hanzi10.setText(array_hanzi[9]);
        hanzi11.setText(array_hanzi[10]);
        hanzi12.setText(array_hanzi[11]);
    }
}

