package com.flipkart.chatheads.reboundextensions;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadContainer;

import java.util.List;

/**
 * Performs spring chaining as well as gives a usable api with chathead as the key.
 */
public class ChatHeadSpringsHolder {

    public ChatHeadSpringChain getHorizontalSpringChain() {
        return mHorizontalSpringChain;
    }

    public ChatHeadSpringChain getVerticalSpringChain() {
        return mVerticalSpringChain;
    }

    private final ChatHeadSpringChain mHorizontalSpringChain;
    private final ChatHeadSpringChain mVerticalSpringChain;

    public Spring getActiveHorizontalSpring() {
        return mActiveHorizontalSpring;
    }

    public Spring getActiveVerticalSpring() {
        return mActiveVerticalSpring;
    }

    private Spring mActiveHorizontalSpring;
    private Spring mActiveVerticalSpring;

    public ChatHeadSpringsHolder(ChatHeadContainer container) {
        mHorizontalSpringChain = ChatHeadSpringChain.create(container);
        mVerticalSpringChain = ChatHeadSpringChain.create(container);
    }

    public void addChatHead(ChatHeadContainer container, ChatHead chatHead,SpringListener commonListener, boolean isSticky)
    {
        ChatHeadSpringChain.SpringData horSpringData = mHorizontalSpringChain.addSpring(container, chatHead, chatHead.getHorizontalPositionListener(), isSticky);
        ChatHeadSpringChain.SpringData verSpringData = mVerticalSpringChain.addSpring(container, chatHead,chatHead.getVerticalPositionListener(), isSticky);
        horSpringData.getSpring().addListener(commonListener);
        verSpringData.getSpring().addListener(commonListener);

    }

    public ChatHeadSpringChain.SpringData getHorizontalSpring(ChatHead chatHead)
    {
        return mHorizontalSpringChain.getSpring(chatHead);
    }
    public ChatHeadSpringChain.SpringData getVerticalSpring(ChatHead chatHead)
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

    public  ChatHeadSpringChain.SpringData getOldestSpring(ChatHeadSpringChain springChain, boolean avoidSticky) {
        List<ChatHeadSpringChain.SpringData> allSprings = springChain.getAllSprings();
            int minIndex = Integer.MAX_VALUE;
            int arrayIndex = 0;
            for (int i = 0; i<allSprings.size(); i++)
            {
                ChatHeadSpringChain.SpringData allSpring = allSprings.get(i);
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

    private void setChaining(boolean enabled, ChatHeadSpringChain springChain) {
        List<ChatHeadSpringChain.SpringData> allSprings = springChain.getAllSprings();

        for (int i = 0; i < allSprings.size(); i++) {
            ChatHeadSpringChain.SpringData springData = allSprings.get(i);
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
