package com.anl.wxb.dzg.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.anl.wxb.dzg.entity.DiZiGui;
import com.anl.wxb.dzg.R;

import java.util.List;

/**
 * Created by admin on 2015/9/1.
 */
public class MyAdapter extends BaseAdapter{

    public Context mContext;
    public List<DiZiGui.Data> mList = null;

    public MyAdapter(Context mContext, List<DiZiGui.Data> mList){
        super();
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.e("MyAdapter","getView");

        ViewHolder mViewHolder = null;

        if(convertView == null){
            mViewHolder = new ViewHolder();
            LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
            convertView = mLayoutInflater.inflate(R.layout.list_textct,null);

            mViewHolder.text_ct = (TextView) convertView.findViewById(R.id.text_ct);

            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        DiZiGui.Data data = mList.get(position);

        if(position < 9) {
            mViewHolder.text_ct.setText(" " + (position + 1) + " ." + data.hanzi.replaceAll(" +", ""));
        } else{
            mViewHolder.text_ct.setText("" + (position + 1) + " ."+data.hanzi.replaceAll(" +",""));
        }
//        Log.e("getView", data.hanzi);

        return convertView;
    }

    private class ViewHolder {
        TextView text_ct;
    }
}
