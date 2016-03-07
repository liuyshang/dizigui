package com.anl.wxb.dzg.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.anl.base.AnlActivity;
import com.anl.base.AnlHttp;
import com.anl.base.annotation.view.ViewInject;
import com.anl.base.http.AjaxCallBack;
import com.anl.wxb.ability.ANLBar;
import com.anl.wxb.ability.XmppFunc;
import com.anl.wxb.dzg.R;
import com.anl.wxb.dzg.adapter.DZGAdapter;
import com.anl.wxb.dzg.bean.DiZiGui;
import com.anl.wxb.dzg.util.AESHelper;
import com.anl.wxb.dzg.util.SimpleGuesture;
import com.anl.wxb.dzg.view.MyTextView;
import com.anl.wxb.dzg.view.TextGroupView;
import com.anl.wxb.dzg.view.VerticalSeekBar;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AnlActivity implements View.OnTouchListener {

    /**
     * 返回按钮
     */
    @ViewInject(id = R.id.RL_back, click = "onClick")
    private RelativeLayout RL_back;
    /**
     * 目录
     */
    @ViewInject(id = R.id.RL_listbar, click = "onClick")
    private RelativeLayout RL_listbar;
    /**
     * 页面
     */
    @ViewInject(id = R.id.RL_bg)
    private RelativeLayout rlBg;
    /**
     * 页面内容
     */
    @ViewInject(id = R.id.RL_content)
    private RelativeLayout rlContent;
    /**
     * 弟子规内容
     */
    @ViewInject(id = R.id.textview)
    private TextGroupView textview;
    /**
     * 语音播放
     */
    @ViewInject(id = R.id.btn_play, click = "onClick")
    private Button btn_play;
    /**
     * 解释内容
     */
    @ViewInject(id = R.id.text_ct)
    private MyTextView text_ct;
    /**
     * 目录布局
     */
    @ViewInject(id = R.id.RL_list)
    private RelativeLayout RL_list;
    @ViewInject(id = R.id.seekBar)
    private VerticalSeekBar seekbar;
    @ViewInject(id = R.id.list_view)
    private ListView list_view;
    @ViewInject(id = R.id.btn_up, click = "onClick")
    private ImageView btn_up;
    @ViewInject(id = R.id.btn_down, click = "onClick")
    private ImageView btn_down;
    /**
     * 翻页按钮
     */
    @ViewInject(id = R.id.btn_pageleft, click = "onClick")
    private Button btn_pageleft;
    @ViewInject(id = R.id.btn_pageright, click = "onClick")
    private Button btn_pageright;

    private static final String RIGHT = "right";
    private static final String LEFT = "left";
    private static final String DOWN = "down";
    private static final String UP = "up";
    public static final String NAME = "dzg";
    private static final String KEY = "dzg";

    private Context mContext;
    private List<DiZiGui.ListEntity> listDZG = new ArrayList<>();
    private GestureDetector gestureDetector;
    private SharedPreferences share_file;
    private SweetAlertDialog mDialog;
    private File file_dzg;
    private int pagecount = 0;      //0~89
    private boolean flag = false;
    private boolean flagMove = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();   //初始化
        showDialog();
        ifXMLExist();
        setListener();
    }

    /**
     * 初始化
     */
    private void init() {
        mContext = MainActivity.this;
        XmppFunc.getInstance().openXmpp(getApplicationContext());
        share_file = getSharedPreferences(NAME, MODE_PRIVATE);
        btn_pageleft.setVisibility(View.GONE);
        RL_list.setVisibility(View.GONE);
        rlContent.setVisibility(View.GONE);
        file_dzg = new File("/data/data/com.anl.wxb.dzg/shared_prefs/dzg.xml");
    }

    /**
     * dzg.xml存在就读取数据，不存在就下载数据
     */
    private void ifXMLExist() {
        if (file_dzg.exists()) {
            String string = share_file.getString(KEY, "");
            new DecryptAsyncTask().execute(string);
        } else {
            downData();
        }
    }

    /**
     * 加载数据时，显示Loading效果
     */
    private void showDialog() {
        mDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        mDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mDialog.setTitleText("Loading");
        mDialog.setCancelable(false);
        mDialog.show();
    }

    /**
     * 监听
     */
    private void setListener() {
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (flag) {
                    list_view.setSelection(progress * 81 / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                flag = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                flag = false;
            }
        });

        //设置scrollview 顶部时，往下拉的时候，不现实颜色渐变
        list_view.setOverScrollMode(View.OVER_SCROLL_NEVER);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pagecount = position;
                contentPage(pagecount);
                list_view.setSelection(pagecount - 3);
                RL_list.setVisibility(View.GONE);
                btnPageVisibility(pagecount);
            }
        });

        //listview 滑动监听
        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!flag) {
                    seekbar.setProgress(firstVisibleItem * 100 / 81);
                }
            }
        });

        //手势监听
        gestureDetector = new GestureDetector(this, new SimpleGuesture() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (flagMove) {
                    if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 100) {
                        addAnimation(RIGHT);
                    } else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 100) {
                        addAnimation(LEFT);
                    }
                }
                return true;
            }
        });
        rlContent.setOnTouchListener(this);
    }

    /**
     * 左右翻页按钮的隐藏，显示
     *
     * @param pagecount
     */
    private void btnPageVisibility(int pagecount) {
        if (pagecount == 0) {
            btn_pageleft.setVisibility(View.GONE);
            btn_pageright.setVisibility(View.VISIBLE);
        } else if (pagecount == 89) {
            btn_pageleft.setVisibility(View.VISIBLE);
            btn_pageright.setVisibility(View.GONE);
        } else {
            btn_pageleft.setVisibility(View.VISIBLE);
            btn_pageright.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 下载数据
     */
    private void downData() {
        AnlHttp http = new AnlHttp();
        http.get("http://api.filialbox.com/api/dizigui", new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                Gson gson = new Gson();
                DiZiGui diZiGui = gson.fromJson(s, DiZiGui.class);
                String code = String.valueOf(diZiGui.getCode());
                if (code.equals("0")) {
                    mDialog.cancel();
                    listDZG = diZiGui.getList();
                    setData(listDZG, pagecount);
                    new EncryptAsyncTask().execute(s);
                } else {
                    showErrorDialog("获取数据失败");
                }
                super.onSuccess(s);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                /*访问网络失败时，取消加载效果图*/
                mDialog.cancel();
                showErrorDialog("访问网络错误");
            }
        });
    }

    /**
     * 显示错误的提示信息
     *
     * @param str
     */
    private void showErrorDialog(String str) {
        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setContentText(str)
                .show();
    }

    /**
     * 设置页面数据
     */
    private void setData(List<DiZiGui.ListEntity> list, int pagecount) {
        rlContent.setVisibility(View.VISIBLE);
        DZGAdapter DZGAdapter = new DZGAdapter(mContext, list);
        list_view.setAdapter(DZGAdapter);
        list_view.setSelected(true);
        contentPage(pagecount);
    }

    /**
     * 弟子规页面的内容，自动语音播放
     */
    private void contentPage(int pagecount) {
        final DiZiGui.ListEntity entity = listDZG.get(pagecount);
        ValueAnimator animator = ValueAnimator.ofFloat(0.3f, 1.0f).setDuration(1000);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textview.setAlpha((Float) animation.getAnimatedValue());
                text_ct.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                textview.setPinyin(entity.getPinyin());
                textview.setHanzi(entity.getHanzi());
                text_ct.setText(entity.getJieshi());
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                soundPlay();
            }
        });
        animator.start();
    }

    /**
     * 声音播放
     */
    private void soundPlay() {
        RL_list.setVisibility(View.GONE);
        String string = null;
        String sound = null;
        string = listDZG.get(pagecount).getHanzi();
        String[] array_sound = string.split("\\s+");
        sound = array_sound[0] + array_sound[1] + array_sound[2] + "，" + array_sound[3] + array_sound[4] + array_sound[5] + "，"
                + array_sound[6] + array_sound[7] + array_sound[8] + "，" + array_sound[9] + array_sound[10] + array_sound[11];
        ANLBar anlBar = new ANLBar(sound, "", "", "", null, null, null);
        anlBar.setShowwindow(0);
        anlBar.setPlaysound(1);
        anlBar.setSpeakspeed("5");
        anlBar.show();
    }

    /**
     * 点击事件
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.RL_back:
                finish();
                break;
            case R.id.RL_listbar:
                if (RL_list.getVisibility() == View.GONE) {
                    RL_list.setVisibility(View.VISIBLE);
                } else if (RL_list.getVisibility() == View.VISIBLE) {
                    RL_list.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_play:
                soundPlay();
                break;
            case R.id.btn_up:
                pageToUp();
                break;
            case R.id.btn_down:
                pageToDown();
                break;
            case R.id.btn_pageleft:
                addAnimation(LEFT);
                break;
            case R.id.btn_pageright:
                addAnimation(RIGHT);
                break;
            default:
                break;
        }
    }

    /**
     * 添加动画
     *
     * @param str
     */
    private void addAnimation(final String str) {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.7f)
                .setDuration(160);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                rlContent.setAlpha((float) Math.pow(value, 4));
                rlContent.setScaleX(value);
                rlContent.setScaleY(value);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                changePage(str);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                btn_pageright.setClickable(false);
                btn_pageleft.setClickable(false);
                flagMove = false;
            }
        });
        ValueAnimator animator1 = ValueAnimator.ofFloat(0.7f, 1.0f)
                .setDuration(700);
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                rlContent.setAlpha((float) Math.pow(value, 4));
                rlContent.setScaleX(value);
                rlContent.setScaleY(value);
            }
        });
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btn_pageleft.setClickable(true);
                btn_pageright.setClickable(true);
                flagMove = true;
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animator, animator1);
        set.start();
    }

    /**
     * 向下翻页
     */
    private void pageToDown() {
        int position_down = list_view.getFirstVisiblePosition();
        if (position_down > 81) {
            list_view.setSelection(81);
        } else {
            list_view.setSelection(position_down + 8);
        }
    }

    /**
     * 向上翻页
     */
    private void pageToUp() {
        int position_up = list_view.getFirstVisiblePosition();
        if (position_up < 8) {
            list_view.setSelection(0);
        } else {
            list_view.setSelection(position_up - 8);
        }
    }

    /**
     * 翻页处理
     */
    private void changePage(String str) {
        switch (str) {
            case "left":
                if (pagecount != 0) {
                    pagecount--;
                    contentPage(pagecount);
                    list_view.setSelection(pagecount - 3);
                    btnPageVisibility(pagecount);
                    btn_pageright.setVisibility(View.VISIBLE);
                    RL_list.setVisibility(View.GONE);
                }
                break;
            case "right":
                if (pagecount != 89) {
                    pagecount++;
                    contentPage(pagecount);
                    list_view.setSelection(pagecount - 3);
                    btnPageVisibility(pagecount);
                    btn_pageleft.setVisibility(View.VISIBLE);
                    RL_list.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 触摸处理
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * 解密数据 异步处理
     */
    private class EncryptAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String aes_s = null;
            try {
                aes_s = new AESHelper().encrypt("aes", params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return aes_s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //获取Editor对象
            SharedPreferences.Editor editor = share_file.edit();
            editor.putString(KEY, s);
            editor.apply();
        }
    }

    private class DecryptAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String encrypt_s = null;
            try {
                encrypt_s = new AESHelper().decrypt("aes", params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return encrypt_s;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.cancel();
            Gson gson = new Gson();
            DiZiGui diZiGui = gson.fromJson(s, DiZiGui.class);
            listDZG = diZiGui.getList();
            setData(listDZG, pagecount);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XmppFunc.getInstance().closeXmpp(getApplicationContext());
    }
}
