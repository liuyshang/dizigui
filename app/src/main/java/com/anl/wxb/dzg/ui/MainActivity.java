package com.anl.wxb.dzg.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.anl.base.AnlActivity;
import com.anl.base.AnlHttp;
import com.anl.base.annotation.view.ViewInject;
import com.anl.base.http.AjaxCallBack;
import com.anl.wxb.ability.ANLBar;
import com.anl.wxb.ability.XmppFunc;
import com.anl.wxb.dzg.aes.AESHelper;
import com.anl.wxb.dzg.entity.DiZiGui;
import com.anl.wxb.dzg.adapter.MyAdapter;
import com.anl.wxb.dzg.util.MyTextView;
import com.anl.wxb.dzg.R;
import com.anl.wxb.dzg.util.VerticalSeekBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AnlActivity implements View.OnTouchListener {

    @ViewInject(id = R.id.RL_back, click = "onClick")
    RelativeLayout RL_back;
    @ViewInject(id = R.id.btn_back, click = "onClick")
    ImageView btn_back;
    @ViewInject(id = R.id.text_back, click = "onClick")
    TextView text_back;
    @ViewInject(id = R.id.btn_list, click = "onClick")
    ImageView btn_list;
    @ViewInject(id = R.id.text_list, click = "onClick")
    TextView text_list;
    @ViewInject(id = R.id.RL_listbar, click = "onClick")
    RelativeLayout RL_listbar;

    @ViewInject(id = R.id.RL_content)
    RelativeLayout RL_content;
    @ViewInject(id = R.id.textview)
    TextGroupView textview;
    @ViewInject(id = R.id.btn_play, click = "onClick")
    Button btn_play;
    @ViewInject(id = R.id.text_ct)
    MyTextView text_ct;

    @ViewInject(id = R.id.RL_list)
    RelativeLayout RL_list;
    @ViewInject(id = R.id.seekBar)
    VerticalSeekBar seekbar;
    @ViewInject(id = R.id.list_view)
    ListView list_view;
    @ViewInject(id = R.id.btn_up, click = "onClick")
    ImageView btn_up;
    @ViewInject(id = R.id.btn_down, click = "onClick")
    ImageView btn_down;

    @ViewInject(id = R.id.btn_pageleft, click = "onClick")
    Button btn_pageleft;
    @ViewInject(id = R.id.btn_pageright, click = "onClick")
    Button btn_pageright;

    private DiZiGui dzg;
    private int pagecount = 0;      //0~89
    private ANLBar anlBar;
    private GestureDetector gestureDetector;
    private boolean left_right = true;  //left true; right false
    private boolean flag = false;

    private SharedPreferences share_file;
    String key = "dzg";
    /*存储的文件名*/
    public static final String dzg_name = "dzg";
    /*存储后的文件路径：/date/data/<package name>/share_prefs + 文件名.xml*/
    public static final String PATH = "/data/data/com.anl.wxb.dzg/shared_prefs/dzg.xml";
    public String path_share = "/data/data/com.anl.wxb.dzg/shared_prefs/";
    private String encrypt_s = null;  //保存解密出的数据
    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();   //初始化
        File file_share = new File(path_share);
        File file_dzg = new File(path_share + "dzg.xml");
        //隐藏页面布局
        RL_content.setVisibility(View.INVISIBLE);
        //加载数据时，显示Loading效果
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        //dzg.xml存在就读取数据，不存在就下载数据
        if (file_share.exists()) {
            if (file_dzg.exists()) {
                //数据
                String string = share_file.getString(key, "");
                new DecryptAsyncTask().execute(string);
            } else {
                downData();
            }
        } else {
            downData();
        }
    }

    /**
     * 初始化 xmppfunc,seekbar的thumb位置，左翻按钮初始为隐藏，listview初始为隐藏
     */
    private void init() {
        XmppFunc.getInstance().openXmpp(getApplicationContext());
        // 获取SharePreferences对象
        share_file = getSharedPreferences(dzg_name, MODE_PRIVATE);
        btn_pageleft.setVisibility(View.INVISIBLE);
        RL_list.setVisibility(View.INVISIBLE);
    }

    /**
     * 下载数据
     */
    private void downData() {
        AnlHttp http = new AnlHttp();
        http.get("http://api.filialbox.com/api/dizigui", new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(s);
                    if (jsonObject.getString("code").equals("0")) {
                        /*首次使用时，若网络较差，下载数据需要时间长*/
                        pDialog.cancel();
                        RL_content.setVisibility(View.VISIBLE);
                        setData(s, pagecount);
                        //加密数据
                        new EncryptAsyncTask().execute(s);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setContentText("获取数据失败")
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onSuccess(s);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                /*访问网络失败时，取消加载效果图*/
                pDialog.cancel();
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setContentText("访问网络错误")
                        .show();
            }
        });
    }

    /**
     * 初始化页面数据
     */
    private void setData(String s, int pagecount) {
        dzg = JSON.parseObject(s, DiZiGui.class);
        MyAdapter myAdapter = new MyAdapter(getApplicationContext(), dzg.getList());
        list_view.setAdapter(myAdapter);
        setListener();
        contentPage(pagecount);
    }

    /**
     * 监听
     */
    private void setListener() {
        //seekbar滑动监听
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (flag){
                    int position_seekbar = progress * 81 / 100;
                    list_view.setSelection(position_seekbar);
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
        //listview 的点击监听
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pagecount = position;
                contentPage(pagecount);
                list_view.setSelection(pagecount - 3);
                RL_list.setVisibility(View.INVISIBLE);
                if (pagecount == 0) {
                    btn_pageleft.setVisibility(View.INVISIBLE);
                    btn_pageright.setVisibility(View.VISIBLE);
                } else if (pagecount == 89) {
                    btn_pageleft.setVisibility(View.VISIBLE);
                    btn_pageright.setVisibility(View.INVISIBLE);
                } else {
                    btn_pageleft.setVisibility(View.VISIBLE);
                    btn_pageright.setVisibility(View.VISIBLE);
                }
            }
        });

        //listview 滑动监听
        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!flag) {
                    int position_scroll = firstVisibleItem * 100 / 81;
                    seekbar.setProgress(position_scroll);
                }
            }
        });

        //手势监听
        gestureDetector = new GestureDetector(this, new SimpleGestureListener());
        RL_content.setOnTouchListener(this);
    }


    /**
     * 弟子规页面的内容
     */
    private void contentPage(int pagecount) {
        DiZiGui.Data data = dzg.getList().get(pagecount);
        textview.setPinyin(data.pinyin);
        textview.setHanzi(data.hanzi);
        text_ct.setText(data.jieshi);
        soundPlay();
    }

    /**
     * 声音播放
     */
    private void soundPlay() {
        String string = null;
        String sound = null;
        RL_list.setVisibility(View.INVISIBLE);
        string = dzg.getList().get(pagecount).hanzi;
        String[] array_sound = string.split("\\s+");
        sound = array_sound[0] + array_sound[1] + array_sound[2] + "，" + array_sound[3] + array_sound[4] + array_sound[5] + "，"
                + array_sound[6] + array_sound[7] + array_sound[8] + "，" + array_sound[9] + array_sound[10] + array_sound[11];
        anlBar = new ANLBar(sound, "", "", "", null, null, null);
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
            case R.id.btn_back:
            case R.id.text_back:
            case R.id.RL_back:
                finish();
                break;
            case R.id.btn_list:
            case R.id.text_list:
            case R.id.RL_listbar:
                if (RL_list.getVisibility() == View.INVISIBLE) {
                    RL_list.setVisibility(View.VISIBLE);
                } else if (RL_list.getVisibility() == View.VISIBLE) {
                    RL_list.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.btn_play:
                soundPlay();
                break;
            case R.id.btn_up:
                int position_up = list_view.getFirstVisiblePosition();
                if (position_up < 8) {
                    list_view.setSelection(0);
                } else {
                    list_view.setSelection(position_up - 8);
                }
                break;
            case R.id.btn_down:
                int position_down = list_view.getFirstVisiblePosition();
                if (position_down > 81) {
                    list_view.setSelection(81);
                } else {
                    list_view.setSelection(position_down + 8);
                }
                break;
            case R.id.btn_pageleft:
                left_right = true;
                changePage(left_right);
                break;
            case R.id.btn_pageright:
                left_right = false;
                changePage(left_right);
                break;
            default:
                break;
        }
    }


    /**
     * 翻页处理
     */
    private void changePage(boolean left_right) {
        if (left_right) {
            //如果pagecount = 1,则左翻页按钮隐藏
            if (pagecount != 0) {
                pagecount--;
                contentPage(pagecount);
                list_view.setSelection(pagecount - 3);
                if (pagecount == 0) {
                    btn_pageleft.setVisibility(View.INVISIBLE);
                } else {
                    btn_pageleft.setVisibility(View.VISIBLE);
                }
                btn_pageright.setVisibility(View.VISIBLE);
                RL_list.setVisibility(View.INVISIBLE);
            }

        } else {
            //如果pagecount=89 ,则右翻页按钮隐藏
            if (pagecount != 89) {
                pagecount++;
                contentPage(pagecount);
                list_view.setSelection(pagecount - 3);
                if (pagecount == 89) {
                    btn_pageright.setVisibility(View.INVISIBLE);
                } else {
                    btn_pageright.setVisibility(View.VISIBLE);
                }
                btn_pageleft.setVisibility(View.VISIBLE);
                RL_list.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XmppFunc.getInstance().closeXmpp(getApplicationContext());
    }


    /**
     * 触摸处理
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }


    /**
     * 手势处理
     */
    private class SimpleGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 100) {
                left_right = false;
                changePage(left_right);

            } else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 100) {
                left_right = true;
                changePage(left_right);
            }
            return true;
        }
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
            //      获取Editor对象
            SharedPreferences.Editor editor = share_file.edit();
            editor.putString(key, s);
            editor.apply();
        }
    }

    private class DecryptAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
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
            pDialog.cancel();
            RL_content.setVisibility(View.VISIBLE);
            setData(s, pagecount);
        }
    }
}
