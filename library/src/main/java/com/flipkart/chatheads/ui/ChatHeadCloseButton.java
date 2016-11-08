package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
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
    private ChatHeadManager chatHeadManager;
    private int centerX;
    private int centerY;

    public ChatHeadCloseButton(Context context, ChatHeadManager manager, int maxHeight, int maxWidth) {
        super(context);
        init(manager, maxHeight, maxWidth);
    }

    public void setListener(CloseButtonListener listener) {
        this.listener = listener;
    }

    public boolean isDisappeared() {
        return disappeared;
    }

    private void init(final ChatHeadManager manager, int maxHeight, int maxWidth) {
        this.chatHeadManager = manager;

        setImageResource(R.drawable.dismiss_big);
        SpringSystem springSystem = SpringSystem.create();
        xSpring = springSystem.createSpring();
        xSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                int x = centerX + (int) spring.getCurrentValue() - getMeasuredWidth()/2;
                manager.getChatHeadContainer().setViewX(ChatHeadCloseButton.this, x);
//                System.out.println("spring x = [" + x + "] center "+centerX);

            }
        });
        ySpring = springSystem.createSpring();
        ySpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                int y = centerY + (int) spring.getCurrentValue() - getMeasuredHeight()/2;
                manager.getChatHeadContainer().setViewY(ChatHeadCloseButton.this, y);
//                System.out.println("spring y = [" + y + "] center "+centerY);
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
        if (isEnabled()) {
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
        ySpring.setEndValue(mParentHeight);
        ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
        xSpring.setEndValue(0);
        scaleSpring.setEndValue(0.1f);
        if (!animate) {
            ySpring.setCurrentValue(mParentHeight, true);
            xSpring.setCurrentValue(0, true);
        }
        disappeared = true;
        if (listener != null) listener.onCloseButtonDisappear();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onParentHeightRefreshed();
        disappear(true, false);
    }

    public void onParentHeightRefreshed() {
        mParentWidth = chatHeadManager.getMaxWidth();
        mParentHeight = chatHeadManager.getMaxHeight();
        this.centerX = (int) ((float)mParentWidth * 0.5f);
        this.centerY = (int) ((float)mParentHeight * 0.9f);
    }

    public void pointTo(float x, float y) {
        if (isEnabled()) {
            double translationX = getTranslationFromSpring(x, PERC_PARENT_WIDTH, mParentWidth);
            double translationY = getTranslationFromSpring(y, PERC_PARENT_HEIGHT, mParentHeight);
//            System.out.println("translationY = " + translationY);
            if (!disappeared) {
                xSpring.setEndValue(translationX);
                ySpring.setEndValue(translationY);
                if (listener != null) listener.onCloseButtonAppear();
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
