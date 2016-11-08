package com.flipkart.chatheads.ui.container;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.flipkart.chatheads.ui.FrameChatHeadContainer;

import static android.content.Context.WINDOW_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

/**
 * Created by kiran.kumar on 08/11/16.
 */

public class WindowManagerContainer extends FrameChatHeadContainer {
    private final View motionCaptureView;
    private int cachedHeight;
    private int cachedWidth;
    private WindowManager windowManager;

    public WindowManagerContainer(Context context) {
        super(context);
        motionCaptureView = new View(context);
        motionCaptureView.setOnTouchListener(new MotionCapturingTouchListener());
        addContainer(motionCaptureView, true);
    }

    public WindowManager getWindowManager() {
        if (windowManager == null) {
            windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        }
        return windowManager;
    }



    @Override
    public int getContainerHeight() {
        if (cachedHeight <= 0) {
            cachedHeight = windowManager.getDefaultDisplay().getHeight();
        }
        return cachedHeight;
    }

    @Override
    public int getContainerWidth() {
        if (cachedWidth <= 0) {
            cachedWidth = windowManager.getDefaultDisplay().getWidth();
        }
        return cachedWidth;
    }

    private void setContainerHeight(View container, int height) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.height = height;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    private void setContainerWidth(View container, int width) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.width = width;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    private WindowManager.LayoutParams getOrCreateLayoutParamsForContainer(View container) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) container.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = createContainerLayoutParams(false);
            container.setLayoutParams(layoutParams);
        }
        return layoutParams;
    }

    protected void setContainerX(View container, int xPosition) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.x = xPosition;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected int getContainerX(View container) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        return layoutParams.x;
    }


    protected void setContainerY(View container, int yPosition) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.y = yPosition;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected int getContainerY(View container) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        return layoutParams.y;
    }

    private WindowManager.LayoutParams createContainerLayoutParams(boolean focusable) {
        int focusableFlag = FLAG_NOT_TOUCH_MODAL;
        if (!focusable) {
            focusableFlag |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(MATCH_PARENT, MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                focusableFlag | FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        return layoutParams;
    }

    @Override
    public void addContainer(View container, boolean focusable) {
        WindowManager.LayoutParams containerLayoutParams = createContainerLayoutParams(focusable);
        container.setLayoutParams(containerLayoutParams);
        getWindowManager().addView(container, containerLayoutParams);
    }

    @Override
    public void setViewX(View view, int xPosition, boolean isHero) {
        super.setViewX(view, xPosition, isHero);
        if (isHero) {
            setContainerX(motionCaptureView, xPosition);
            setContainerWidth(motionCaptureView, view.getMeasuredWidth());
        }
    }

    @Override
    public void setViewY(View view, int yPosition, boolean isHero) {
        super.setViewY(view, yPosition, isHero);
        if (isHero) {
            setContainerY(motionCaptureView, yPosition);
            setContainerHeight(motionCaptureView, view.getMeasuredHeight());
        }
    }

    private class MotionCapturingTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            event.offsetLocation(getContainerX(v), getContainerY(v));
            getFrameLayout().dispatchTouchEvent(event);
            return false;
        }
    }
}
