package com.flipkart.chatheads.ui.arrangements;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;

import com.facebook.rebound.Spring;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.reboundextensions.ModifiedSpringChain;
import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadArrangement;
import com.flipkart.chatheads.ui.ChatHeadCloseButton;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.SpringConfigsHolder;

import java.util.List;

/**
 * Created by kiran.kumar on 07/04/15.
 */
public class CircularArrangement extends ChatHeadArrangement {
    public static String BUNDLE_KEY_X = "X";
    public static String BUNDLE_KEY_Y = "Y";
    private Point pointTo;
    private boolean isActive = false;
    private ChatHeadContainer container;

    public CircularArrangement(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void setContainer(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void onActivate(ChatHeadContainer container, Bundle extras, ChatHeadSpringsHolder springsHolder, int maxWidth, int maxHeight) {
        springsHolder.setChaining(false);
        List<ModifiedSpringChain.SpringData> horizontalSprings = springsHolder.getHorizontalSpringChain().getAllSprings();
        List<ModifiedSpringChain.SpringData> verticalSprings = springsHolder.getVerticalSpringChain().getAllSprings();
        pointTo = new Point(extras.getInt(BUNDLE_KEY_X), extras.getInt(BUNDLE_KEY_Y));
        float radius = ChatHeadUtils.dpToPx(container.getContext(), 100);
        int chatHeadDiameter = ChatHeadUtils.dpToPx(container.getContext(), ChatHead.DIAMETER);
        Pair<Float, Float> angles = calculateStartEndAngles(pointTo, (float) (radius*1.2), 0, 0, maxWidth, maxHeight);
        for (int i = 0; i < horizontalSprings.size(); i++) {
            ModifiedSpringChain.SpringData horizontalSpring = horizontalSprings.get(i);
            horizontalSpring.getSpring().setSpringConfig(SpringConfigsHolder.CONVERGING);
            double xValue = pointTo.x + radius * Math.cos(angles.first + ((float) Math.abs(angles.second - angles.first) * (float) i / (float) horizontalSprings.size()));
            xValue -= chatHeadDiameter / 2;
            horizontalSpring.getSpring().setEndValue(xValue);
        }
        for (int i = 0; i < verticalSprings.size(); i++) {
            ModifiedSpringChain.SpringData verticalSpring = verticalSprings.get(i);
            verticalSpring.getSpring().setSpringConfig(SpringConfigsHolder.CONVERGING);
            double yValue = pointTo.y + radius * Math.sin(angles.first + ((float) (angles.second - angles.first) * (float) i / (float) verticalSprings.size()));
            yValue -= chatHeadDiameter / 2;
            verticalSpring.getSpring().setEndValue(yValue);
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
    }

    /**
     * Brute force method to find the start angles. Not very well thought out. Working under pressure :(
     * This method will give the start and end angles where the arc cut out by the rectangle lies within the rectangle.
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
        double startAngle = 0;
        double endAngle = fullAngle;
        Rect rect = new Rect(left, top, right, bottom);
        int outside = 2;
        int inside = 1;
        int current = 0;
        for(double sweep = Math.PI/4; sweep <= fullAngle+Math.PI/4; sweep += Math.PI / 4) {
            int x = pointTo.x + (int) (radius * Math.cos(sweep));
            int y = pointTo.y + (int) (radius * Math.sin(sweep));

            if (!rect.contains(x, y)) {
                if (current == inside) {
                    endAngle = sweep;
                }
                current = outside;
            }
            if (rect.contains(x, y)) {
                if (current == outside) {
                    startAngle = sweep;
                }
                current = inside;
            }
        }
        float finalStartAngle = (float) startAngle;

        if (endAngle < startAngle) {
            endAngle += fullAngle;
        }
        float finalEndAngle = (float) endAngle;
        return new Pair<>(finalStartAngle, finalEndAngle);
    }

    private void deactivate() {
        container.setArrangement(MinimizedArrangement.class, null);
        container.hideOverlayView();
    }


    @Override
    public void onDeactivate(int maxWidth, int maxHeight, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        isActive = false;
    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {

    }
    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {
        if (isActive) {
            boolean handled = container.onItemSelected(activeChatHead);
            if(!handled) {
                deactivate();
            }
            return false;

        } else {
            return true;
        }
    }

    @Override
    public void onChatHeadAdded(ChatHead chatHead, ChatHeadSpringsHolder springsHolder) {

    }

    @Override
    public void onChatHeadRemoved(ChatHead removed, ChatHeadSpringsHolder springsHolder) {

    }

    @Override
    public void onCapture(ChatHeadContainer container, ChatHead activeChatHead) {

    }

    @Override
    public void selectChatHead(ChatHead chatHead) {

    }
}
