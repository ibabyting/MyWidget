package com.eusoft.recite.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import static android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

/**
 * Created by xj on 2016/12/13.
 */

public class XMenuWindow {
    private View mContentView;
    private Context mContext;
    private int resId;
    private boolean isShowing;
    private WindowManager.LayoutParams params;
    private WindowManager manager;

    public XMenuWindow(Context mContext, @LayoutRes int id) {
        this.resId = id;
        this.mContext = mContext;
        init();
    }

    private void init() {
        manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mContentView = LayoutInflater.from(mContext).inflate(resId, null);
        params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        params.flags = FLAG_DIM_BEHIND | FLAG_WATCH_OUTSIDE_TOUCH;
        params.dimAmount = 0f;
        params.format = PixelFormat.TRANSPARENT;

        //点击菜单外部关闭菜单
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect r = new Rect();
                v.getGlobalVisibleRect(r);
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                        && !r.contains((int) event.getX(), (int) event.getY())) {
                    dismiss();
                }
                return false;
            }
        });
        mContentView.setFocusableInTouchMode(true);
        mContentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    public void showMenu() {
        if (!isShowing) {
            isShowing = true;
            //mContentView.setAlpha(1f);
            manager.addView(mContentView, params);
        }
    }

    public void dismiss() {
        if (isShowing) {
            removeWindow();
        }
    }
    private boolean needDelayedDismiss;
    public void setDelayedDismiss(boolean isNeed) {
        needDelayedDismiss = isNeed;
    }

    private void removeWindow() {
        try {
            if (needDelayedDismiss) {
                mContentView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.removeView(mContentView);
                        isShowing = false;
                    }
                }, 300);
            } else {
                manager.removeView(mContentView);
                isShowing = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回填充的view
     */
    public View getContentView() {
        return mContentView;
    }

    /**
     * Information about how wide the view wants to be. Can be one of the
     * constants FILL_PARENT (replaced by MATCH_PARENT ,
     * in API Level 8) or WRAP_CONTENT. or an exact size.
     */
    public XMenuWindow setWidth(int w) {
        params.width = w;
        return this;
    }

    /**
     * Information about how wide the view wants to be. Can be one of the
     * constants FILL_PARENT (replaced by MATCH_PARENT ,
     * in API Level 8) or WRAP_CONTENT. or an exact size.
     */
    public XMenuWindow setHeight(int h) {
        params.height = h;
        return this;
    }

    public XMenuWindow setGravity(int gravity) {
        params.gravity = gravity;
        return this;
    }

    /**
     * X position for this window.  With the default gravity it is ignored.
     * When using {@link Gravity#LEFT} or {@link Gravity#START} or {@link Gravity#RIGHT} or
     * {@link Gravity#END} it provides an offset from the given edge.
     */
    public XMenuWindow setOffsetX(int x) {
        params.x = x;
        return this;
    }

    /**
     * Y position for this window.  With the default gravity it is ignored.
     * When using {@link Gravity#TOP} or {@link Gravity#BOTTOM} it provides
     * an offset from the given edge.
     */
    public XMenuWindow setOffsetY(int y) {
        params.y = y;
        return this;
    }

    public XMenuWindow dimAmount(float dimAmount) {
        params.dimAmount = dimAmount;
        return this;
    }

    public XMenuWindow setAnimationStyle(@StyleRes int id) {
        params.windowAnimations = id;
        return this;
    }
}
