package com.flipkart.chatheads.ui.container;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadArrangement;
import com.flipkart.chatheads.ui.FrameChatHeadContainer;
import com.flipkart.chatheads.ui.MaximizedArrangement;
import com.flipkart.chatheads.ui.MinimizedArrangement;

import static android.content.Context.WINDOW_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

/**
 * Created by kiran.kumar on 08/11/16.
 */

public class WindowManagerContainer extends FrameChatHeadContainer {
    /**
     * A transparent view of the size of chat head which capture motion events and delegates them to the real view (frame layout)
     * This view is required since window managers will delegate the touch events to the window beneath it only if they are outside the bounds.
     * {@link android.view.WindowManager.LayoutParams#FLAG_NOT_TOUCH_MODAL}
     */
    private final View motionCaptureView;

    private int cachedHeight;
    private int cachedWidth;
    private WindowManager windowManager;
    private ChatHeadArrangement currentArrangement;

    public WindowManagerContainer(Context context) {
        super(context);
        motionCaptureView = new View(context);
        MotionCapturingTouchListener listener = new MotionCapturingTouchListener();
        motionCaptureView.setOnTouchListener(listener);
        motionCaptureView.setOnKeyListener(listener);
        addContainer(motionCaptureView, true);
        registerReceiver(context);
    }

    public void registerReceiver(Context context) {
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                minimize();
            }
        }, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
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

    protected void setContainerHeight(View container, int height) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.height = height;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected void setContainerWidth(View container, int width) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.width = width;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected WindowManager.LayoutParams getOrCreateLayoutParamsForContainer(View container) {
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

    protected WindowManager.LayoutParams createContainerLayoutParams(boolean focusable) {
        int focusableFlag = FLAG_NOT_TOUCH_MODAL | FLAG_ALT_FOCUSABLE_IM;
        if (!focusable) {
            focusableFlag |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE;
        }
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(MATCH_PARENT, MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                focusableFlag,
                PixelFormat.TRANSLUCENT);
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        return layoutParams;
    }

    @Override
    public void addContainer(View container, boolean focusable) {
        WindowManager.LayoutParams containerLayoutParams = createContainerLayoutParams(focusable);
        addContainer(container, containerLayoutParams);
    }

    public void addContainer(View container, WindowManager.LayoutParams containerLayoutParams) {
        container.setLayoutParams(containerLayoutParams);
        getWindowManager().addView(container, containerLayoutParams);
    }

    @Override
    public void setViewX(View view, int xPosition) {
        super.setViewX(view, xPosition);
        if (view instanceof ChatHead) {
            boolean hero = ((ChatHead) view).isHero();
            if (hero && currentArrangement instanceof MinimizedArrangement) {
                setContainerX(motionCaptureView, xPosition);
                setContainerWidth(motionCaptureView, view.getMeasuredWidth());
            }
        }
    }

    @Override
    public void setViewY(View view, int yPosition) {
        super.setViewY(view, yPosition);
        if (view instanceof ChatHead && currentArrangement instanceof MinimizedArrangement) {
            boolean hero = ((ChatHead) view).isHero();
            if (hero) {
                setContainerY(motionCaptureView, yPosition);
                setContainerHeight(motionCaptureView, view.getMeasuredHeight());
            }
        }
    }

    @Override
    public void onArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement) {
        currentArrangement = newArrangement;
        if (oldArrangement instanceof MinimizedArrangement && newArrangement instanceof MaximizedArrangement) {
            WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(motionCaptureView);
            layoutParams.flags |= FLAG_ALT_FOCUSABLE_IM;
            windowManager.updateViewLayout(motionCaptureView,layoutParams);

            setContainerX(motionCaptureView, 0);
            setContainerY(motionCaptureView, 0);
            setContainerWidth(motionCaptureView, getContainerWidth());
            setContainerHeight(motionCaptureView, getContainerHeight());
        } else {
            WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(motionCaptureView);
            layoutParams.flags |= FLAG_NOT_FOCUSABLE;
            windowManager.updateViewLayout(motionCaptureView,layoutParams);
        }
    }

    private void removeContainer(View motionCaptureView) {
        windowManager.removeView(motionCaptureView);
    }


    protected class MotionCapturingTouchListener implements View.OnTouchListener, View.OnKeyListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            event.offsetLocation(getContainerX(v), getContainerY(v));
            return getFrameLayout().dispatchTouchEvent(event);
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                minimize();
                return true;
            }
            return false;
        }
    }

    private void minimize() {
        if (!(getManager().getActiveArrangement() instanceof MinimizedArrangement)) {
            getManager().setArrangement(MinimizedArrangement.class, null, true);
        }
    }
}
