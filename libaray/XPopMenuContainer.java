package com.eusoft.dict.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by XJ on 2017/1/13
 */

public class XPopMenuContainer extends FrameLayout {
    public XPopMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        XFloatingPopMenu view = (XFloatingPopMenu) getChildAt(0);
        int l, t, r, b;
        int state = view.getState();
        switch (state) {
            case XFloatingPopMenu.IN_LEFT:
                r = view.getRight();
                b = view.getBottom();
                t = view.getTop();
                l = r - view.getMeasuredWidth();
                view.layout(l, t, r, b);
                break;
            case XFloatingPopMenu.IN_RIGHT:
                l = view.getLeft();
                b = view.getBottom();
                t = view.getTop();
                r = l + view.getMeasuredWidth();
                view.layout(l, t, r, b);
                break;
            case XFloatingPopMenu.IN_TOP:
                l = view.getLeft();
                b = view.getBottom();
                t = b - view.getMeasuredHeight();
                r = view.getRight();
                view.layout(l, t, r, b);
                break;
            case XFloatingPopMenu.IN_BOTTOM:
                l = view.getLeft();
                t = view.getTop();
                b = t + view.getMeasuredHeight();
                r = view.getRight();
                view.layout(l, t, r, b);
                break;
            case XFloatingPopMenu.IN_RIPPLE:
                View child_1 = view.getChildAt(1);
                if (child_1 == null) l = view.getLeft();
                else l = view.getLeft() + view.getChildAt(0).getLeft() - child_1.getMeasuredWidth();
                t = view.getTop();
                b = view.getBottom();
                View child_2 = view.getChildAt(2);
                if (child_2 == null) r = view.getRight();
                else r = l + view.getMeasuredWidth();
                view.layout(l, t, r, b);
                break;
            default:
                if (view.needLayout) {
                    super.onLayout(changed, left, top, right, bottom);
                }
                break;
        }
    }
}
