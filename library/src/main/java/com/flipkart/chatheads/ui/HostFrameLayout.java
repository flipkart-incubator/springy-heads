package com.flipkart.chatheads.ui;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.FrameLayout;

/**
 * Created by kiran.kumar on 11/11/16.
 */
public class HostFrameLayout extends FrameLayout {
    private final ChatHeadManager manager;
    private final ChatHeadContainer container;

    public HostFrameLayout(Context context, ChatHeadContainer chatHeadContainer, ChatHeadManager manager) {
        super(context);
        this.manager = manager;
        this.container = chatHeadContainer;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        manager.onMeasure(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        manager.onSizeChanged(w,h,oldw,oldh);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = super.dispatchKeyEvent(event);
        if(!handled) {
            if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                minimize();
                return true;
            }
        }
        return handled;
    }

    public void minimize() {
        if (!(manager.getActiveArrangement() instanceof MinimizedArrangement)) {
            manager.setArrangement(MinimizedArrangement.class, null, true);
        }
    }
}
