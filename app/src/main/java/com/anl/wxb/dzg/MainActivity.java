package com.anl.wxb.dzg;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.anl.base.AnlActivity;
import com.anl.base.AnlHttp;
import com.anl.base.annotation.view.ViewInject;
import com.anl.base.http.AjaxCallBack;
import com.anl.wxb.ability.ANLBar;
import com.anl.wxb.ability.XmppFunc;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class MainActivity extends AnlActivity implements View.OnTouchListener {

    @ViewInject(id = R.id.RL_back, click = "clickActionbar")
    RelativeLayout RL_back;
    @ViewInject(id = R.id.btn_back, click = "clickActionbar")
    ImageView btn_back;
    @ViewInject(id = R.id.text_back, click = "clickActionbar")
    TextView text_back;
    @ViewInject(id = R.id.btn_list, click = "clickActionbar")
    ImageView btn_list;
    @ViewInject(id = R.id.text_list, click = "clickActionbar")
    TextView text_list;
    @ViewInject(id = R.id.RL_listbar, click = "clickActionbar")
    RelativeLayout RL_listbar;

    @ViewInject(id = R.id.RL_content)
    RelativeLayout RL_content;
    @ViewInject(id = R.id.textview)
    TextGroupView textview;
    @ViewInject(id = R.id.btn_play, click = "clickPlay")
    Button btn_play;
    @ViewInject(id = R.id.text_ct)
    MyTextView text_ct;

    @ViewInject(id = R.id.RL_list)
    RelativeLayout RL_list;
    @ViewInject(id = R.id.seekbar)
    VerticalSeekbar seekbar;
    @ViewInject(id = R.id.list_view)
    ListView list_view;
    @ViewInject(id = R.id.btn_up, click = "clickList")
    ImageView btn_up;
    @ViewInject(id = R.id.btn_down, click = "clickList")
    ImageView btn_down;

    @ViewInject(id = R.id.btn_pageleft, click = "clickPage")
    Button btn_pageleft;
    @ViewInject(id = R.id.btn_pageright, click = "clickPage")
    Button btn_pageright;

    private DiZiGui dzg;
    private int pagecount = 0;      //0~89
    private ANLBar anlBar;
    private GestureDetector gestureDetector;
    private boolean left_right = true;  //left true; right false

    private  SharedPreferences share_file;
    String key = "dzg";
    /*存储的文件名*/
    public static final String dzg_name = "dzg";
    /*存储后的文件路径：/date/data/<package name>/share_prefs + 文件名.xml*/
    public static final String PATH = "/data/data/com.anl.wxb.dzg/shared_prefs/dzg.xml";
    public String path_share = "/data/data/com.anl.wxb.dzg/shared_prefs/";
    private String encrypt_s = null;  //保存解密出的数据


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("MainActivity", "onCreate");

        init();   //初始化

        File file_share = new File(path_share);
        File file_dzg = new File(path_share + "dzg.xml");

        if(file_share.exists()){
            if(file_dzg.exists()){
                Log.e("File", "file_share 存在///file_dzg 存在");

                String string = share_file.getString(key,"");
                new DecryptAsyncTask().execute(string);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setData(encrypt_s, pagecount);
                    }
                },500);

//                Log.e("File_string",string);
            }else{
                Log.e("File","file_share 存在/// file_dzg 不存在");

                downData();
            }
        }else{
            Log.e("File","file_share 不存在");

            downData();
        }


    }

//    初始化 xmppfunc,seekbar的thumb位置，左翻按钮初始为隐藏，listview初始为隐藏
    private void init() {
        Log.e("MainActivity", "init");

        XmppFunc.getInstance().openXmpp(getApplicationContext());

//        获取SharePreferences对象
        share_file = getSharedPreferences(dzg_name, MODE_PRIVATE);

        seekbar.setProgress(100);
        btn_pageleft.setVisibility(View.INVISIBLE);
        RL_list.setVisibility(View.INVISIBLE);
    }


//    下载数据
    private void downData() {
        Log.e("MainActivity", "downData");

        AnlHttp http = new AnlHttp();
        http.get("http://api.filialbox.com/api/dizigui", new AjaxCallBack<String>() {
            @Override
            public boolean isProgress() {
                return super.isProgress();
            }

            @Override
            public int getRate() {
                return super.getRate();
            }

            @Override
            public AjaxCallBack<String> progress(boolean progress, int rate) {
                Log.e("progress", "rate:" + rate);

                return super.progress(progress, rate);
            }

            @Override
            public void onStart() {
                super.onStart();

                Log.e("downData", "onStart");
            }

            @Override
            public void onLoading(long count, long current) {
                super.onLoading(count, current);

                Log.e("onLoading", "count:" + count + "  " + "current" + current);
            }

            @Override
            public void onSuccess(String s) {
                Log.e("downData", "onSuccess");
//                Log.e("onSuccess_s", s);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(s);
                    if ("0".equals(jsonObject.getString("code"))) {

                        new EncryptAsyncTask().execute(s);
//                        String aes_s = new AESHelper().encrypt("aes",s);
//                        //      获取Editor对象
//                        SharedPreferences.Editor editor = share_file.edit();
//                        editor.putString(key, aes_s);
//                        editor.apply();

                        setData(s, pagecount);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.e("onSuccess_e", e.toString());
                    Toast.makeText(getApplicationContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onSuccess(s);
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);

                Log.e("downData", "onFailure");
                Log.e("onFailure", strMsg);
                Toast.makeText(getApplicationContext(), "访问网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    设置页面数据
    private void setData(String s, int pagecount) {
        Log.e("downData", "setData");

        dzg = JSON.parseObject(s, DiZiGui.class);

        MyAdapter myAdapter = new MyAdapter(getApplicationContext(), dzg.getList());
        list_view.setAdapter(myAdapter);

        setListener();

        contentPage(pagecount);
    }

    //    seekbar,list_view，RL_content的滑动，点击监听
    private void setListener() {
//        seekbar的滑动监听
        seekbar.setOnSeekBarChangeListener(new VerticalSeekbar.OnSeekBarChangeListener() {
            boolean scroll_flag = false;

            @Override
            public void onProgressChanged(VerticalSeekbar VerticalSeekbar, int progress, boolean fromUser) {
                if (scroll_flag) {
                    int position_seekbar = (int) Math.floor((100 - progress) * 81 / 100);
                    list_view.setSelection(position_seekbar);

                    Log.e("onProgressChanged_p", String.valueOf(position_seekbar));
                    Log.e("onProgressChanged_f", String.valueOf(list_view.getFirstVisiblePosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekbar VerticalSeekbar) {
                scroll_flag = true;
            }

            @Override
            public void onStopTrackingTouch(VerticalSeekbar VerticalSeekbar) {
                scroll_flag = false;
            }
        });

                /*
        * 设置scrollview 顶部时，往下拉的时候，不现实颜色渐变
        * */
        list_view.setOverScrollMode(View.OVER_SCROLL_NEVER);

//        listview 的点击监听
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

        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            //          listview的滑动监听
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int position_scroll = (int) (100 - Math.floor(firstVisibleItem * 100 / 80));
                seekbar.setProgress(position_scroll);
            }
        });

//        RL-content的滑动监听
        gestureDetector = new GestureDetector(this, new SimpleGestureListener());
        RL_content.setOnTouchListener(this);
    }


    //    设置弟子规的内容
    private void contentPage(int pagecount) {
        DiZiGui.Data data = dzg.getList().get(pagecount);

        textview.setPinyin(data.pinyin);
        textview.setHanzi(data.hanzi);
        text_ct.setText(data.jieshi);

        Log.e("setData_p", data.pinyin);
        Log.e("setData_h", data.hanzi);
        Log.e("setData_c", data.jieshi);

        soundPlay();
    }

//    声音播放
    private void soundPlay() {
        String string = null;
        String sound = null;

        RL_list.setVisibility(View.INVISIBLE);
        string = dzg.getList().get(pagecount).hanzi;
        String[] array_sound = string.split("\\s+");
//        for(int i=0; i<array_sound.length; i++){
//            Log.e("clickPlay_a",array_sound[i]);
//        }
        sound = array_sound[0] + array_sound[1] + array_sound[2] + "，" + array_sound[3] + array_sound[4] + array_sound[5] + "，"
                + array_sound[6] + array_sound[7] + array_sound[8] + "，" + array_sound[9] + array_sound[10] + array_sound[11];

        Log.e("clickPlay_s", sound);

        anlBar = new ANLBar(sound, "", "", "", null, null, null);
        anlBar.setShowwindow(0);
        anlBar.setPlaysound(1);
        anlBar.setSpeakspeed("5");
        anlBar.show();
    }

    public void clickActionbar(View view) {
        Log.e("onCreate", "clickActionbar");

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
            default:
                break;
        }
    }

    public void clickPlay(View view) {
        Log.e("onCreate", "clickPlay");

        switch (view.getId()) {
            case R.id.btn_play:
                soundPlay();
                break;
            default:
                break;
        }
    }

    public void clickList(View view) {
        Log.e("onCreate", "clickList");

        switch (view.getId()) {
            case R.id.btn_up:
                int position_up = list_view.getFirstVisiblePosition();

                Log.e("clickList_up", String.valueOf(position_up));

                if (position_up < 8) {
                    list_view.setSelection(0);
                } else {
                    list_view.setSelection(position_up - 8);
                }
                break;
            case R.id.btn_down:
                int position_down = list_view.getFirstVisiblePosition();

                Log.e("clickList_down", String.valueOf(position_down));
                if (position_down > 81) {
                    list_view.setSelection(81);
                } else {
                    list_view.setSelection(position_down + 8);
                }
                break;
            default:
                break;
        }
    }

    public void clickPage(View view) {
        Log.e("onCreate", "clickPage");

        switch (view.getId()) {
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

//    翻页
    private void changePage(boolean left_right) {
        Log.e("clickPage", "ChangePage");

        if (left_right) {
//            左翻页
            /*
            * 如果pagecount = 1,则左翻页按钮隐藏
            * */
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
//            右翻页
            /*
            * 如果pagecount=89 ,则右翻页按钮隐藏
            * */
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

        Log.e("ChangePage", String.valueOf(pagecount));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("MainActivity", "onDestroy");
        XmppFunc.getInstance().closeXmpp(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

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
            Log.e("SimpleGestureListener", "onFling");

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

    private class EncryptAsyncTask extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            Log.e("MainActivity","EncryptAsyncTask");

            String aes_s = null;
            try {
                aes_s = new AESHelper().encrypt("aes", params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("EncryptAsyncTask", String.valueOf(e));
            }
            //      获取Editor对象
            SharedPreferences.Editor editor = share_file.edit();
            editor.putString(key, aes_s);
            editor.apply();
            return null;
        }
    }

    private class DecryptAsyncTask extends AsyncTask<String ,Integer,String>{
        @Override
        protected String doInBackground(String... params) {
            Log.e("MainActivity","DecryptAsyncTask");
            try {
                encrypt_s = new AESHelper().decrypt("aes",params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DecryptAsyncTask", String.valueOf(e));
            }
            return null;
        }
    }
}
