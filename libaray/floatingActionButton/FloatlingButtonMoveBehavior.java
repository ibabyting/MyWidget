package com.example.eusoft_nas.myapplication;

import android.animation.ValueAnimator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by xj on 2017/3/8.
 * floating action button show or hide in vertical bottom orientation
 */

public class FloatlingButtonMoveBehavior extends FloatingActionButton.Behavior {

    private ValueAnimator transAnim;
    private View dependView;
    private boolean isHide = false;

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        boolean b = super.layoutDependsOn(parent, child, dependency);
        if (b) dependView = dependency;
        return b;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        return params.getAnchorId() == View.NO_ID;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, final FloatingActionButton child, View target, int dx, int dy, int[] consumed) {
        if (transAnim == null) {
            transAnim = new ValueAnimator();
            transAnim.setInterpolator(new LinearInterpolator());
            transAnim.setDuration(300);
            transAnim.setFloatValues(0, 1f);
            final int distance = coordinatorLayout.getHeight() - child.getBottom() + child.getHeight();
            transAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    child.setTranslationY(distance * (Float) animation.getAnimatedValue());
                }
            });
        }

        if (transAnim.isRunning()) return;

        if (dependView != null && dependView instanceof Snackbar.SnackbarLayout) {
            Snackbar.SnackbarLayout snackBarLayout = (Snackbar.SnackbarLayout) dependView;
            if (snackBarLayout.isShown()) return;
        }

        if (isHide && dy < -30) {
            isHide = false;
            transAnim.reverse();
        } else if (!isHide && dy > 30) {
            isHide = true;
            transAnim.start();
        }
    }
}
