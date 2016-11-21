package com.flipkart.chatheads.ui;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by kiran.kumar on 02/11/16.
 */

public abstract class FrameChatHeadContainer implements ChatHeadContainer {

    private HostFrameLayout frameLayout;
    private final Context context;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    private ChatHeadManager manager;

    public FrameChatHeadContainer(Context context) {
        this.context = context;
    }


    public ChatHeadManager getManager() {
        return manager;
    }

    @Override
    public void onInitialized(ChatHeadManager manager) {
        this.manager = manager;
        HostFrameLayout frameLayout = new HostFrameLayout(context, this, manager);
        frameLayout.setFocusable(true);
        frameLayout.setFocusableInTouchMode(true);
        this.frameLayout = frameLayout;
        addContainer(frameLayout, false);
    }

    public Context getContext() {
        return context;
    }

    public HostFrameLayout getFrameLayout() {
        return frameLayout;
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        if(frameLayout!=null) {
            frameLayout.addView(view, layoutParams);
        }
    }

    @Override
    public void requestLayout() {
        if(frameLayout!=null) {
            frameLayout.requestLayout();
        }
    }

    @Override
    public void removeView(View view) {
        if (frameLayout!=null) {
            frameLayout.removeView(view);
        }
    }

    @Override
    public ViewGroup.LayoutParams createLayoutParams(int height, int width, int gravity, int bottomMargin) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.gravity = gravity;
        layoutParams.bottomMargin = bottomMargin;
        return layoutParams;
    }

    @Override
    public void setViewX(View view, int xPosition) {
        view.setTranslationX(xPosition);
    }

    @Override
    public void setViewY(View view, int yPosition) {
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
    public void bringToFront(View view) {
        view.bringToFront();
    }

    public abstract void addContainer(View container, boolean focusable);
}
