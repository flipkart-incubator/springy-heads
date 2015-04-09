package com.flipkart.chatheads.reboundextensions;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.ui.ChatHead;

import java.util.List;

/**
 * Performs spring chaining as well as gives a usable api with chathead as the key.
 */
public class ChatHeadSpringsHolder {

    public ModifiedSpringChain getHorizontalSpringChain() {
        return mHorizontalSpringChain;
    }

    public ModifiedSpringChain getVerticalSpringChain() {
        return mVerticalSpringChain;
    }

    private final ModifiedSpringChain mHorizontalSpringChain;
    private final ModifiedSpringChain mVerticalSpringChain;

    public Spring getActiveHorizontalSpring() {
        return mActiveHorizontalSpring;
    }

    public Spring getActiveVerticalSpring() {
        return mActiveVerticalSpring;
    }

    private Spring mActiveHorizontalSpring;
    private Spring mActiveVerticalSpring;

    public ChatHeadSpringsHolder() {
        mHorizontalSpringChain = ModifiedSpringChain.create();
        mVerticalSpringChain = ModifiedSpringChain.create();
    }

    public void addChatHead(ChatHead chatHead,SpringListener commonListener, boolean isSticky)
    {
        Spring horSpring = mHorizontalSpringChain.addSpring(chatHead,chatHead.getHorizontalPositionListener(),isSticky);
        Spring verSpring = mVerticalSpringChain.addSpring(chatHead,chatHead.getVerticalPositionListener(), isSticky);
        horSpring.setRestDisplacementThreshold(10);
        verSpring.setRestDisplacementThreshold(10);
        horSpring.setRestSpeedThreshold(10);
        verSpring.setRestSpeedThreshold(10);
        horSpring.addListener(commonListener);
        verSpring.addListener(commonListener);
    }

    public ModifiedSpringChain.SpringData getHorizontalSpring(ChatHead chatHead)
    {
        return mHorizontalSpringChain.getSpring(chatHead);
    }
    public ModifiedSpringChain.SpringData getVerticalSpring(ChatHead chatHead)
    {
        return mVerticalSpringChain.getSpring(chatHead);
    }

    public SpringSystem getHorizontalSpringSystem()
    {
        return mHorizontalSpringChain.getSpringSystem();
    }
    public SpringSystem getVerticalSpringSystem()
    {
        return mVerticalSpringChain.getSpringSystem();
    }

    public  ModifiedSpringChain.SpringData getOldestSpring(ModifiedSpringChain springChain, boolean avoidSticky) {
        List<ModifiedSpringChain.SpringData> allSprings = springChain.getAllSprings();
            int minIndex = Integer.MAX_VALUE;
            int arrayIndex = 0;
            for (int i = 0; i<allSprings.size(); i++)
            {
                ModifiedSpringChain.SpringData allSpring = allSprings.get(i);
                int index = allSpring.getIndex();
                if(index<minIndex) {
                    if(allSpring.isSticky() && avoidSticky)
                    {
                        continue;
                    }
                    minIndex = index;
                    arrayIndex = i;
                }
            }
        return allSprings.get(arrayIndex);

        }


    public void removeChatHead(ChatHead chatHead)
    {
        Spring removedHorSpring = mHorizontalSpringChain.removeSpring(chatHead);
        Spring removedVerSpring = mVerticalSpringChain.removeSpring(chatHead);


    }

    /**
     * Marks the springs for the specified chat head as active.
     * @param chatHead
     */
    public void selectSpring(ChatHead chatHead)
    {
        mHorizontalSpringChain.setControlSpring(chatHead);
        mActiveHorizontalSpring = mHorizontalSpringChain.getControlSpring();
        mHorizontalSpringChain.moveControlSpringToEnd();
        mVerticalSpringChain.setControlSpring(chatHead);
        mActiveVerticalSpring = mVerticalSpringChain.getControlSpring();
        mVerticalSpringChain.moveControlSpringToEnd();
    }

    public void setChaining(boolean enabled)
    {
        setChaining(enabled,mHorizontalSpringChain);
        setChaining(enabled,mVerticalSpringChain);
        mVerticalSpringChain.activateFollowControlSpring();
        mHorizontalSpringChain.activateFollowControlSpring();
    }

    private void setChaining(boolean enabled, ModifiedSpringChain springChain) {
        List<ModifiedSpringChain.SpringData> allSprings = springChain.getAllSprings();

        for (int i = 0; i < allSprings.size(); i++) {
            ModifiedSpringChain.SpringData springData = allSprings.get(i);
            SpringListener springListener = springData.getListener();
            if(enabled)
            {
                springData.getSpring().addListener(springChain);
            }
            else {
                springData.getSpring().removeListener(springChain);
                springData.getSpring().addListener(springListener);
            }
        }
    }

    public void setChainDelta(double deltaX, double deltaY)
    {
        mHorizontalSpringChain.setDelta(deltaX);
        mVerticalSpringChain.setDelta(deltaY);
    }
}
