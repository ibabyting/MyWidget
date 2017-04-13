package com.eusoft.dict.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.eusoft.dict.R;
import com.eusoft.dict.util.UIUtils;

/**
 * Created by xj on 2017/1/13.
 * 悬浮可移动菜单按钮
 */

public class XFloatingPopMenu extends ViewGroup implements View.OnClickListener {
    private OnPopMenuClickListener listener;
    private int orientation;
    private boolean isOpen;
    private int[] ids = new int[]{R.id.menu_id_top, R.id.menu_id_pre_page, R.id.menu_id_next_page,
            R.id.menu_id_pre_word, R.id.menu_id_next_word};
    private int[] imgResId = new int[]{R.drawable.pop_top_icon, R.drawable.pop_up_icon, R.drawable.pop_down_icon,
            R.drawable.pop_previous_icon, R.drawable.pop_next_icon};
    private int[] imgWhiteResId = new int[]{R.drawable.sel_top_icon, R.drawable.sel_up_icon, R.drawable.sel_down_icon,
            R.drawable.sel_previous_icon, R.drawable.sel_next_icon};
    private int paddingPx;
    private int marginPx;
    private ObjectAnimator openAnim;
    private ObjectAnimator closeAnim;
    private LinearLayout[] groups;
    private ImageView switchView;
    private ImageView[] imageViews;
    private int state = -1;
    public static final int IN_TOP = 0;
    public static final int IN_BOTTOM = 1;
    public static final int IN_LEFT = 2;
    public static final int IN_RIGHT = 3;
    public static final int IN_RIPPLE = 4;

    public XFloatingPopMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        paddingPx = UIUtils.dip2px(getContext(), 12);
        marginPx = UIUtils.dip2px(getContext(), 16);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        generateChildView();
    }

    private void generateChildView() {
        imageViews = new ImageView[5];
        for (int i = 0; i < 5; i++) {
            ImageView view = getView(ids[i], i);
            view.setImageResource(imgResId[i]);
            imageViews[i] = view;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        switchView = getView(R.id.menu_id_switch, 0);
        addView(switchView);
        switchView.setImageResource(R.drawable.pop_open_icon);
    }

    private ImageView getView(int id, final int position) {
        ImageView iv = new ImageView(getContext());
        iv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        iv.setClickable(true);
        iv.setOnClickListener(this);
        iv.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        iv.setId(id);
        if (id != R.id.menu_id_switch) {
            iv.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        ImageView iv = (ImageView) v;
                        iv.setBackgroundResource(R.color.colorPrimary);
                        iv.setImageResource(imgWhiteResId[position]);
                    }
                    if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                            event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                        ImageView iv = (ImageView) v;
                        iv.setBackgroundResource(0);
                        iv.setImageResource(imgResId[position]);
                    }
                    return false;
                }
            });
        }
        return iv;
    }

    //根据位置重新确定添加view的顺序
    private void updateViews() {
        LinearLayout group_1 = new LinearLayout(getContext());
        LayoutParams params;
        int maxLength = switchView.getMeasuredWidth() * 6 + marginPx;
        ViewGroup parent = (ViewGroup) getParent();
        if (getTop() >= maxLength) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, 0);
            orientation = LinearLayout.VERTICAL;
            //往上添加group
            state = IN_TOP;
            group_1.setLayoutParams(params);
            addChildViews(group_1, 0, 4);
        } else if (parent.getHeight() - getBottom() >= maxLength) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, 0);
            orientation = LinearLayout.VERTICAL;
            //往下添加group
            state = IN_BOTTOM;
            group_1.setLayoutParams(params);
            addChildViews(group_1, 0, 4);
        } else if (getLeft() >= maxLength) {
            orientation = LinearLayout.HORIZONTAL;
            params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
            //左边添加group
            state = IN_LEFT;
            group_1.setLayoutParams(params);
            addChildViews(group_1, 0, 4);
        } else if (parent.getWidth() - getRight() >= maxLength) {
            params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
            orientation = LinearLayout.HORIZONTAL;
            //右边添加group
            state = IN_RIGHT;
            group_1.setLayoutParams(params);
            addChildViews(group_1, 0, 4);
        } else {
            params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
            orientation = LinearLayout.HORIZONTAL;
            //两边添加group
            state = IN_RIPPLE;
            int childNum_1 = (getLeft() - marginPx) / switchView.getMeasuredWidth();
            group_1.setLayoutParams(params);
            addChildViews(group_1, 0, childNum_1 - 1);

            LinearLayout group_2 = new LinearLayout(getContext());
            LayoutParams params_2 = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
            group_2.setLayoutParams(params_2);
            addChildViews(group_2, childNum_1, 4);
            groups = new LinearLayout[]{group_1, group_2};
            return;
        }
        groups = new LinearLayout[]{group_1};
    }

    private void addChildViews(LinearLayout layout, int start, int end) {
        layout.setOrientation(orientation);
        for (int i = start; i <= end; i++) {
            ImageView view = imageViews[i];
            layout.addView(view);
        }
        addView(layout);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View child_1, child_2;
        if (getChildCount() == 1) {
            View child = getChildAt(0);
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            return;
        }
        switch (state) {
            case IN_LEFT:
                child_1 = getChildAt(1);
                child_1.layout(0, 0, child_1.getMeasuredWidth(), child_1.getMeasuredHeight());
                child_2 = getChildAt(0);
                child_2.layout(child_1.getRight(), 0, child_1.getRight() + child_2.getMeasuredWidth(), child_2.getMeasuredHeight());
                break;
            case IN_RIGHT:
                child_1 = getChildAt(0);
                child_1.layout(0, 0, child_1.getMeasuredWidth(), child_1.getMeasuredHeight());
                child_2 = getChildAt(1);
                child_2.layout(child_1.getRight(), 0, child_1.getRight() + child_2.getMeasuredWidth(), child_2.getMeasuredHeight());
                break;
            case IN_TOP:
                child_1 = getChildAt(1);
                child_1.layout(0, 0, child_1.getMeasuredWidth(), child_1.getMeasuredHeight());
                child_2 = getChildAt(0);
                child_2.layout(0, child_1.getBottom(), child_2.getMeasuredWidth(), child_1.getBottom() + child_2.getMeasuredHeight());
                break;
            case IN_BOTTOM:
                child_1 = getChildAt(0);
                child_1.layout(0, 0, child_1.getMeasuredWidth(), child_1.getMeasuredHeight());
                child_2 = getChildAt(1);
                child_2.layout(0, child_1.getBottom(), child_2.getMeasuredWidth(), child_1.getBottom() + child_2.getMeasuredHeight());
                break;
            case IN_RIPPLE:
                child_1 = getChildAt(1);
                child_1.layout(0, 0, child_1.getMeasuredWidth(), child_1.getMeasuredHeight());
                child_2 = getChildAt(0);
                child_2.layout(child_1.getRight(), 0, child_1.getRight() + child_2.getMeasuredWidth(), child_2.getMeasuredHeight());
                View child_3 = getChildAt(2);
                child_3.layout(child_2.getRight(), 0, child_2.getRight() + child_3.getMeasuredWidth(), child_3.getMeasuredHeight());
                break;
            default:
                View child = getChildAt(0);
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0, height = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams params = child.getLayoutParams();
            if (orientation == LinearLayout.HORIZONTAL) {
                child.measure(MeasureSpec.makeMeasureSpec(params.width, MeasureSpec.EXACTLY), heightMeasureSpec);
                height = Math.max(height, child.getMeasuredHeight());
                width += child.getMeasuredWidth();
            } else {
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
                width = Math.max(width, child.getMeasuredWidth());
                height += child.getMeasuredHeight();
            }
        }
        setMeasuredDimension(width, height);
    }

    //region 事件处理
    private float oldX;
    private float oldY;
    private boolean isSlopChecked;
    private static int touchSlop;

    /**
     * 判断是否能处理move事件
     */
    private boolean canMove(MotionEvent event) {
        if (!isSlopChecked) {
            if (Math.abs(event.getRawY() - oldY) > touchSlop ||
                    Math.abs(event.getRawX() - oldX) > touchSlop) {
                isSlopChecked = true;
                oldX = event.getRawX();
                oldY = event.getRawY();
                return true;
            } else return false;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ViewParent parent = getParent();
            while ((parent = parent.getParent()) != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        if (!isEnabled()) return false;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            isSlopChecked = false;
            oldX = ev.getRawX();
            oldY = ev.getRawY();
        } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
            return canMove(ev);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - oldX;
                float dy = event.getRawY() - oldY;
                ViewGroup parent = (ViewGroup) getParent();
                int l = getLeft();
                int r;
                if (l + dx < marginPx) {
                    l = marginPx;
                    r = l + getWidth();
                } else {
                    if (l + dx + getWidth() > parent.getWidth() - marginPx) {
                        r = parent.getWidth() - marginPx;
                        l = r - getWidth();
                    } else {
                        l = (int) (l + dx);
                        r = l + getWidth();
                    }
                }
                int t = getTop();
                int b;
                if (t + dy < marginPx) {
                    t = marginPx;
                    b = t + getHeight();
                } else {
                    if (t + dy + getHeight() > parent.getHeight() - marginPx) {
                        b = parent.getHeight() - marginPx;
                        t = b - getHeight();
                    } else {
                        t = (int) (t + dy);
                        b = t + getHeight();
                    }
                }
                layout(l, t, r, b);
                break;
        }
        oldX = event.getRawX();
        oldY = event.getRawY();
        return super.onTouchEvent(event);
    }
    //endregion

    public void setOnPopMenuClickListener(OnPopMenuClickListener listener) {
        this.listener = listener;
    }

    protected boolean needLayout = true;

    public void initLocationAndState(final boolean needOpen, final int x, final int y) {
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                layout(x, y, x + getMeasuredWidth(), y + getMeasuredHeight());
                needLayout = false;
                if (needOpen) openMenu();
            }
        });
    }

    private void openMenu() {
        if (isOpen) return;
        setEnabled(false);
        isOpen = true;
        updateViews();
        getChildAt(0).setRotation(135);
        for (LinearLayout child : groups) {
            if (orientation == LinearLayout.HORIZONTAL) {
                child.getLayoutParams().width = child.getChildCount() * switchView.getMeasuredWidth();
            } else {
                child.getLayoutParams().height = child.getChildCount() * switchView.getMeasuredHeight();
            }
        }
        requestLayout();
        setEnabled(true);
    }

    public boolean isMenuOpen() {
        return isOpen;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.menu_id_switch) {
            v.setPivotX(v.getMeasuredWidth() / 2);
            v.setPivotY(v.getMeasuredHeight() / 2);
            if (isOpen) startCloseAnim(v);
            else startOpenAnim(v);
            return;
        }
        if (listener == null) return;
        if (id == R.id.menu_id_top) listener.onGoTopClick();
        else if (id == R.id.menu_id_pre_page) listener.onPrePageClick();
        else if (id == R.id.menu_id_next_page) listener.onNextPageClick();
        else if (id == R.id.menu_id_pre_word) listener.onPreWordClick();
        else if (id == R.id.menu_id_next_word) listener.onNextWordClick();
    }

    private void startOpenAnim(View view) {
        updateViews();
        if (openAnim == null) {
            openAnim = ObjectAnimator.ofFloat(view, "rotation", 0, 135);
        }
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isOpen = true;
                setEnabled(true);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                setEnabled(false);
            }
        });
        AnimatorSet.Builder builder = set.play(openAnim);
        for (LinearLayout child : groups) {
            ObjectAnimator animator;
            if (orientation == LinearLayout.HORIZONTAL) {
                animator = ObjectAnimator.ofInt(new ViewWrapper(child),
                        "width", 0, child.getChildCount() * switchView.getMeasuredWidth());
            } else {
                animator = ObjectAnimator.ofInt(new ViewWrapper(child),
                        "height", 0, child.getChildCount() * switchView.getMeasuredHeight());
            }
            builder.with(animator);
        }
        set.start();
    }

    private void startCloseAnim(View view) {
        if (closeAnim == null) {
            closeAnim = ObjectAnimator.ofFloat(view, "rotation", 135, 270);
            closeAnim.setInterpolator(new OvershootInterpolator());
        }
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isOpen = false;
                setEnabled(true);
                for (LinearLayout group : groups) {
                    group.removeAllViews();
                    removeView(group);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                setEnabled(false);
            }
        });
        AnimatorSet.Builder builder = set.play(closeAnim);
        for (LinearLayout child : groups) {
            ObjectAnimator animator;
            if (orientation == LinearLayout.HORIZONTAL) {
                animator = ObjectAnimator.ofInt(new ViewWrapper(child),
                        "width", child.getChildCount() * switchView.getMeasuredWidth(), 0);
            } else {
                animator = ObjectAnimator.ofInt(new ViewWrapper(child),
                        "height", child.getChildCount() * switchView.getMeasuredHeight(), 0);
            }
            animator.setInterpolator(new DecelerateInterpolator());
            builder.with(animator);
        }
        set.start();
    }

    public int getState() {
        return state;
    }

    private static class ViewWrapper {
        private int width;
        private View target;
        private int height;

        ViewWrapper(View view) {
            target = view;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            if (width < 0) return;
            this.width = width;
            ViewGroup.LayoutParams params = target.getLayoutParams();
            params.width = width;
            target.setLayoutParams(params);
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
            if (height < 0) return;
            ViewGroup.LayoutParams params = target.getLayoutParams();
            params.height = height;
            target.setLayoutParams(params);
        }
    }

    public interface OnPopMenuClickListener {
        void onGoTopClick();

        void onPrePageClick();

        void onNextPageClick();

        void onPreWordClick();

        void onNextWordClick();
    }
}
