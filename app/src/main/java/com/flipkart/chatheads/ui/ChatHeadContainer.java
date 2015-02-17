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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by kirankumar on 10/02/15.
 */
public class ChatHeadContainer<T> extends FrameLayout implements SpringListener {

    private Spring scaleSpring;
    private float downX = -1;
    private float downY = -1;
    private VelocityTracker velocityTracker;
    private int maxWidth;
    private int maxHeight;

    private boolean isDragging;
    private float downTranslationX;
    private float downTranslationY;
    private ChatHeadCloseButton closeButton;
    private final int CLOSE_ATTRACTION_THRESHOLD = ChatHeadUtils.dpToPx(getContext(), 110);
    private ChatHeadSpringsHolder springsHolder;
    private ChatHead<T> activeChatHead;
    private float DELTA = ChatHeadUtils.dpToPx(getContext(), 10);
    private Runnable closeButtonDisplayer;
    private MinimizedArrangement minimizedArrangement;
    private MaximizedArrangement maximizedArrangement;
    private ChatHeadArrangement activeArrangement;
    private int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    public Map<T, ChatHead> getChatHeads() {
        return chatHeads;
    }

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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled = super.dispatchTouchEvent(event); // only true if the child chathead has been touched

        if (handled) {
            //Chathead view will set the correct active springs on touch
            Spring activeHorizontalSpring = springsHolder.getActiveHorizontalSpring();
            Spring activeVerticalSpring = springsHolder.getActiveVerticalSpring();

            int action = event.getAction();
            float rawX = event.getX();
            float rawY = event.getY();
            float offsetX = rawX - downX;
            float offsetY = rawY - downY;


            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            }

            if (action == MotionEvent.ACTION_DOWN) {
                activeChatHead.setState(ChatHead.State.FREE);
                downX = rawX;
                downY = rawY;
                downTranslationX = (float) activeHorizontalSpring.getCurrentValue();
                downTranslationY = (float) activeVerticalSpring.getCurrentValue();
                scaleSpring.setEndValue(1.2f);
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                velocityTracker.addMovement(event);
                closeButton.setShouldNotDismissOnRest(true);
                postDelayed(closeButtonDisplayer, 500);

            } else if (action == MotionEvent.ACTION_MOVE) {
                if(Math.hypot(offsetX,offsetY)>touchSlop)
                {
                    isDragging = true;
                }
                if (isDragging) {
                    closeButton.pointTo(rawX, rawY);
                    double distanceCloseButtonFromHead = getDistanceCloseButtonFromHead(rawX, rawY);
                    if (distanceCloseButtonFromHead < CLOSE_ATTRACTION_THRESHOLD) {
                        activeChatHead.setState(ChatHead.State.CAPTURED);
                        int[] coords = getChatHeadCoordsForCloseButton();
                        activeHorizontalSpring.setEndValue(coords[0]);
                        activeVerticalSpring.setEndValue(coords[1]);
                        closeButton.capture();

                    } else {
                        activeChatHead.setState(ChatHead.State.FREE);
                        activeHorizontalSpring.setEndValue(downTranslationX + offsetX);
                        activeVerticalSpring.setEndValue(downTranslationY + offsetY);
                        velocityTracker.addMovement(event);
                        closeButton.release();
                    }
                }

            } else {
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    boolean wasDragging = isDragging;
                    isDragging = false;

                    removeCallbacks(closeButtonDisplayer);
                    closeButton.disappear(true);
                    scaleSpring.setEndValue(1);
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1000);
                    int xVelocity = (int) velocityTracker.getXVelocity();
                    int yVelocity = (int) velocityTracker.getYVelocity();
                    boolean touchUpHandled = activeArrangement.handleTouchUp(activeChatHead, xVelocity, yVelocity, activeHorizontalSpring, activeVerticalSpring, wasDragging);
                    if(!touchUpHandled)
                    {
                        activeArrangement.onDeactivate(activeChatHead, maxWidth, maxHeight, activeHorizontalSpring, activeVerticalSpring);
                        toggleArrangement(activeChatHead);
                    }


                }
            }
        }
        return handled;
    }

    public void selectChatHead(ChatHead chatHead) {
        activeChatHead = chatHead;
        if (activeArrangement instanceof MaximizedArrangement) {
            springsHolder.selectSpring(chatHead);
            activeChatHead.bringToFront();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        maxWidth = getMeasuredWidth();
        maxHeight = getMeasuredHeight();

        if(activeArrangement == null)
        {
            setArrangement(minimizedArrangement, activeChatHead);
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
        springsHolder.addChatHead(chatHead, this);
        activeChatHead = chatHead;
        if(activeArrangement!=null)
        activeArrangement.onChatHeadAdded(chatHead, springsHolder);
        springsHolder.selectSpring(activeChatHead);
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
                activeChatHead.setScaleX(valueFromRangeToRange);
                activeChatHead.setScaleY(valueFromRangeToRange);
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


    private double getDistanceCloseButtonFromHead(float touchX, float touchY) {
        int left = closeButton.getLeft();
        int top = closeButton.getTop();
        double xDiff = touchX - left - closeButton.getTranslationX() - closeButton.getMeasuredWidth() / 2;
        double yDiff = touchY - top - closeButton.getTranslationY() - closeButton.getMeasuredHeight() / 2;
        double distance = Math.hypot(xDiff, yDiff);
        return distance;

    }

    @Override
    public void onSpringUpdate(Spring spring) {
        Spring activeHorizontalSpring = springsHolder.getActiveHorizontalSpring();
        Spring activeVerticalSpring = springsHolder.getActiveVerticalSpring();
        float deltaX = (float) (DELTA * ((float) maxWidth / 2f - (activeHorizontalSpring.getCurrentValue() + activeChatHead.getMeasuredWidth() / 2)) / ((float) maxWidth / 2f));
        springsHolder.setChainDelta(deltaX, 0);
        double distanceCloseButtonFromHead = getDistanceCloseButtonFromHead((float) activeHorizontalSpring.getCurrentValue() + activeChatHead.getMeasuredWidth() / 2, (float) activeVerticalSpring.getCurrentValue() + activeChatHead.getMeasuredHeight() / 2);
        int totalVelocity = (int) Math.hypot(activeHorizontalSpring.getVelocity(), activeVerticalSpring.getVelocity());
        activeArrangement.onSpringUpdate(activeChatHead, isDragging, maxWidth, maxHeight, spring, activeHorizontalSpring, activeVerticalSpring, totalVelocity);
        if (!isDragging) {


            /** Capturing check **/

            if (distanceCloseButtonFromHead < CLOSE_ATTRACTION_THRESHOLD && totalVelocity < 1000) {

                int[] coords = getChatHeadCoordsForCloseButton();
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeHorizontalSpring.setEndValue(coords[0]);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeVerticalSpring.setEndValue(coords[1]);
                closeButton.appear(false);
                if (activeHorizontalSpring.currentValueIsApproximately(coords[0]) && activeVerticalSpring.currentValueIsApproximately(coords[0])) {
                    activeHorizontalSpring.setAtRest();
                    activeVerticalSpring.setAtRest();
                    activeChatHead.setState(ChatHead.State.CAPTURED);
                }

            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED) {
                scaleSpring.setEndValue(0);
                closeButton.disappear(true);
                captureChatHeads();
            }
        }
    }

    private void captureChatHeads() {
        activeArrangement.onCapture(this,activeChatHead);
    }

    public void removeAllChatHeads() {
        Set<Map.Entry<T, ChatHead>> entries = chatHeads.entrySet();
        for (Map.Entry<T, ChatHead> entry : entries) {
            removeChatHead(entry.getKey());
        }

    }

    private void setArrangement(ChatHeadArrangement arrangement, ChatHead activeChatHead) {
        activeArrangement = arrangement;
        activeArrangement.onActivate(this, activeChatHead, springsHolder, maxWidth, maxHeight);
    }

    public void toggleArrangement(ChatHead activeChatHead) {
        if (activeArrangement == maximizedArrangement) {
            setArrangement(minimizedArrangement,activeChatHead);
        } else {
            setArrangement(maximizedArrangement,activeChatHead);
        }
    }

    private int[] getChatHeadCoordsForCloseButton() {
        int[] coords = new int[2];
        int x = (int) (closeButton.getLeft() + closeButton.getTranslationX() + closeButton.getMeasuredWidth() / 2 - activeChatHead.getMeasuredWidth() / 2);
        int y = (int) (closeButton.getTop() + closeButton.getTranslationY() + closeButton.getMeasuredHeight() / 2 - activeChatHead.getMeasuredHeight() / 2);
        coords[0] = x;
        coords[1] = y;
        return coords;
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }

    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {
        minimizedArrangement.setViewAdapter(chatHeadViewAdapter);
        maximizedArrangement.setViewAdapter(chatHeadViewAdapter);
    }
}
