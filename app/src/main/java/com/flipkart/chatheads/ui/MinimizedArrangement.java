package com.flipkart.chatheads.ui;

import com.facebook.rebound.Spring;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;

/**
 * Created by kirankumar on 13/02/15.
 */
public class MinimizedArrangement extends ChatHeadArrangement {

    private int currentX = 0;
    private int currentY = 200;
    private int maxWidth;
    private int maxHeight;
    private ChatHeadContainer container;

    @Override
    public void onActivate(ChatHeadContainer container, ChatHead activeChatHead, ChatHeadSpringsHolder springsHolder, int maxWidth, int maxHeight) {
        this.container = container;
        if (springsHolder.getActiveHorizontalSpring() != null)
            springsHolder.getActiveHorizontalSpring().setEndValue(currentX);
        if (springsHolder.getActiveVerticalSpring() != null)
            springsHolder.getActiveVerticalSpring().setEndValue(currentY);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        springsHolder.setChaining(true);


    }

    @Override
    public void onChatHeadAdded(ChatHead chatHead, ChatHeadSpringsHolder springsHolder) {
        onActivate(container, chatHead, springsHolder, maxWidth, maxHeight);
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed, ChatHeadSpringsHolder springsHolder) {
        onActivate(container, null, springsHolder, maxWidth, maxHeight);
    }

    @Override
    public void onCapture(ChatHeadContainer container, ChatHead activeChatHead) {
        // we dont care about the active ones
        container.removeAllChatHeads();
    }

    @Override
    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {

    }

    @Override
    public void onDeactivate(ChatHead activeChatHead, int maxWidth, int maxHeight, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        currentX = (int) activeHorizontalSpring.getCurrentValue();
        currentY = (int) activeVerticalSpring.getCurrentValue();
    }

    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {
        if (xVelocity > 6000 || xVelocity < -6000) {
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.COASTING);
            activeVerticalSpring.setSpringConfig(SpringConfigsHolder.COASTING);
            activeHorizontalSpring.setVelocity(xVelocity);
            activeVerticalSpring.setVelocity(yVelocity);
        } else {
            double currentValue = activeHorizontalSpring.getCurrentValue();
            if (xVelocity < -1000) {
                activeHorizontalSpring.setEndValue(0);
                activeVerticalSpring.setVelocity(yVelocity);
            } else if (xVelocity >= 1000) {
                activeHorizontalSpring.setEndValue(maxWidth - activeChatHead.getMeasuredWidth());
                activeVerticalSpring.setVelocity(yVelocity);
            } else {
                if (currentValue < Math.abs(maxWidth - currentValue)) {
                    activeHorizontalSpring.setEndValue(0);
                    activeVerticalSpring.setVelocity(yVelocity);
                } else {
                    activeHorizontalSpring.setEndValue(maxWidth - activeChatHead.getMeasuredWidth());
                    activeVerticalSpring.setVelocity(yVelocity);
                }
            }
        }
        if (!wasDragging) {
            return false;
        }
        return true;
    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** Bounds Check **/
        if (spring == activeHorizontalSpring) {
            double xPosition = activeHorizontalSpring.getCurrentValue();
            //currentX = (int) xPosition;
            if (xPosition + activeChatHead.getMeasuredWidth() > maxWidth && !isDragging && spring.getSpringConfig() == SpringConfigsHolder.COASTING) {
                int newPos = maxWidth - activeChatHead.getMeasuredWidth();
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeHorizontalSpring.setEndValue(newPos);
            }
            if (xPosition < 0 && !isDragging && spring.getSpringConfig() == SpringConfigsHolder.COASTING) {
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeHorizontalSpring.setEndValue(0);
            }
        } else if (spring == activeVerticalSpring) {
            double yPosition = activeVerticalSpring.getCurrentValue();
            //currentY = (int) yPosition;
            if (yPosition + activeChatHead.getMeasuredHeight() > maxHeight && !isDragging && spring.getSpringConfig() == SpringConfigsHolder.COASTING) {
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeVerticalSpring.setEndValue(maxHeight - activeChatHead.getMeasuredHeight());
            }
            if (yPosition < 0 && !isDragging && spring.getSpringConfig() == SpringConfigsHolder.COASTING) {
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
                activeVerticalSpring.setEndValue(0);
            }

        }
    }
}
