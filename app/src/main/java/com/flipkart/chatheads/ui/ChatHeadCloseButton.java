package com.flipkart.chatheads.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.flipkart.chatheads.R;

/**
 * Created by kirankumar on 10/02/15.
 */
public class ChatHeadCloseButton extends ImageView {


    private int mParentWidth;
    private int mParentHeight;
    private static final float PERC_PARENT_WIDTH = 0.1f; //perc of parent to be covered during drag
    private static final float PERC_PARENT_HEIGHT = 0.1f; //perc of parent to be covered during drag
    private boolean mShouldNotDismissOnRest;
    private Spring scaleSpring;

    public ChatHeadCloseButton(Context context) {
        super(context);
        init();
    }

    public ChatHeadCloseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatHeadCloseButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageResource(R.drawable.chat_head_close_ic);
        disappear(false);

        scaleSpring = SpringSystem.create().createSpring();
        scaleSpring.addListener(new SimpleSpringListener(){
            @Override
            public void onSpringUpdate(Spring spring) {
                double currentValue = spring.getCurrentValue();
                setScaleX((float) currentValue);
                setScaleY((float) currentValue);
            }
        });
        release();
    }

    public void appear(boolean animate)
    {

        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillEnabled(true);
        alphaAnimation.setFillBefore(true);
        alphaAnimation.setFillAfter(true);
        if(animate) {
            setAlpha(1f);
            startAnimation(alphaAnimation);
        }
        else
        {
           setAlpha(1f);
        }
    }

    public void capture()
    {
        scaleSpring.setEndValue(1);
    }

    public void release()
    {
        scaleSpring.setEndValue(0.5);
    }

    public void disappear(boolean animate)
    {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillEnabled(true);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setFillBefore(false);

        if(animate) {
            //setAlpha(1f);
            startAnimation(alphaAnimation);
        }
        else
        {
            setAlpha(0.1f);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mParentWidth = ((View)getParent()).getMeasuredWidth();
        mParentHeight = ((View)getParent()).getMeasuredHeight();
    }

    public void setShouldNotDismissOnRest(boolean flag)
    {
        mShouldNotDismissOnRest = flag;
    }

    public void pointTo(float x, float y)
    {
        double translationX = getTranslationFromSpring(x, PERC_PARENT_WIDTH, mParentWidth);
        double translationY = getTranslationFromSpring(y, PERC_PARENT_HEIGHT, mParentHeight);
        setTranslationX((float) translationX);
        setTranslationY((float) translationY);

    }



    private double getTranslationFromSpring(double springValue,float percent, int fullValue)
    {
        float widthToCover = percent * fullValue;
        double translation = SpringUtil.mapValueFromRangeToRange(springValue, 0, fullValue, -widthToCover / 2, widthToCover / 2);
        return translation;
    }




}
