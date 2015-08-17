package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.flipkart.chatheads.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ChatHeadCloseButton extends ImageView {

    private static final float PERC_PARENT_WIDTH = 0.1f; //perc of parent to be covered during drag
    private static final float PERC_PARENT_HEIGHT = 0.05f; //perc of parent to be covered during drag
    private int mParentWidth;
    private int mParentHeight;
    private Spring scaleSpring;
    private Spring xSpring;
    private Spring ySpring;
    private boolean disappeared;
    private CloseButtonListener listener;
    private ChatHeadContainer chatHeadContainer;

    public ChatHeadCloseButton(Context context, ChatHeadContainer container) {
        super(context);
        init(container);
    }

    public void setListener(CloseButtonListener listener) {
        this.listener = listener;
    }

    public boolean isDisappeared() {
        return disappeared;
    }

    private void init(ChatHeadContainer container) {
        this.chatHeadContainer = container;
        setImageResource(R.drawable.dismiss_big);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(chatHeadContainer.getConfig().getCloseButtonWidth(), chatHeadContainer.getConfig().getCloseButtonHeight());
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        layoutParams.bottomMargin = chatHeadContainer.getConfig().getCloseButtonBottomMargin();
        setLayoutParams(layoutParams);
        SpringSystem springSystem = SpringSystem.create();
        xSpring = springSystem.createSpring();
        xSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                setTranslationX((float) spring.getCurrentValue());
            }
        });
        ySpring = springSystem.createSpring();
        ySpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                setTranslationY((float) spring.getCurrentValue());
            }
        });
        scaleSpring = springSystem.createSpring();
        scaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                double currentValue = spring.getCurrentValue();
                setScaleX((float) currentValue);
                setScaleY((float) currentValue);
            }
        });
    }

    public void appear() {
        if(isEnabled()) {
            ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            xSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            scaleSpring.setEndValue(.8f);
            bringToFront();
            disappeared = false;

        }
    }

    public void onCapture() {
        scaleSpring.setEndValue(1);
    }

    public void onRelease() {
        scaleSpring.setEndValue(0.8);
    }

    public void disappear(boolean immediate, boolean animate) {
        ySpring.setEndValue(mParentHeight - getTop());
        ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
        xSpring.setEndValue(0);
        scaleSpring.setEndValue(0.1f);
        if (!animate) {
            ySpring.setCurrentValue(mParentHeight-getTop(), true);
            xSpring.setCurrentValue(0, true);
        }
        disappeared = true;
        if(listener!=null) listener.onCloseButtonDisappear();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onParentHeightRefreshed();
        disappear(true, false);
    }

    public void onParentHeightRefreshed() {
        mParentWidth = ((View) getParent()).getMeasuredWidth();
        mParentHeight = ((View) getParent()).getMeasuredHeight();
    }

    public void pointTo(float x, float y) {
        if(isEnabled()) {
            double translationX = getTranslationFromSpring(x, PERC_PARENT_WIDTH, mParentWidth);
            double translationY = getTranslationFromSpring(y, PERC_PARENT_HEIGHT, mParentHeight);
            if (!disappeared) {
                xSpring.setEndValue(translationX);
                ySpring.setEndValue(translationY);
                if(listener!=null) listener.onCloseButtonAppear();
            }
        }
    }

    private double getTranslationFromSpring(double springValue, float percent, int fullValue) {
        float widthToCover = percent * fullValue;
        return SpringUtil.mapValueFromRangeToRange(springValue, 0, fullValue, -widthToCover / 2, widthToCover / 2);
    }

    public boolean isAtRest() {
        return xSpring.isAtRest() && ySpring.isAtRest();
    }

    public int getEndValueX() {
        return (int) xSpring.getEndValue();
    }

    public int getEndValueY() {
        return (int) ySpring.getEndValue();
    }

    public interface CloseButtonListener {
        void onCloseButtonAppear();
        void onCloseButtonDisappear();
    }
}
