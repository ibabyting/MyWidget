package com.eusoft.recite.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.eusoft.dict.R;
import com.eusoft.dict.util.CommonUtil.ScreenUtil;

/**
 * Created by xj on 2016/12/15.
 */

public class XIndexView extends View {

    private Paint mPaint;
    private int textSize;
    private int textColor;
    private boolean isBlod;
    private int textPadding;
    private Character[] datas;
    private RectF rectF;
    private int itemH;
    private Paint.FontMetrics fontMetrics;
    private OnIndexTouchListener listener;

    public XIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resloveAttrs(context, attrs);
        initPaint();
        datas = new Character[0];
        rectF = new RectF();
    }

    private void resloveAttrs(Context context, AttributeSet attrs) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.XIndexView);
        textSize = ta.getDimensionPixelSize(R.styleable.XIndexView_textSize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, metrics));
        textColor = ta.getColor(R.styleable.XIndexView_textColor, Color.BLUE);
        isBlod = ta.getBoolean(R.styleable.XIndexView_textBold, true);
        textPadding = ta.getDimensionPixelSize(R.styleable.XIndexView_textPadding,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, metrics));
        ta.recycle();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);
        mPaint.setStrokeWidth(ScreenUtil.dp2px(getContext(), 2));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setFakeBoldText(isBlod);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        fontMetrics = mPaint.getFontMetrics();
        int width = (int) (mPaint.measureText("X") * 2.0f) + textPadding * 2;
        itemH = (int) ((fontMetrics.bottom - fontMetrics.top));
        int height = itemH * datas.length;
        setMeasuredDimension(width, height);
    }

    private int oldIndex = -1;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (listener == null) return super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN ||
                event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float y = event.getY();
            int index = (int) (y / itemH);
            if (index >= 0 && index != oldIndex && index < datas.length) {
                listener.onIndexTouched(index);
                oldIndex = index;
            }
        }else {
            oldIndex = -1;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rectF.set(0, 0, getMeasuredWidth(), itemH);
        for (Character c : datas) {
            float baseLine = (rectF.bottom + rectF.top - fontMetrics.bottom - fontMetrics.top) / 2;
            canvas.drawText(c.toString(), rectF.width() / 2, baseLine, mPaint);
            rectF.top = rectF.bottom;
            rectF.bottom += itemH;
        }
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setBlod(boolean blod) {
        isBlod = blod;
    }

    public void setTextPadding(int textPadding) {
        this.textPadding = textPadding;
    }

    public void setDatas(Character[] datas) {
        this.datas = datas;
        requestLayout();
        invalidate();
    }

    public void setOnIndexTouchListener(OnIndexTouchListener listener) {
        this.listener = listener;
    }

    public interface OnIndexTouchListener {
        /**
         * index not container search icon*/
        void onIndexTouched(int index);
    }
}
