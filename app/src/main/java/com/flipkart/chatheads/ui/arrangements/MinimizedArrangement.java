package com.flipkart.chatheads.ui.arrangements;

import android.os.Bundle;

import com.facebook.rebound.Spring;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadArrangement;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.SpringConfigsHolder;

public class MinimizedArrangement extends ChatHeadArrangement {

    private int currentX = 0;
    private int currentY = -Integer.MAX_VALUE;
    private int maxWidth;
    private int maxHeight;
    private ChatHeadContainer container;

    public MinimizedArrangement(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void setContainer(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void onActivate(ChatHeadContainer container, Bundle extras, ChatHeadSpringsHolder springsHolder, int maxWidth, int maxHeight) {
        this.container = container;
        if (currentY < 0) {
            currentY = (int) (maxHeight * 0.8);
        }
        if (springsHolder.getActiveHorizontalSpring() != null)
            springsHolder.getActiveHorizontalSpring().setEndValue(currentX);
        if (springsHolder.getActiveVerticalSpring() != null)
            springsHolder.getActiveVerticalSpring().setEndValue(currentY);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        springsHolder.setChaining(true);
        container.getCloseButton().setEnabled(false);
    }

    @Override
    public void onChatHeadAdded(ChatHead chatHead, ChatHeadSpringsHolder springsHolder) {
        onActivate(container, null, springsHolder, maxWidth, maxHeight);
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
    public void selectChatHead(ChatHead chatHead) {
        //container.toggleArrangement();
    }

    @Override
    public void onDeactivate(int maxWidth, int maxHeight, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        currentX = (int) activeHorizontalSpring.getCurrentValue();
        currentY = (int) activeVerticalSpring.getCurrentValue();
    }

    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {

        if(Math.abs(xVelocity)<ChatHeadUtils.dpToPx(container.getContext(),50))
        {
            if(activeHorizontalSpring.getCurrentValue() < (maxWidth-activeHorizontalSpring.getCurrentValue()))
            {
                xVelocity = -1;
            }
            else
            {
                xVelocity = 1;
            }
        }
        if (xVelocity < 0) {
            int newVelocity = (int) (-activeHorizontalSpring.getCurrentValue() * SpringConfigsHolder.DRAGGING.friction);
            if (xVelocity > newVelocity)
                xVelocity = (newVelocity);

        } else if (xVelocity > 0) {
            int newVelocity = (int) ((maxWidth - activeHorizontalSpring.getCurrentValue()) * SpringConfigsHolder.DRAGGING.friction);
            if (newVelocity > xVelocity)
                xVelocity = (newVelocity);
        }
        activeHorizontalSpring.setVelocity(xVelocity);
        activeVerticalSpring.setVelocity(yVelocity);

        if (!wasDragging) {
            boolean handled = container.onItemSelected(activeChatHead);
            if (!handled) {
                deactivate();
                return false;
            }
        }
        return true;
    }

    private void deactivate() {
        container.setArrangement(MaximizedArrangement.class, null);
    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** This method does a bounds Check **/
        double xVelocity = activeHorizontalSpring.getVelocity();
        double yVelocity = activeVerticalSpring.getVelocity();
        if (!isDragging && Math.abs(totalVelocity) < ChatHeadUtils.dpToPx(container.getContext(), 600)) {
            if (spring == activeHorizontalSpring) {

                double xPosition = activeHorizontalSpring.getCurrentValue();
                if (xPosition + activeChatHead.getMeasuredWidth() > maxWidth) {
                    //outside the right bound
                    //System.out.println("outside the right bound !! xPosition = " + xPosition);
                    int newPos = maxWidth - activeChatHead.getMeasuredWidth();
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setEndValue(newPos);
                    activeVerticalSpring.setVelocity(0);
                } else if (xPosition < 0) {
                    //outside the left bound
                    //System.out.println("outside the left bound !! xPosition = " + xPosition);
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setEndValue(0);
                    activeVerticalSpring.setVelocity(0);

                } else {
                    //within bound


                }
            } else if (spring == activeVerticalSpring) {
                double yPosition = activeVerticalSpring.getCurrentValue();
                if (yPosition + activeChatHead.getMeasuredHeight() > maxHeight) {
                    //outside the bottom bound
                    //System.out.println("outside the bottom bound !! yPosition = " + yPosition);

                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeVerticalSpring.setEndValue(maxHeight - activeChatHead.getMeasuredHeight());
                } else if (yPosition < 0) {
                    //outside the top bound
                    //System.out.println("outside the top bound !! yPosition = " + yPosition);

                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeVerticalSpring.setEndValue(0);
                } else {
                    //within boundt
                }

            }
        }
    }
}
