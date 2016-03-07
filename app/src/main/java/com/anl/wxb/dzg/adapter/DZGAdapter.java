package com.anl.wxb.dzg.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.anl.wxb.dzg.bean.DiZiGui;
import com.anl.wxb.dzg.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2015/9/1.
 */
public class DZGAdapter extends BaseAdapter {

    public Context mContext;
    public List<DiZiGui.ListEntity> mList = new ArrayList<>();

    public DZGAdapter(Context mContext, List<DiZiGui.ListEntity> mList) {
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
        ViewHolder mViewHolder = null;

        if (convertView == null) {
            mViewHolder = new ViewHolder();
            LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
            convertView = mLayoutInflater.inflate(R.layout.list_textct, null);
            mViewHolder.text_ct = (TextView) convertView.findViewById(R.id.text_ct);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        DiZiGui.ListEntity entity = mList.get(position);
        if (position < 9) {
            mViewHolder.text_ct.setText(" " + (position + 1) + " ." + entity.getHanzi().replaceAll(" +", ""));
        } else {
            mViewHolder.text_ct.setText("" + (position + 1) + " ." + entity.getHanzi().replaceAll(" +", ""));
        }
        return convertView;
    }

    private class ViewHolder {
        TextView text_ct;
    }
}
