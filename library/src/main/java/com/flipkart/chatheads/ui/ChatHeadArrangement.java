package com.flipkart.chatheads.ui;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MotionEvent;

import com.facebook.rebound.Spring;

/**
 * Created by kirankumar on 13/02/15.
 */
public abstract class ChatHeadArrangement {
    public abstract void setContainer(ChatHeadContainer container);

    public abstract void onActivate(ChatHeadContainer container, Bundle extras, int maxWidth, int maxHeight, boolean animated);

    public abstract void onDeactivate(int maxWidth, int maxHeight);

    public abstract void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity);

    public boolean handleRawTouchEvent(MotionEvent event) {
        return false;
    }

    public void onDraw(Canvas canvas) {};

    public abstract boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging);

    public abstract void onChatHeadAdded(ChatHead chatHead, boolean animated);

    public abstract void onChatHeadRemoved(ChatHead removed);

    public abstract void onCapture(ChatHeadContainer container, ChatHead activeChatHead);

    public abstract void selectChatHead(ChatHead chatHead);

    public abstract void bringToFront(ChatHead chatHead);

    public abstract void onReloadFragment(ChatHead chatHead);

    public abstract boolean shouldShowCloseButton(ChatHead chatHead);

    public abstract Integer getHeroIndex();

    public abstract void onConfigChanged(ChatHeadConfig newConfig);

    public abstract Bundle getRetainBundle();

    public abstract boolean canDrag(ChatHead chatHead);

    public abstract void removeOldestChatHead();
}
