package com.flipkart.chatheads.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.ChatHeadManager;
import com.flipkart.chatheads.utils.SpringConfigsHolder;

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
                int x = getXFromSpring(spring);
                manager.getChatHeadContainer().setViewX(ChatHeadCloseButton.this, x);
            }
        });
        ySpring = springSystem.createSpring();
        ySpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                int y = getYFromSpring(spring);
                manager.getChatHeadContainer().setViewY(ChatHeadCloseButton.this, y);
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

    private int getYFromSpring(Spring spring) {
        return centerY + (int) spring.getCurrentValue() - getMeasuredHeight() / 2;
    }

    private int getXFromSpring(Spring spring) {
        return centerX + (int) spring.getCurrentValue() - getMeasuredWidth() / 2;
    }

    public void appear() {
        if (isEnabled()) {
            ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            xSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            scaleSpring.setEndValue(.8f);
            ViewParent parent = getParent();
            if (parent instanceof ViewGroup) {
                int i = ((ViewGroup) parent).indexOfChild(this);
                if (i != ((ViewGroup) parent).getChildCount() - 1) {
                    bringToFront();
                }
            }
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
        ySpring.setEndValue(mParentHeight - centerY + chatHeadManager.getConfig().getCloseButtonHeight());
        ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
        xSpring.setEndValue(0);
        ySpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
                ySpring.removeListener(this);
            }
        });
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
        disappear(true, false);
    }

    public void onParentHeightRefreshed() {
        mParentWidth = chatHeadManager.getMaxWidth();
        mParentHeight = chatHeadManager.getMaxHeight();
    }

    public void setCenter(int x, int y) {
        boolean changed = false;
        if (x != centerX || y != centerY) {
            changed = true;
        }
        if(changed) {
            this.centerX = x;
            this.centerY = y;
            xSpring.setCurrentValue(0,false);
            ySpring.setCurrentValue(0,false);
        }
    }

    public void pointTo(float x, float y) {
        if (isEnabled()) {
            double translationX = getTranslationFromSpring(x, PERC_PARENT_WIDTH, mParentWidth);
            double translationY = getTranslationFromSpring(y, PERC_PARENT_HEIGHT, mParentHeight);
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
        return getXFromSpring(xSpring);
    }

    public int getEndValueY() {
        return getYFromSpring(ySpring);
    }

    public interface CloseButtonListener {
        void onCloseButtonAppear();

        void onCloseButtonDisappear();
    }
}
