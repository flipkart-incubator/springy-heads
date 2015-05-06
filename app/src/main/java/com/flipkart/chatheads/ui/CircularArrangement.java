package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;

import java.util.List;

/**
 * Created by kiran.kumar on 07/04/15.
 */
public class CircularArrangement<T> extends ChatHeadArrangement {
    public static final String BUNDLE_KEY_X = "X";
    public static final String BUNDLE_KEY_Y = "Y";
    private final ImageView pointerViewMovable;
    private final Spring pointerXSpring;
    private final Spring pointerYSpring;
    private int CLOSE_ATTRACTION_THRESHOLD;
    private final Spring pointerScaleSpring;
    private final ImageView pointerViewStatic;
    private int RADIUS;
    private boolean isActive = false;
    private ChatHeadContainer container;
    private ChatHead<T> currentChatHead;


    public CircularArrangement(ChatHeadContainer container) {
        this.container = container;
        this.pointerViewMovable = new ImageView(container.getContext());
        this.pointerViewStatic = new ImageView(container.getContext());
        this.pointerViewMovable.setLayoutParams(new FrameLayout.LayoutParams(container.getConfig().getHeadWidth() + ChatHeadUtils.dpToPx(container.getContext(), 5), container.getConfig().getHeadHeight() + ChatHeadUtils.dpToPx(container.getContext(), 5)));
        this.pointerViewStatic.setLayoutParams(new FrameLayout.LayoutParams(container.getConfig().getHeadWidth() + ChatHeadUtils.dpToPx(container.getContext(), 5), container.getConfig().getHeadHeight() + ChatHeadUtils.dpToPx(container.getContext(), 5)));
        container.addView(pointerViewMovable);
        container.addView(pointerViewStatic);
        this.pointerXSpring = container.getSpringSystem().createSpring();
        this.pointerYSpring = container.getSpringSystem().createSpring();
        this.pointerScaleSpring = container.getSpringSystem().createSpring();
        this.pointerXSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                pointerViewMovable.setTranslationX((float) (spring.getCurrentValue() - pointerViewMovable.getMeasuredWidth() / 2));
            }
        });
        this.pointerYSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                pointerViewMovable.setTranslationY((float) (spring.getCurrentValue() - pointerViewMovable.getMeasuredHeight() / 2));
            }
        });
        this.pointerScaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                pointerViewMovable.setScaleX((float) spring.getCurrentValue());
                pointerViewMovable.setScaleY((float) spring.getCurrentValue());
            }
        });


    }

    @Override
    public void setContainer(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void onActivate(ChatHeadContainer container, Bundle extras, int maxWidth, int maxHeight) {
        List<ChatHead> chatHeads = container.getChatHeads();
        RADIUS = (int) (maxWidth / 2.5);
        CLOSE_ATTRACTION_THRESHOLD = (int) (RADIUS * 0.5);

        Point pointTo = new Point(extras.getInt(BUNDLE_KEY_X), extras.getInt(BUNDLE_KEY_Y));
        int radius = RADIUS;
        Pair<Float, Float> angles = calculateStartEndAngles(pointTo, (float) (radius), 0, 0, maxWidth, maxHeight);
        double totalSweepArea = (chatHeads.size() - 1) * Math.PI / 4;
        int i = 0;
        for (ChatHead chatHead : chatHeads) {
            /** Horizontal **/
            chatHead.getHorizontalSpring().setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            double angle = angles.first + (angles.second - angles.first) / 2 - (totalSweepArea / 2);
            if (chatHeads.size() > 1) {
                angle += (float) i / ((float) chatHeads.size() - 1) * totalSweepArea;
            }
            double xValue = pointTo.x + radius * Math.cos(angle);
            xValue -= container.getConfig().getHeadWidth() / 2;
            chatHead.getHorizontalSpring().setEndValue(xValue);

            /** Vertical **/
            chatHead.getVerticalSpring().setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            angle = angles.first + (angles.second - angles.first) / 2 - (totalSweepArea / 2);
            if (chatHeads.size() > 1) {
                angle += (float) i / ((float) chatHeads.size() - 1) * totalSweepArea;
            }
            double yValue = pointTo.y + radius * Math.sin(angle);
            yValue -= container.getConfig().getHeadHeight() / 2;
            chatHead.getVerticalSpring().setEndValue(yValue);

            i++;
        }

        isActive = true;
        this.container = container;
        container.showOverlayView();
        container.getOverlayView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivate();
            }
        });
        Drawable pointerDrawable = container.getViewAdapter().getPointerDrawable();
        pointerViewMovable.setImageDrawable(pointerDrawable);
        Drawable pointerDrawable2 = container.getViewAdapter().getPointerDrawable();
        pointerViewStatic.setImageDrawable(pointerDrawable2);
        pointerViewMovable.setVisibility(View.VISIBLE);
        pointerViewStatic.setVisibility(View.VISIBLE);

        pointerXSpring.setCurrentValue(pointTo.x);
        pointerYSpring.setCurrentValue(pointTo.y);
        pointerScaleSpring.setCurrentValue(0.5f);
        pointerViewStatic.setTranslationX(pointTo.x - pointerViewStatic.getMeasuredWidth() / 2);
        pointerViewStatic.setTranslationY(pointTo.y - pointerViewStatic.getMeasuredHeight() / 2);
        pointerViewStatic.setScaleX(0.5f);
        pointerViewStatic.setScaleY(0.5f);
        currentChatHead = null;

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean handleRawTouchEvent(MotionEvent event) {
        super.handleRawTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            boolean foundSpring = false;
            List<ChatHead> chatHeads = container.getChatHeads();
            for (ChatHead chatHead : chatHeads) {
                double distance = Math.hypot(event.getX() - pointerViewMovable.getMeasuredWidth() / 2 - chatHead.getTranslationX(), event.getY() - pointerViewMovable.getMeasuredHeight() / 2 - chatHead.getTranslationY());
                if (distance < CLOSE_ATTRACTION_THRESHOLD) {
                    foundSpring = true;
                    pointerXSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    pointerXSpring.setEndValue(chatHead.getTranslationX() + chatHead.getMeasuredWidth() / 2);
                    pointerYSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    pointerYSpring.setEndValue(chatHead.getTranslationY() + chatHead.getMeasuredHeight() / 2);
                    pointerScaleSpring.setEndValue(1f);
                    if (currentChatHead != chatHead) {
                        container.getOverlayView().drawPath(pointerViewStatic.getTranslationX() + pointerViewStatic.getMeasuredWidth() / 2, pointerViewStatic.getTranslationY() + pointerViewStatic.getMeasuredHeight() / 2, chatHead.getTranslationX() + chatHead.getMeasuredHeight() / 2, chatHead.getTranslationY() + chatHead.getMeasuredHeight() / 2);
                    }
                    currentChatHead = chatHead;
                    break;
                }
            }


            if (!foundSpring) {
                pointerXSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                pointerYSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                pointerXSpring.setEndValue(event.getX());
                pointerYSpring.setEndValue(event.getY());
                pointerScaleSpring.setEndValue(0.5f);
                container.getOverlayView().clearPath();
                currentChatHead = null;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            container.getOverlayView().clearPath();
            if (currentChatHead != null) {
                boolean handled = container.onItemSelected(currentChatHead);
                currentChatHead = null;
                if (!handled) {
                    deactivate();
                }
            } else {
                deactivate();
            }

        }
        return true;
    }

    /**
     * Brute force method to find the start angles. Not very well thought out. Working under pressure :(
     * This method will give the start and end angles where the arc cut out by the rectangle lies within the rectangle.
     *
     * @param pointTo
     * @param radius
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    private Pair<Float, Float> calculateStartEndAngles(Point pointTo, float radius, int left, int top, int right, int bottom) {
        float fullAngle = (float) (Math.PI * 2);
        double startAngle = -Math.PI;
        double endAngle = Math.PI;
        Rect rect = new Rect(left, top, right, bottom);
        int outside = 2;
        int inside = 1;
        int current = 0;
        boolean finishedFullsweep = false;
        for (double sweep = -Math.PI; sweep <= 3 * Math.PI; sweep += Math.PI / 4) {
            int x = pointTo.x + (int) (radius * Math.cos(sweep));
            int y = pointTo.y + (int) (radius * Math.sin(sweep));

            if (!rect.contains(x, y)) {

                current = outside;
            }
            if (rect.contains(x, y)) {
                if (current == outside) {
                    startAngle = sweep;
                }
                endAngle = sweep;
                current = inside;
            }
            if (sweep >= Math.PI) {
                finishedFullsweep = true;
            }
            if (finishedFullsweep && current == outside) {
                break;
            }
        }
        float finalStartAngle = (float) startAngle;

        if (endAngle < startAngle) {
            endAngle += fullAngle;
        }

        System.out.println("finalStartAngle = " + Math.toDegrees(finalStartAngle));
        System.out.println("endAngle = " + Math.toDegrees(endAngle));
        float finalEndAngle = (float) endAngle;
        return new Pair<>(finalStartAngle, finalEndAngle);
    }

    private void deactivate() {
        container.setArrangement(MinimizedArrangement.class, null);
        container.hideOverlayView();
    }


    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        isActive = false;
        pointerViewMovable.setVisibility(View.GONE);
        pointerViewStatic.setVisibility(View.GONE);
    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {

    }

    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {
        if (isActive) {
            boolean handled = container.onItemSelected(activeChatHead);
            if (!handled) {
                deactivate();
            }
            return false;

        } else {
            return true;
        }
    }

    @Override
    public void onChatHeadAdded(ChatHead chatHead) {

    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {

    }

    @Override
    public void onCapture(ChatHeadContainer container, ChatHead activeChatHead) {

    }

    @Override
    public void selectChatHead(ChatHead chatHead) {

    }

    @Override
    public void bringToFront(ChatHead chatHead) {
        //everything is in front. nothing to do here.
    }

    @Override
    public void onReloadFragment(ChatHead chatHead) {
        //nothing to do
    }

    @Override
    public boolean shouldShowCloseButton(ChatHead chatHead) {
        return false;
    }
}
