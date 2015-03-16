package com.flipkart.chatheads.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.R;

/**
 * Created by kirankumar on 10/02/15.
 */
public class ChatHead<T> extends ImageView {

    private static final int radius = 75;
    private ChatHeadContainer container;
    private ChatHeadSpringsHolder springsHolder;
    private State state;
    private T key;

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    private int unreadCount = 0;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public enum State {
        FREE, CAPTURED;
    }

    public ChatHead(Context context) {
        super(context);
        init();
    }

    public ChatHead(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatHead(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private SpringListener xPositionListener;
    private SpringListener yPositionListener;

    public ChatHead(ChatHeadContainer container, ChatHeadSpringsHolder springsHolder, Context context) {
        super(context);
        init();
        this.container = container;
        this.springsHolder = springsHolder;
    }

    private void init() {
        int radiusInDp = ChatHeadUtils.dpToPx(getContext(), radius);
        setLayoutParams(new ViewGroup.LayoutParams(radiusInDp, radiusInDp));
        setImageResource(R.drawable.chathead);
        xPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                setTranslationX((float) spring.getCurrentValue());
            }
        };
        yPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                setTranslationY((float) spring.getCurrentValue());
            }
        };
    }

    public SpringListener getHorizontalPositionListener() {
        return xPositionListener;
    }

    public SpringListener getVerticalPositionListener() {
        return yPositionListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            container.selectChatHead(this);
        }
        return true;
    }



}

