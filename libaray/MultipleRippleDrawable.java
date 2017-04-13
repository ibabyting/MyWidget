package com.eusoft.dict.ui.widget;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;

public  class MultipleRippleDrawable extends Drawable implements Animatable {
    private ArrayList<AnimatorSet> mAnimators;
    private int alpha = 255;
    private static final Rect ZERO_BOUNDS_RECT = new Rect();
    protected Rect drawBounds = ZERO_BOUNDS_RECT;

    private boolean mHasAnimators;

    private Paint mPaint=new Paint();

    public MultipleRippleDrawable(){
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    public int getColor() {
        return mPaint.getColor();
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    @Override
    public int getAlpha() {
        return alpha;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public void draw(Canvas canvas) {
        draw(canvas,mPaint);
    }

    public  void draw(Canvas canvas, Paint paint) {
        float circleSpacing=4;
        for (int i = 0; i < delays.length; i++) {
            canvas.save();
            paint.setAlpha(alphaInts[i]);
            canvas.scale(scaleFloats[i],scaleFloats[i],getWidth()/2,getHeight()/2);
            canvas.drawCircle(getWidth()/2,getHeight()/2,getWidth()/2-circleSpacing,paint);
            canvas.restore();
        }
    }

    int[] alphaInts = {0,0,0,0,0,0};
    float[] scaleFloats = {0,0,0,0,0,0};
    long[] delays = {0, 600,1200};

    public  ArrayList<AnimatorSet> onCreateAnimators() {
        ArrayList<AnimatorSet> list = new ArrayList<>();
        for (int i =0; i < delays.length; i++) {
            AnimatorSet set = new AnimatorSet();
            set.setInterpolator(new DecelerateInterpolator());
            set.setDuration(2200);
            set.setStartDelay(delays[i]);
            final int index = i;

            ValueAnimator scaleAnim=ValueAnimator.ofFloat(0f, 1f);
            scaleAnim.setRepeatMode(ValueAnimator.RESTART);
            scaleAnim.setRepeatCount(ValueAnimator.INFINITE);
            scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    scaleFloats[index] = v;
                    postInvalidate();
                }
            });

            ValueAnimator alphaAnim=ValueAnimator.ofInt(120,0);
            alphaAnim.setRepeatMode(ValueAnimator.RESTART);
            alphaAnim.setRepeatCount(ValueAnimator.INFINITE);
            alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int v = (int) animation.getAnimatedValue();
                    alphaInts[index] = v;
                    postInvalidate();
                }
            });

            set.playTogether(scaleAnim, alphaAnim);
            list.add(set);
        }

        return list;
    }

    @Override
    public void start() {
        ensureAnimators();

        if (mAnimators == null) {
            return;
        }

        if (isStarted()) {
            return;
        }
        startAnimators();
        invalidateSelf();
    }
    
    private void startAnimators() {
        for (int i = 0; i < mAnimators.size(); i++) {
            AnimatorSet animator = mAnimators.get(i);
            animator.start();
        }
    }

    private void stopAnimators() {
        if (mAnimators!=null){
            for (AnimatorSet animator : mAnimators) {
                if (animator != null && animator.isStarted()) {
                    animator.end();
                }
            }
        }
    }

    private void ensureAnimators() {
        if (!mHasAnimators) {
            mAnimators = onCreateAnimators();
            mHasAnimators = true;
        }
    }

    @Override
    public void stop() {
        stopAnimators();
    }

    private boolean isStarted() {
        for (AnimatorSet animator : mAnimators) {
            if (animator.isStarted()) return true;
        }
        return false;
    }

    @Override
    public boolean isRunning() {
        for (AnimatorSet animator : mAnimators) {
            if (animator.isRunning()) return true;
        }
        return false;
    }


    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        setDrawBounds(bounds);
    }

    public void setDrawBounds(Rect drawBounds) {
        setDrawBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom);
    }

    public void setDrawBounds(int left, int top, int right, int bottom) {
        this.drawBounds = new Rect(left, top, right, bottom);
    }

    public void postInvalidate(){
        invalidateSelf();
    }

    public int getWidth(){
        return drawBounds.width();
    }

    public int getHeight(){
        return drawBounds.height();
    }

}
