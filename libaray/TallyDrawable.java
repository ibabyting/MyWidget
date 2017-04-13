package com.eusoft.recite.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.animation.LinearInterpolator;

import com.eusoft.dict.MainApplication;
import com.eusoft.dict.R;
import com.eusoft.dict.util.CommonUtil.ScreenUtil;

/**
 * Created by xj on 2016/12/6.
 * 计数条drawable
 */

public class TallyDrawable extends Drawable implements ValueAnimator.AnimatorUpdateListener {

    private Context mContext;
    private RectF mRect;
    private Paint mPaint;
    private int textSize;
    private int roundRadius;
    private float[] values = new float[0];
    private int[] colors;
    private float totalNum;
    private int textColor;
    private PorterDuffXfermode xfermode;
    private PorterDuffXfermode xfermode1;
    private ValueAnimator animator;
    private float[] cache;

    public TallyDrawable(@NonNull Context mContext) {
        this.mContext = mContext;
        initPaint(mContext);
        initAnim();
    }

    private void initPaint(@NonNull Context mContext) {
        mRect = new RectF();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStyle(Paint.Style.FILL);
        textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
                mContext.getResources().getDisplayMetrics());
        mPaint.setTextSize(textSize);
        xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        xfermode1 = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
        textColor = mContext.getResources().getColor(R.color.text_type7);
        roundRadius = ScreenUtil.dp2px(mContext, 3);
    }

    private void initAnim() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(500);
        animator.addUpdateListener(this);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (colors == null || colors.length == 0) return;
        if (values == null || values.length == 0) return;
        int index = canvas.saveLayer(mRect.left, mRect.top, mRect.right, mRect.bottom, mPaint, Canvas.ALL_SAVE_FLAG);
        int bg = colors[colors.length - 1];
        float width = mRect.width();
        float currX = mRect.left;
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(bg);
        canvas.drawRoundRect(mRect, roundRadius, roundRadius, mPaint);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        RectF rectF = new RectF();
        for (int i = 0; i < values.length; i++) {
            if (i != values.length - 1) {
                float percent = values[i] / totalNum;
                mPaint.setColor(colors[i]);
                mPaint.setXfermode(xfermode);
                float left = currX;
                currX += width * percent;
                canvas.drawRect(left, mRect.top, currX, mRect.bottom, mPaint);
                rectF.set(left, mRect.top, currX, mRect.bottom);
            } else {
                rectF.left = currX;
                rectF.right = mRect.right;
            }
            float baseLine = (mRect.bottom + mRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
            String str = String.valueOf(Math.round(values[i]));
            float textWidth = mPaint.measureText(str);
            mPaint.setXfermode(xfermode1);
            mPaint.setColor(textColor);
            if (rectF.width() > textWidth * 1.1f) {
                canvas.drawText(str, rectF.left + rectF.width() / 2, baseLine, mPaint);
            }
        }
        canvas.restoreToCount(index);
    }

    private float[] diffValues;

    public void setDisplayValues(float[] values, @ColorInt int[] colors) {
        if (animator.isRunning()) animator.cancel();
        if (values.length != colors.length) {
            throw new IllegalArgumentException("values.length should equals colors.length");
        }
        diffValues = new float[values.length];
        cache = new float[values.length];
        System.arraycopy(this.values, 0, cache, 0, this.values.length);
        for (int i = 0; i < values.length; i++) {
            diffValues[i] = values[i] - cache[i];
        }
        this.values = cache.clone();
        this.colors = colors;
        totalNum = 0;
        for (float value : values) {
            totalNum += value;
        }
        MainApplication.mHandler.post(new Runnable() {
            @Override
            public void run() {
                animator.start();
            }
        });
    }
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float factor = animation.getAnimatedFraction();
        for (int i = 0; i < values.length; i++) {
            values[i] = cache[i] + diffValues[i] * factor;
        }
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        int height = getIntrinsicHeight();
        int h = bottom - top;
        top = top + (h - height) / 2;
        bottom = top + height;
        mRect.set(left, top, right, bottom);
    }

    @Override
    public void setBounds(@NonNull Rect bounds) {
        super.setBounds(bounds);
        int height = getIntrinsicHeight();
        int h = bounds.bottom - bounds.top;
        bounds.top = bounds.top + (h - height) / 2;
        bounds.bottom = bounds.top + height;
        mRect.set(bounds);
    }

    @Override
    public int getIntrinsicWidth() {
        return ScreenUtil.dp2px(mContext, 200);
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) (textSize * 1.3f);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

}
