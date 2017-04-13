package com.eusoft.recite.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by xj on 2016/12/16.
 */

public class XSwipeLayout extends FrameLayout {

    private Scroller mScroll;
    private VelocityTracker mTracker;
    private int minVelocity;
    private int maxVelocity;
    private int touchSlop;

    public XSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mScroll = new Scroller(context, new LinearInterpolator());
        mTracker = VelocityTracker.obtain();
        minVelocity = 3000;
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        View child = getChildAt(getChildCount() - 1);
        child.layout(getMeasuredWidth(), 0,
                child.getMeasuredWidth() + getMeasuredWidth(), getMeasuredHeight());
    }

    private float oldX;
    private float downX;
    private float downY;
    private boolean mTag;              //标志着是否执行了处理move事件的判断
    private boolean isSlopChecked;              //标志着是否执行了move最小距离的判断

    /**
     * 判断是否能处理move事件*/
    private boolean canMove(MotionEvent event) {
        if (!isSlopChecked) {
            if (Math.abs(event.getY() - downY) > touchSlop ||
                    Math.abs(event.getX() - downX) > touchSlop) {
                isSlopChecked = true;
                oldX = event.getX();
                return true;
            } else return false;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isAnimating) return true;
        return super.onInterceptTouchEvent(ev);
    }

    private static boolean isAnimating;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canSwipe) {
            return super.onTouchEvent(event);
        }
        if (isAnimating) {
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        mTracker.addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTag = true;
                isSlopChecked = false;
                oldX = event.getX();
                downX = event.getX();
                downY = event.getY();
                setEnabled(true);
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canMove(event)) {
                    break;
                }
                if (mTag) {
                    mTag = false;
                    if (Math.abs(event.getX() - downX) < Math.abs(event.getY() - downY) * 3) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                }
                float dx = event.getX() - oldX;
                if (dx < 0 || getScrollX() > 0) {
                    if (getScrollX() - dx > 0) {
                        scrollBy((int) -dx, 0);
                    } else scrollTo(0, 0);
                }
                setEnabled(false);
                oldX = event.getX();
                break;
            default:
                startScroll();
                break;
        }
        super.onTouchEvent(event);
        return true;
    }

    private void startScroll() {
        isAnimating = true;
        mTracker.computeCurrentVelocity(1000, maxVelocity);
        float xVelocity = mTracker.getXVelocity();
        mTracker.clear();
        if (Math.abs(getScrollX()) >= getMeasuredWidth() / 2 ||
                (Math.abs(xVelocity) > minVelocity && xVelocity < 0)) {
            float dx = getMeasuredWidth() - getScrollX();
            mScroll.startScroll(getScrollX(), 0, (int) dx, 0, 300);
        } else {
            mScroll.startScroll(getScrollX(), 0, -getScrollX(), 0, 300);
        }
        update();
    }

    private void update() {
        if (mScroll.computeScrollOffset()) {
            int dx = mScroll.getCurrX();
            scrollTo(dx, 0);
            if (mScroll.isFinished()) {
                if (listener != null && getScrollX() > 0) {
                    listener.onRemoved(this);
                }
                if (getScrollX() > 0) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isAnimating = false;
                        }
                    }, 300);
                }else isAnimating = false;
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    update();
                }
            }, 16);
        }
    }

    private boolean canSwipe = true;

    public void setCanSwipe(boolean canSwipe) {
        this.canSwipe = canSwipe;
    }

    private OnRemoveListener listener;

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.listener = listener;
    }

    public interface OnRemoveListener {
        void onRemoved(XSwipeLayout XSwipeLayout);
    }
}
