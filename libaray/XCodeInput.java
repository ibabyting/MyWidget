package com.eusoft.recite.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.eusoft.dict.R;
import com.eusoft.recite.model.FixedStack;
import com.eusoft.recite.model.Underline;

import java.util.regex.Pattern;

/**
 * @author xujie
 * 自定义字符输入框，自动跳到下一格，辅以动画
 */
public class XCodeInput extends View {

    private static final int DEFAULT_CODES = 6;
    private static final Pattern KEYCODE_PATTERN = Pattern.compile("\\w");
    private FixedStack<Character> characters;
    private Underline underlines[];
    private Paint mPaint;
    private float underlineReduction;
    private float underlineStrokeWidth;
    private float charItemWidth;
    private float charItemHeight;
    private float textPadding;
    private int charAmount;
    private int underlineColor;
    private int underlineSelectedColor;
    private int textColor;
    private Paint.FontMetrics metrics;

    private CodeReadyListener listener;
    private InputMethodManager inputmethodmanager;


    public XCodeInput(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        init(attributeset);
    }


    public XCodeInput(Context context, AttributeSet attributeset, int defStyledAttrs) {
        super(context, attributeset, defStyledAttrs);
        init(attributeset);
    }

    public void setCodeReadyListener(CodeReadyListener listener) {
        this.listener = listener;
    }

    private void init(AttributeSet attributeset) {
        inputmethodmanager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        initAttributes(attributeset);
        initDataStructures();
        initViewOptions();
    }

    public interface CodeReadyListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        void onCodeReady(Character[] code);

    }

    private void initAttributes(AttributeSet attributeset) {
        underlineStrokeWidth = dp2px(1);
        charAmount = DEFAULT_CODES;
        TypedArray ta =
                getContext().obtainStyledAttributes(attributeset, R.styleable.core_area);
        underlineColor = ta.getColor(R.styleable.core_area_underline_color, Color.parseColor("#cccccc"));
        underlineSelectedColor =
                ta.getColor(R.styleable.core_area_underline_selected_color, Color.parseColor("#000000"));
        float textSize = ta.getDimensionPixelSize(R.styleable.core_area_text_size, dp2sp(14));
        textColor = ta.getInt(R.styleable.core_area_text_color, Color.parseColor("#000000"));
        textPadding = ta.getDimensionPixelSize(R.styleable.core_area_text_padding, dp2px(5));
        ta.recycle();

        underlineReduction = textPadding;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(textSize);
        mPaint.setFakeBoldText(true);
        mPaint.setTextAlign(Paint.Align.CENTER);

        int fontWidth = (int) (mPaint.measureText("X") * 2.5f);
        charItemWidth = fontWidth + 2 * textPadding;
        metrics = mPaint.getFontMetrics();
    }

    private void initDataStructures() {
        if (characters == null) {
            characters = new FixedStack<>();
        } else characters.clear();
        characters.setMaxSize(charAmount);
    }

    private void initViewOptions() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = Math.max(MeasureSpec.getSize(widthMeasureSpec), dp2px(100));
        charItemHeight = (int) (metrics.bottom - metrics.top + textPadding * 2);
        float width = charAmount * charItemWidth;
        float height;
        if (width > maxWidth) {
            int i = 1;
            while (width > maxWidth * i) {
                i++;
            }
            width = maxWidth;
            height = charItemHeight * i;
        } else {
            height = charItemHeight;
        }
        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        underlines = new Underline[charAmount];

        int numOfLine = (int) (w / charItemWidth);
        int t = charAmount / numOfLine;
        float offset = (w - numOfLine * charItemWidth) / 2;
        for (int i = 1; i <= t; i++) {
            for (int j = 0; j < numOfLine; j++) {
                int index = (i - 1) * numOfLine + j;
                float x = j * charItemWidth + offset;
                underlines[index] = new Underline(x + underlineReduction, charItemHeight * i - underlineStrokeWidth
                        , x + charItemWidth - underlineReduction, charItemHeight * i - underlineStrokeWidth);
            }
        }
        int surplusNum = charAmount - t * numOfLine;
        offset = (w - surplusNum * charItemWidth) / 2;
        for (int i = 0; i < surplusNum; i++) {
            int index = t * numOfLine + i;
            float x = i * charItemWidth + offset;
            underlines[index] = new Underline(x + underlineReduction, h - underlineStrokeWidth,
                    x + charItemWidth - underlineReduction, h - underlineStrokeWidth);
        }
    }

    public void setCharNum(int num) {
        charAmount = num;
        initDataStructures();
        requestLayout();
        invalidate();
    }

    public void showKeyboard() {
        requestFocus();
        boolean result = inputmethodmanager.showSoftInput(XCodeInput.this, InputMethodManager.SHOW_IMPLICIT);
        if (result) inputmethodmanager.viewClicked(XCodeInput.this);
        else postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard();
            }
        }, 100);
    }

    public void hideKeyBoard() {
        inputmethodmanager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.RESULT_UNCHANGED_HIDDEN);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.actionLabel = null;
        outAttrs.inputType |= InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new BaseInputConnection(this, true){
            @Override
            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                return true;
            }

            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                if (!TextUtils.isEmpty(text.toString())) {
                    inputText(text);
                }
                return true;
            }

            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    delete();
                }
                return super.sendKeyEvent(event);
            }

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                delete();
                return true;
            }

            private void delete() {
                if (characters.size() != 0) {
                    characters.pop();
                    invalidate();
                }
            }
        };
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public boolean checkInputConnectionProxy(View view) {
        return true;
    }

    /**
     * String text
     * Pass empty string to remove text
     *
     * @param text text to input
     */
    private void inputText(CharSequence text) {
        char[] cs = text.toString().toCharArray();
        for (char c : cs) {
            boolean result = characters.pushElement(c);
            if (result) {
                invalidate();
                if (characters.size() >= charAmount && listener != null) {
                    listener.onCodeReady(getCode());
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < underlines.length; i++) {
            Underline sectionpath = underlines[i];
            float fromX = sectionpath.getFromX();
            float fromY = sectionpath.getFromY();
            float toX = sectionpath.getToX();
            float toY = sectionpath.getToY();
            drawSection(i, fromX, fromY, toX, toY, canvas);
            if (characters.size() > i) {
                drawCharacter(fromX, toX, fromY, characters.get(i), canvas);
            }
        }
    }

    public void setTypeface(Typeface typeface) {
        mPaint.setTypeface(typeface);
    }

    private void drawSection(int position, float fromX, float fromY, float toX, float toY,
                             Canvas canvas) {
        mPaint.setColor(underlineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(underlineStrokeWidth);
        if (position == characters.size()) {
            mPaint.setColor(underlineSelectedColor);
        }
        canvas.drawLine(fromX, fromY, toX, toY, mPaint);
    }

    private void drawCharacter(float fromX, float toX, float fromY, Character character, Canvas canvas) {
        mPaint.setColor(textColor);
        mPaint.setStyle(Paint.Style.FILL);
        float actualWidth = toX - fromX;
        float centerWidth = actualWidth / 2;
        float centerX = fromX + centerWidth;
        float baseline = (fromY + fromY - charItemHeight - metrics.bottom - metrics.top) / 2;
        canvas.drawText(character.toString(), centerX, baseline, mPaint);
    }

    public Character[] getCode() {
        return characters.toArray(new Character[charAmount]);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    private int dp2sp(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp,
                getContext().getResources().getDisplayMetrics());
    }
}
