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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiran.kumar on 07/04/15.
 */
public class CircularArrangement<T extends Serializable> extends ChatHeadArrangement {
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
    private ChatHeadContainer<T> container;
    private ChatHead<T> currentChatHead;
    private int maxWidth;
    private int maxHeight;
    private RollState rollOverState; //whether we are over or out of a chat head
    private ChatHead rollOverChatHead; //the chat head where we rolled over //will be non null if we are in roll over state. null if roll out state
    private Bundle retainBundle;


    public CircularArrangement(ChatHeadContainer container) {
        this.container = container;
        this.pointerViewMovable = new ImageView(container.getContext());
        this.pointerViewStatic = new ImageView(container.getContext());

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

        onConfigChanged(container.getConfig());

    }

    @Override
    public void onConfigChanged(ChatHeadConfig newConfig) {
        this.pointerViewMovable.setLayoutParams(new FrameLayout.LayoutParams(newConfig.getCircularRingWidth(), newConfig.getCircularRingHeight()));
        this.pointerViewStatic.setLayoutParams(new FrameLayout.LayoutParams(newConfig.getCircularRingWidth(), newConfig.getCircularRingHeight()));

    }

    @Override
    public Bundle getRetainBundle() {
        return retainBundle;
    }

    @Override
    public boolean canDrag(ChatHead chatHead) {
        return true;
    }

    @Override
    public void removeOldestChatHead() {
        for (ChatHead<T> chatHead : container.getChatHeads()) {
            if (!chatHead.isSticky()) {
                container.removeChatHead(chatHead.getKey(), false);
                break;
            }
        }
    }

    @Override
    public void setContainer(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void onActivate(ChatHeadContainer container, Bundle extras, int maxWidth, int maxHeight, boolean animated) {
        List<ChatHead> chatHeads = container.getChatHeads();
        RADIUS = container.getConfig().getCircularFanOutRadius(maxWidth, maxHeight);
        int headHeight = container.getConfig().getHeadHeight();
        int headWidth = container.getConfig().getHeadWidth();
        CLOSE_ATTRACTION_THRESHOLD = (int) (RADIUS * 0.5);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        if(container.getChatHeads().size()>0) {

            Point pointTo = new Point(extras.getInt(BUNDLE_KEY_X), extras.getInt(BUNDLE_KEY_Y));
            int radius = RADIUS;
            Pair<Float, Float> angles = calculateStartEndAngles(pointTo, (float) (radius), 0, 0, maxWidth, maxHeight);
            double totalSweepArea = (angles.second - angles.first);
            if (chatHeads.size() > 0) {
                double perHeadSweep = totalSweepArea / chatHeads.size();
                if (perHeadSweep > Math.PI / 5) {
                    perHeadSweep = Math.PI / 5;
                    totalSweepArea = perHeadSweep * chatHeads.size();
                }
            }
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
            container.showOverlayView(true);
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
        retainBundle = extras;


    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean handleRawTouchEvent(MotionEvent event) {
        super.handleRawTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            boolean foundSpring = false;
            List<ChatHead<T>> chatHeads = container.getChatHeads();
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
                    if (rollOverChatHead != chatHead && isActive) {
                        container.onItemRollOver(chatHead);
                        rollOverChatHead = chatHead;
                    }
                    rollOverState = RollState.OVER;
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
                onRollOut();
                currentChatHead = null;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            container.getOverlayView().clearPath();
            onRollOut();
            if (currentChatHead != null) {
                boolean handled = container.onItemSelected(currentChatHead);
                if (!handled) {
                    deactivate();
                }
            } else {
                deactivate();
            }

        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            onRollOut();
        }
        return true;
    }

    private void onRollOut() {
        if (rollOverState != RollState.OUT && rollOverChatHead != null) {
            container.onItemRollOut(rollOverChatHead);
            rollOverChatHead = null;
        }
        rollOverState = RollState.OUT;
    }

    /**
     * Brute force method to find the start angles. Not very well thought out.
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
        Rect circleRect = new Rect((pointTo.x - (int) radius), (pointTo.y - (int) radius), (pointTo.x + (int) radius), (pointTo.y + (int) radius));
        Rect screenRect = new Rect(left, top, right, bottom);
        List<Quadrant> quadrants = findContainingQuadrants(circleRect, screenRect);
        float minAngle = Float.MAX_VALUE, maxAngle = Float.MIN_VALUE;
        if (quadrants.contains(Quadrant.IV) && quadrants.contains(Quadrant.I) && quadrants.size() == 2) {
            //TODO this is a hack until a better way if figured out
            minAngle = (float) (-Math.PI / 2);
            maxAngle = (float) (Math.PI / 2);
        } else {
            if (quadrants.size() < 4) {
                for (Quadrant quadrant : quadrants) {
                    double angle2 = (quadrant.getStartAngle());
                    minAngle = (float) Math.min(minAngle, angle2);
                    double angle4 = (quadrant.getEndAngle());
                    maxAngle = (float) Math.max(maxAngle, angle4);
                }
            } else {
                minAngle = (float) Math.PI;
                maxAngle = (float) (Math.PI * 2);
            }
        }
        return new Pair<>(minAngle, maxAngle);
    }

    private double convert(double angle) {
        return ((angle + Math.PI) % (Math.PI * 2f)) - Math.PI;
    }

    /**
     * Returns the quadrants which the second rect contains in itself fully.
     */
    private List<Quadrant> findContainingQuadrants(Rect firstRect, Rect secondRect) {
        List<Quadrant> quadrants = new ArrayList<>();
        int radiusX = (firstRect.right - firstRect.left) / 2;
        int radiusY = (firstRect.bottom - firstRect.top) / 2;
        for (Quadrant quadrant : Quadrant.values()) {
            int x = firstRect.centerX() + quadrant.getxSign() * radiusX;
            int y = firstRect.centerY() + quadrant.getySign() * radiusY;
            Rect quadrantRect = new Rect(Math.min(x, firstRect.centerX()), Math.min(y, firstRect.centerY()), Math.max(firstRect.centerX(), x), Math.max(firstRect.centerY(), y));
            if (secondRect.contains(quadrantRect)) {
                quadrants.add(quadrant);
            }
        }
        return quadrants;
    }

    private void deactivate() {
        container.setArrangement(MinimizedArrangement.class, null);
    }

    @Override
    public Integer getHeroIndex() {
        int heroIndex = 0;
        List<ChatHead<T>> chatHeads = container.getChatHeads();
        int i = 0;
        for (ChatHead chatHead : chatHeads) {
            if (currentChatHead == chatHead) {
                heroIndex = i;
            }
            i++;
        }
        return heroIndex;
    }


    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        onRollOut();
        isActive = false;
        pointerViewMovable.setVisibility(View.GONE);
        pointerViewStatic.setVisibility(View.GONE);
        this.container.hideOverlayView(true);
        currentChatHead = null;
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
    public void onChatHeadAdded(ChatHead chatHead, boolean animated) {
        onActivate(container, retainBundle, maxWidth, maxHeight, true);
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        onActivate(container, retainBundle, maxWidth, maxHeight, true);
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

    private enum RollState {
        OVER, // over means that the finger is over the chat head
        OUT; // out means that finger is outside the touch tolerance of chat head
    }


//    II  |   I
//        |
// <------+------>x
//        |
//   III  |  IV

    private enum Quadrant {
        IV(+1, +1, 0, Math.PI / 2), III(-1, +1, Math.PI / 2, Math.PI), II(-1, -1, Math.PI, 1.5 * Math.PI), I(+1, -1, 1.5 * Math.PI, 2 * Math.PI);

        public double getStartAngle() {
            return startAngle;
        }

        public double getEndAngle() {
            return endAngle;
        }

        public int getySign() {
            return ySign;
        }

        public int getxSign() {
            return xSign;
        }

        private final double startAngle;
        private final double endAngle;
        private final int ySign;
        private final int xSign;

        Quadrant(int xSign, int ySign, double startAngle, double endAngle) {
            this.startAngle = startAngle;
            this.endAngle = endAngle;
            this.xSign = xSign;
            this.ySign = ySign;
        }
    }
}
