package com.example.demo03.mytree;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;

public class CustomHorizontalScrollView extends HorizontalScrollView {
    //用于确定触摸滑动的最小距离
    private int mTouchSlop;
    private float mPrevX;
    private float mPrevY;
    private boolean mIsHorizontalScrolling;

    public CustomHorizontalScrollView(Context context) {
        this(context, null);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = ev.getX();
                mPrevY = ev.getY();
                mIsHorizontalScrolling = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(ev.getX() - mPrevX);
                float deltaY = Math.abs(ev.getY() - mPrevY);

                if (deltaX > mTouchSlop && deltaX > deltaY) {
                    mIsHorizontalScrolling = true;
                    return true;
                }else {
                    mIsHorizontalScrolling = false;
                    return false;
                }
//                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsHorizontalScrolling) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(ev);
    }
}
