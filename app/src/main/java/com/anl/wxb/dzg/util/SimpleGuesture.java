package com.anl.wxb.dzg.util;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * author: admin
 * time: 2016/3/4 16:53
 * e-mail: lance.cao@anarry.com
 */
public abstract class SimpleGuesture implements GestureDetector.OnGestureListener{
    @Override
    public boolean onDown(MotionEvent e) {
        //必须返回true，拦截事件
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
    public abstract boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);

}
