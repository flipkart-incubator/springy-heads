package com.flipkart.chatheads.ui;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.WindowManager;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;

import java.io.Serializable;

/**
 * Created by kiran.kumar on 27/10/16.
 */
public interface ChatHeadContainer {
    DisplayMetrics getDisplayMetrics();

    ViewGroup.LayoutParams createLayoutParams(int height, int width, int gravity, int bottomMargin);

    int getContainerHeight();

    int getContainerWidth();

    void setViewX(View view, int xPosition, boolean isHero);

    void setViewY(View view, int yPosition, boolean isHero);

    int getViewX(View view);

    int getViewY(View view);

    void setZOrder(View view, int zIndex);

    void addView(View view, ViewGroup.LayoutParams layoutParams);

    void removeView(View view);
}