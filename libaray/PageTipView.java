package com.eusoft.recite.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.eusoft.dict.util.UIUtils;

/**
 * Created by xj on 2017/3/14.
 * viewpager小圆点指示条
 */

public class PageTipView extends View implements ViewPager.OnPageChangeListener {
    private int circleRadius;
    private int normalColor;
    private int selectedColor;
    private int tipPadding;
    private Paint mPaint;
    private ViewPager viewPager;

    public PageTipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        circleRadius = UIUtils.dip2px(getContext(), 3);
        normalColor = Color.GRAY;
        selectedColor = Color.DKGRAY;
        tipPadding = UIUtils.dip2px(getContext(), 8);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (viewPager == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            int count = viewPager.getAdapter().getCount();
            widthSize = Math.min(widthSize, count * circleRadius * 2 + (count - 1) * tipPadding + getPaddingLeft() + getPaddingRight());
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = Math.min(heightSize, circleRadius * 2 + getPaddingTop() + getPaddingBottom());
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    private final Rect rect = new Rect();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewPager != null) {
            int count = viewPager.getAdapter().getCount();
            rect.top = getPaddingTop();
            rect.bottom = rect.top + circleRadius * 2;
            rect.left = getPaddingLeft();
            mPaint.setColor(normalColor);
            for (int i = 0; i < count; i++) {
                rect.right = rect.left + circleRadius * 2;
                canvas.drawCircle(rect.centerX(), rect.centerY(), circleRadius, mPaint);
                rect.left = rect.right + tipPadding;
            }
            mPaint.setColor(selectedColor);
            rect.left = (int) (getPaddingLeft() + offset * (tipPadding + circleRadius * 2));
            rect.right = rect.left + circleRadius * 2;
            canvas.drawCircle(rect.centerX(), rect.centerY(), circleRadius, mPaint);
        }
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
    }

    public void setNormalColor(int id) {
        this.normalColor = getResources().getColor(id);
    }

    public void setSelectedColor(int id) {
        this.selectedColor = getResources().getColor(id);
    }

    public void setTipPadding(int tipPadding) {
        this.tipPadding = tipPadding;
    }

    public void setUpWithViewPager(ViewPager viewPager) {
        if (viewPager.getAdapter() == null) {
            throw new IllegalArgumentException("viewPage must be set adapter");
        }
        this.viewPager = viewPager;
        this.viewPager.addOnPageChangeListener(this);
    }

    private float offset;
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        offset = positionOffset + position;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
