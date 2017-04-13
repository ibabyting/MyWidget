package com.eusoft.dict.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by xj on 2017/2/16.
 */

public class DynamicLinearLayout extends LinearLayout {
    private static int touchSlop;
    private int originMarginTop;
    private int originMarginBottom;

    public DynamicLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public DynamicLinearLayout(Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        originMarginTop = params.topMargin;
        originMarginBottom = params.bottomMargin;
    }

    private boolean isChecked;
    private static final int STATE_MOVE = 1;
    private static final int STATE_SCALE = 2;
    private int state = -1;

    private boolean childNeedMoveEvent() {
        state = -1;
        int count = getChildCount();
        Rect r = new Rect();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            view.getGlobalVisibleRect(r);
            if (r.contains((int) downX, (int) downY)) {
                if ("needMoveEvent".equals(view.getTag())) {
                    return true;
                }
                if ("performScale".equals(view.getTag())) {
                    state = STATE_SCALE;
                    return false;
                }
                state = STATE_MOVE;
                return false;
            }
        }
        return false;
    }

    private float downX;
    private float downY;

    private int mode = -1;
    private boolean intercept;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                oldY = ev.getRawY();

                mode = 0;
                isChecked = false;
                intercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mode) {
                    case 0:
                        if (Math.abs(ev.getRawX() - downX) > touchSlop || Math.abs(ev.getRawY() - downY) > touchSlop) {
                            mode = 1;
                        }
                        break;
                    case 1:
                        if (!isChecked) {
                            intercept = !childNeedMoveEvent();
                            isChecked = true;
                            oldY = ev.getRawY();
                        }
                        break;
                }
                break;
        }
        return intercept;
    }

    private float oldY;
    private int oldPosition;
    private int oldHeight;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float dy = event.getRawY() - oldY;
            oldY = event.getRawY();

            MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
            if (params != null) {
                switch (state) {
                    case STATE_MOVE:
                        params.topMargin += dy;

                        if (params.topMargin + getHeight() + originMarginBottom > parentHeight) {
                            params.topMargin = parentHeight - getHeight() - originMarginBottom;
                        } else if (params.topMargin < originMarginTop)
                            params.topMargin = originMarginTop;

                        if (oldPosition == params.topMargin) return super.onTouchEvent(event);
                        oldPosition = params.topMargin;

                        setLayoutParams(params);

                        break;
                    case STATE_SCALE:
                        params.height += dy;
                        if (params.height + params.topMargin + params.bottomMargin > parentHeight) {
                            params.height = parentHeight - params.topMargin - params.bottomMargin;
                        } else if (params.height < getMinimumHeight())
                            params.height = getMinimumHeight();

                        if (oldHeight == params.height) return super.onTouchEvent(event);
                        oldHeight = params.height;

                        setLayoutParams(params);

                        break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private int parentHeight;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        parentHeight = ((ViewGroup) getParent()).getHeight();
    }
}
