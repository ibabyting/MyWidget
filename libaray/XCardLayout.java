package com.eusoft.recite.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.eusoft.dict.util.UIUtils;
import com.eusoft.recite.ReciteConstants;
import com.eusoft.recite.adapter.XCardAdapter;
import com.eusoft.recite.model.ReciteCard;

/**
 * Created by XJ on 2016/12/3
 */

public class XCardLayout extends ViewGroup {

    private static final int MAX_CARD_NUM = 4;
    private static final float SCALE_FACTOR = 0.12f;
    private static final float MAX_ROTATE_DEGREE = 8;
    public static final int IN_TOP = 0x11;
    public static final int IN_LEFT = 0x12;
    public static final int IN_BOTTOM = 0x13;
    public static final int IN_RIGHT = 0x14;
    private static int TOUCH_SLOP;

    private float max_move_distance;         //用于计算移动percent
    private static int height_increase_factor;
    private XCardAdapter mAdapter;
    private VelocityTracker mVelocityTracker;
    private float downX;
    private float downY;
    private float currX;
    private float currY;
    private float topCardOffsetX;
    private float topCardOffsetY;
    private float scalePercent = 0f;
    private float rotatePercent = 0f;
    private Scroller mScroller;

    private int currentState;

    private RectF mContentRange;
    private boolean isBanAnim;

    private OnCardMovedListener listener;
    private static final double TAN_MAX_DEGREE = Math.tan(Math.toRadians(20));

    public XCardLayout(Context context) {
        super(context);
        init();
    }

    public XCardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XCardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        isBanAnim = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(ReciteConstants.PREF_RECITE_BAN_ANIM, false);
        height_increase_factor = UIUtils.dip2px(getContext(), 9);
        mContentRange = new RectF();
        mVelocityTracker = VelocityTracker.obtain();
        mScroller = new Scroller(getContext(), new LinearInterpolator());
        TOUCH_SLOP = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            params.topMargin = Math.max(params.topMargin, height_increase_factor * 2);
            int w = width - params.leftMargin - params.rightMargin;
            int h = height - getPaddingBottom() - params.topMargin - params.bottomMargin;
            child.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY)
                    , MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
        }
    }

    public void setChildBottomMargin(int px) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            params.bottomMargin = px;
        }
        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        max_move_distance = w / 3;
    }

    private int l, t, r, b;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.l = l;
        this.t = t;
        this.r = r;
        this.b = b;
        int childNum = getChildCount();
        int width = getWidth();
        int height = getHeight();
        if (childNum == 0) {
            mContentRange.setEmpty();
            return;
        }
        for (int i = 0; i < childNum; i++) {
            View view = getChildAt(i);
            view.setPivotX(width / 2);
            view.setPivotY(height);
            view.setRotation(0);
            view.setScaleX(1f);
            view.setEnabled(false);

            MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
            int vl = l + params.leftMargin;
            int vr = r - params.rightMargin;
            int vt = (int) (params.topMargin - (childNum - 1 - i - scalePercent) * height_increase_factor);
            int vb = vt + view.getMeasuredHeight();
            if (i == 0 && childNum > 3) {
                view.setAlpha(scalePercent);
            } else view.setAlpha(1f);
            if (i == childNum - 1) {
                view.setEnabled(true);
                int _w = vr - vl;
                int _h = vb - vt;
                vl += topCardOffsetX;
                vr = vl + _w;
                vt += topCardOffsetY;
                vb = vt + _h;
                view.layout(vl, vt, vr, vb);
                view.setRotation(MAX_ROTATE_DEGREE * rotatePercent);
                mContentRange.set(vl, vt, vr, vb);
            } else {
                view.layout(vl, vt, vr, vb);
                view.setScaleX(1 - (childNum - 1 - i - scalePercent) * SCALE_FACTOR);
            }
        }
    }

    private boolean canIntercept = true;                     //标志是否拦截，true执行代码判断是否拦截，false不拦截所有事件
    private boolean mTag;

    private boolean disallowIntercept = false;

    public void setDisallowIntercept(boolean disallowIntercept) {
        this.disallowIntercept = disallowIntercept;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (disallowIntercept) return false;
        if (isCardMoved) return true;

        boolean intercept = false;
        currX = ev.getX();
        currY = ev.getY();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                mTag = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(currX - downX) <= TOUCH_SLOP && Math.abs(currY - downY) <= TOUCH_SLOP)
                    return false;

                if (!canIntercept) {
                    if (!mTag) {
                        mTag = true;
                        if (Math.abs(currX - downX) / 3 < Math.abs(currY - downY)) {
                            return false;
                        } else {
                            ev.setAction(MotionEvent.ACTION_DOWN);
                            onTouchEvent(ev);
                            return true;
                        }
                    } else {
                        return false;
                    }

                }
                intercept = true;
                ev.setAction(MotionEvent.ACTION_DOWN);
                onTouchEvent(ev);
                break;
        }
        return intercept;
    }

    private boolean touchInContent = false;

    private boolean isCardMoved = false;
    private int maxVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
    private static final int MIN_VELOCITY = 3000;

    private boolean canTouch = true;

    private boolean hasDownEvent = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isCardMoved) return true;
        if (!canTouch) return false;
        boolean result = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            touchInContent = isTouchInContent(event);
            hasDownEvent = true;
        }

        if (touchInContent && hasDownEvent) {
            mVelocityTracker.addMovement(event);
            currX = event.getX();
            currY = event.getY();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = currX - downX;
                    float dy = currY - downY;

                    topCardOffsetX = dx;
                    topCardOffsetY = dy;
                    float distance = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                    scalePercent = computeMovePercent(distance);

                    rotatePercent = computeMovePercent(dx);
                    float angleValue = Math.abs(dx) / Math.abs(dy);
                    if (listener != null) {
                        if (angleValue > TAN_MAX_DEGREE) listener.xOffset(rotatePercent);
                        else listener.yOffset(computeMovePercent(dy));
                    }
                    onLayout(false, l, t, r, b);
                    break;
                case MotionEvent.ACTION_UP:
                    startScroll();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    startScroll();
                    break;
            }
            result = true;
        }
        return result;
    }

    private void startScroll() {
        hasDownEvent = false;
        isCardMoved = true;
        mVelocityTracker.computeCurrentVelocity(1000, maxVelocity);
        float xV = mVelocityTracker.getXVelocity();
        float yV = mVelocityTracker.getYVelocity();
        mVelocityTracker.clear();
        int startX = (int) topCardOffsetX;
        int startY = (int) topCardOffsetY;

        if (scalePercent >= 1f || Math.abs(xV) > MIN_VELOCITY || Math.abs(yV) > MIN_VELOCITY) {
            float dx = currX - downX;
            float dy = currY - downY;

            if (dx > 0 && dy > 0) currentState = dx / dy > TAN_MAX_DEGREE ? IN_RIGHT : IN_BOTTOM;
            else if (dx > 0 && dy < 0) currentState = dx / -dy > TAN_MAX_DEGREE ? IN_RIGHT : IN_TOP;
            else if (dx < 0 && dy > 0) currentState = -dx / dy > TAN_MAX_DEGREE ? IN_LEFT : IN_BOTTOM;
            else if (dx < 0 && dy < 0) currentState = dx / dy > TAN_MAX_DEGREE ? IN_LEFT : IN_TOP;
            else if (dx == 0) currentState = dy > 0 ? IN_BOTTOM : IN_TOP;
            else currentState = dx > 0 ? IN_RIGHT : IN_LEFT;

            switch (currentState) {
                case IN_LEFT:
                    dx = -getWidth();
                    dy = dy > 0 ? getHeight() : -getHeight();
                    break;
                case IN_RIGHT:
                    dx = getWidth();
                    dy = dy > 0 ? getHeight() : -getHeight();
                    break;
                case IN_TOP:
                    dx = 0;
                    dy = -getHeight();
                    break;
                case IN_BOTTOM:
                    dx = 0;
                    dy = getHeight();
                    break;
            }
            /*if (currentState == IN_BOTTOM) {
                mScroller.startScroll(startX, startY, -startX, -startY, 100);
                computeOffset();
                return;
            }*/

            mScroller.startScroll(startX, startY, (int) dx, (int) dy, 200);
            if (listener != null) {
                listener.preCardRemoved(currentState);
            }
        } else {
            mScroller.startScroll(startX, startY, -startX, -startY, 100);
        }
        computeOffset();
    }

    private void computeOffset() {
        if (mScroller.computeScrollOffset()) {
            topCardOffsetX = mScroller.getCurrX();
            topCardOffsetY = mScroller.getCurrY();
            float distance = (float) Math.hypot(topCardOffsetX, topCardOffsetY);
            scalePercent = computeMovePercent(distance);
            rotatePercent = computeMovePercent(topCardOffsetX);
            onLayout(false, l, t, r, b);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    computeOffset();
                }
            }, 24);
        } else if (mScroller.isFinished()) {
            if (listener != null) {
                listener.xOffset(0);
                listener.yOffset(0);
            }
            if (topCardOffsetX == 0 && topCardOffsetY == 0) {
                scalePercent = 0;
                rotatePercent = 0;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCardMoved = false;
                    }
                }, 100);
                return;
            }
            topCardOffsetX = 0;
            topCardOffsetY = 0;
            scalePercent = 0;
            rotatePercent = 0;
            showNext();
            isCardMoved = false;
        }
    }

    private float computeMovePercent(float distance) {
        float result;
        float percent = distance / max_move_distance;
        if (percent >= 0) {
            result = Math.min(percent, 1f);
        } else {
            result = Math.max(percent, -1f);
        }
        return result;
    }

    private boolean isTouchInContent(MotionEvent ev) {
        return mContentRange.contains(ev.getX(), ev.getY());
    }

    /**
     * show next card
     *
     * @param state the orientation which one of the
     *              {@link XCardLayout}:
     *              {@link XCardLayout#IN_RIGHT},
     *              {@link XCardLayout#IN_LEFT}.
     *              {@link XCardLayout#IN_TOP}
     *              mean which state to current card
     */
    public void showNextCard(int state) {
        if (isCardMoved) return;
        int dx = 0;
        int dy = 0;
        currentState = state;
        switch (state) {
            case IN_LEFT:
                dx -= getWidth();
                if (listener != null) listener.xOffset(-1f);
                break;
            case IN_RIGHT:
                dx += getWidth();
                if (listener != null) listener.xOffset(1f);
                break;
            case IN_TOP:
                dy -= getHeight();
                if (listener != null) listener.yOffset(-1f);
                break;
            case IN_BOTTOM:
                dy += getHeight();
                if (listener != null) listener.yOffset(1f);
                break;
        }
        if (listener != null) {
            listener.preCardRemoved(currentState);
        }
        if (isBanAnim) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.xOffset(0);
                        listener.yOffset(0);
                    }
                    showNext();
                }
            }, 100);
        } else {
            final int finalDx = dx;
            final int finalDy = dy;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScroller.startScroll(0, 0, finalDx, finalDy, 300);
                    isCardMoved = true;
                    computeOffset();

                }
            }, 100);
        }
    }

    private void showNext() {
        if (mAdapter == null || (host != null && host.isFinishing())) return;
        int index = getChildCount() - 1;
        if (index < 0) return;
        setAdapterTag(index - 1);
        View convertView = getChildAt(index);
        removeView(convertView);
        if (listener != null) {
            listener.onCardRemoved(mCardData, currentState);
        }
        if (mAdapter.getCount() > getChildCount()) {
            View view = mAdapter.getView(convertView, this);
            initAddView(view);
        }
        bindData();
    }

    private Activity host;
    public void setHostActivity(Activity host) {
        this.host = host;
    }

    private void initAddView(View view) {
        if (view == null) return;
        view.setEnabled(false);
        addView(view, 0);
    }

    public XCardAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(XCardAdapter mAdapter) {
        if (mAdapter == null) {
            return;
        }
        this.mAdapter = mAdapter;
        removeAllViews();
        int initCount = Math.min(MAX_CARD_NUM, mAdapter.getCount());
        for (int i = 0; i < initCount; i++) {
            View view = mAdapter.getView(null, this);
            initAddView(view);
        }
        setAdapterTag(getChildCount() - 1);
        bindData();
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p != null && p instanceof MarginLayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        MarginLayoutParams params = new MarginLayoutParams(p);
        if (p instanceof MarginLayoutParams) {
            params.setMargins(((MarginLayoutParams) p).leftMargin, ((MarginLayoutParams) p).topMargin,
                    ((MarginLayoutParams) p).rightMargin, ((MarginLayoutParams) p).bottomMargin);
        }
        return params;
    }

    public interface OnCardMovedListener {
        void xOffset(float offset);

        void yOffset(float offset);

        void onCardRemoved(ReciteCard card, int state);

        void preCardRemoved(int state);
    }

    public void setOnCardMovedListener(OnCardMovedListener listener) {
        this.listener = listener;
    }

    private ReciteCard mCardData;

    private void bindData() {
        if (getChildCount() == 0) return;
        mCardData = mAdapter.bindData(false);
    }

    private void setAdapterTag(int index) {
        if (index < 0) return;
        View view = getChildAt(index);
        mAdapter.setTopView(view);
    }

    /**
     * 设为true表示需要执行代码判断是否拦截，false表示不拦截上下滑动，拦截左右滑动
     */
    public void setCanIntercept(boolean canIntercept) {
        this.canIntercept = canIntercept;
    }

    public void setCanTouch(boolean canTouch) {
        this.canTouch = canTouch;
    }
}
