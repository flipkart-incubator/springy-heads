package com.flipkart.chatheads.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.facebook.rebound.Spring;

/**
 * Created by kiran.kumar on 02/11/16.
 */

public abstract class FrameChatHeadContainer implements ChatHeadContainer {

    private final FrameLayout frameLayout;
    private final Context context;
    DisplayMetrics displayMetrics = new DisplayMetrics();

    public FrameChatHeadContainer(Context context) {
        this.context = context;
        FrameLayout frameLayout = new FrameLayout(context);
        this.frameLayout = frameLayout;
        addContainer(frameLayout, false);
    }

    public Context getContext() {
        return context;
    }

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        frameLayout.addView(view, layoutParams);
    }

    @Override
    public void removeView(View view) {
        frameLayout.removeView(view);
    }

    @Override
    public ViewGroup.LayoutParams createLayoutParams(int height, int width, int gravity, int bottomMargin) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.gravity = gravity;
        layoutParams.bottomMargin = bottomMargin;
        return layoutParams;
    }

    @Override
    public void setViewX(View view, int xPosition, boolean isHero) {
        view.setTranslationX(xPosition);
    }

    @Override
    public void setViewY(View view, int yPosition, boolean isHero) {
        view.setTranslationY(yPosition);
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    @Override
    public int getViewX(View view) {
        return (int) view.getTranslationX();
    }

    @Override
    public int getViewY(View view) {
        return (int) view.getTranslationY();
    }

    @Override
    public void setZOrder(View view, int zIndex) {
        view.bringToFront();
    }

    public abstract void addContainer(View container, boolean focusable);

}
