package com.eusoft.recite.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by xj on 2017/1/10.
 * 专用于horizontal两子view适配
 */

public class WrapLinearLayout extends LinearLayout {

    public WrapLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View child_1 = getChildAt(0);
        View child_2 = getChildAt(1);
        int width = getMeasuredWidth() - getPaddingLeft()
                - getPaddingRight() - getHorizontalMargin(child_1)
                - getHorizontalMargin(child_2);
        int halfWidth = width / 2;
        if (child_1.getMeasuredWidth() >= halfWidth || child_2.getMeasuredWidth() >= halfWidth) {
            makeZeroToHorizontalMargin(child_1);
            makeZeroToHorizontalMargin(child_2);
            setOrientation(VERTICAL);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int getHorizontalMargin(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof MarginLayoutParams) {
            MarginLayoutParams p = (MarginLayoutParams) params;
            return p.leftMargin + p.rightMargin;
        }
        return 0;
    }

    private void makeZeroToHorizontalMargin(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof MarginLayoutParams) {
            MarginLayoutParams p = (MarginLayoutParams) params;
            p.leftMargin = p.rightMargin = 0;
        }
    }
}
