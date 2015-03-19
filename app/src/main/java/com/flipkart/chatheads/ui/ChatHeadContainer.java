package com.flipkart.chatheads.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.reboundextensions.ModifiedSpringChain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by kirankumar on 10/02/15.
 */
public class ChatHeadContainer<T> extends FrameLayout {

    private Spring scaleSpring;
    private int maxWidth;
    private int maxHeight;
    private ChatHeadCloseButton closeButton;
    private ChatHeadSpringsHolder springsHolder;
    //private ChatHead<T> activeChatHead;
    private Runnable closeButtonDisplayer;
    private MinimizedArrangement minimizedArrangement;
    private MaximizedArrangement maximizedArrangement;
    private ChatHeadArrangement activeArrangement;
    private Map<T,ChatHead> chatHeads = new LinkedHashMap<>();
    public ChatHeadContainer(Context context) {
        super(context);
        init();

    }
    public ChatHeadContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public ChatHeadContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public ChatHeadArrangement getActiveArrangement() {
        return activeArrangement;
    }

    public Map<T, ChatHead> getChatHeads() {
        return chatHeads;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled = super.dispatchTouchEvent(event); // only true if the child chathead has been touched

        if (handled) {

        }
        return handled;
    }

    void selectSpring(ChatHead chatHead) {
        springsHolder.selectSpring(chatHead);
        chatHead.bringToFront();
    }

    /**
     * Selects the chat head. Very similar to performing touch up on it.
     * @param chatHead
     */
    public void selectChatHead(ChatHead chatHead)
    {
        activeArrangement.selectChatHead(chatHead);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        maxWidth = getMeasuredWidth();
        maxHeight = getMeasuredHeight();

        if(activeArrangement == null)
        {
            setArrangement(minimizedArrangement);
        }

    }


    /**
     * Adds and returns the created chat head
     *
     * @return
     */
    public ChatHead<T> addChatHead(T key) {
        ChatHead<T> chatHead = new ChatHead(this, springsHolder, getContext());
        chatHead.setKey(key);
        chatHeads.put(key, chatHead);
        addView(chatHead);
        springsHolder.addChatHead(chatHead, chatHead);
        if(springsHolder.getHorizontalSpringChain().getAllSprings().size() >5) {
            ModifiedSpringChain.SpringData oldestSpring = springsHolder.getOldestSpring(springsHolder.getHorizontalSpringChain());
            ChatHead<T> chatHeadToRemove = (ChatHead) oldestSpring.getKey();
            removeChatHead(chatHeadToRemove.getKey());
        }
        if(activeArrangement!=null)
        activeArrangement.onChatHeadAdded(chatHead, springsHolder);
        springsHolder.selectSpring(chatHead);

        return chatHead;
    }


    public void removeChatHead(T key) {
        ChatHead chatHead = chatHeads.get(key);
        if(chatHead!=null && chatHead.getParent()!=null) {
            removeView(chatHead);
            chatHeads.remove(key);
            springsHolder.removeChatHead(chatHead);
            if (activeArrangement != null)
                activeArrangement.onChatHeadRemoved(chatHead, springsHolder);
        }
    }

    private void init() {

        springsHolder = new ChatHeadSpringsHolder();
        SpringSystem springSystem = SpringSystem.create();
        scaleSpring = springSystem.createSpring();
        scaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                double currentValue = spring.getCurrentValue();
                float valueFromRangeToRange = (float) currentValue;
                //activeChatHead.setScaleX(valueFromRangeToRange);
                //activeChatHead.setScaleY(valueFromRangeToRange);
            }
        });



        closeButton = new ChatHeadCloseButton(getContext());
        LayoutParams layoutParams = new LayoutParams(ChatHeadUtils.dpToPx(getContext(), 100), ChatHeadUtils.dpToPx(getContext(), 100));
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        layoutParams.bottomMargin = ChatHeadUtils.dpToPx(getContext(), 50);
        closeButton.setLayoutParams(layoutParams);
        addView(closeButton);
        closeButtonDisplayer = new Runnable() {
            @Override
            public void run() {
                closeButton.appear(true);
            }
        };
        minimizedArrangement = new MinimizedArrangement();
        maximizedArrangement = new MaximizedArrangement();

    }


    double getDistanceCloseButtonFromHead(float touchX, float touchY) {
        int left = closeButton.getLeft();
        int top = closeButton.getTop();
        double xDiff = touchX - left - closeButton.getTranslationX() - closeButton.getMeasuredWidth() / 2;
        double yDiff = touchY - top - closeButton.getTranslationY() - closeButton.getMeasuredHeight() / 2;
        double distance = Math.hypot(xDiff, yDiff);
        return distance;

    }

    private void captureChatHeads(ChatHead causingChatHead) {
        activeArrangement.onCapture(this,causingChatHead);
    }

    public void removeAllChatHeads() {
        Set<Map.Entry<T, ChatHead>> entries = chatHeads.entrySet();
        for (Map.Entry<T, ChatHead> entry : entries) {
            removeChatHead(entry.getKey());
        }

    }

    private void setArrangement(ChatHeadArrangement arrangement) {
        activeArrangement = arrangement;
        activeArrangement.onActivate(this, springsHolder, maxWidth, maxHeight);
    }

    public void toggleArrangement(ChatHead activeChatHead) {
        if (activeArrangement == maximizedArrangement) {
            setArrangement(minimizedArrangement);
        } else {
            setArrangement(maximizedArrangement);
        }
    }

    public int[] getChatHeadCoordsForCloseButton(ChatHead chatHead) {
        int[] coords = new int[2];
        int x = (int) (closeButton.getLeft() + closeButton.getTranslationX() + closeButton.getMeasuredWidth() / 2 - chatHead.getMeasuredWidth() / 2);
        int y = (int) (closeButton.getTop() + closeButton.getTranslationY() + closeButton.getMeasuredHeight() / 2 - chatHead.getMeasuredHeight() / 2);
        coords[0] = x;
        coords[1] = y;
        return coords;
    }

    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {
        minimizedArrangement.setViewAdapter(chatHeadViewAdapter);
        maximizedArrangement.setViewAdapter(chatHeadViewAdapter);
    }
}
