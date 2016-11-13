package com.flipkart.chatheads.ui;

import android.content.res.Configuration;
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

    void onInitialized(ChatHeadManager manager);

    DisplayMetrics getDisplayMetrics();

    ViewGroup.LayoutParams createLayoutParams(int height, int width, int gravity, int bottomMargin);

    void setViewX(View view, int xPosition);

    void setViewY(View view, int yPosition);

    int getViewX(View view);

    int getViewY(View view);

    void bringToFront(View view);

    void addView(View view, ViewGroup.LayoutParams layoutParams);

    void removeView(View view);

    void onArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement);

    void requestLayout();
}