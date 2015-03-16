package com.flipkart.chatheads.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.rebound.Spring;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ModifiedSpringChain;

import java.util.List;
import java.util.Map;

/**
 * Created by kirankumar on 13/02/15.
 */
public class MaximizedArrangement<T> extends ChatHeadArrangement {
    private final Map<ChatHead, Point> positions = new ArrayMap<>();
    private ChatHeadViewAdapter adapter;
    private ChatHeadContainer<T> container;
    private int maxWidth;
    private int maxHeight;
    private ChatHead currentTab = null;
    private UpArrowLayout arrowLayout;
    private FragmentManager fragmentManager;

    @Override
    public void onActivate(ChatHeadContainer container, ChatHead activeChatHead, ChatHeadSpringsHolder springsHolder, int maxWidth, int maxHeight) {
        this.container = container;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        springsHolder.setChaining(false);
        List<ModifiedSpringChain.SpringData> horizontalSprings = springsHolder.getHorizontalSpringChain().getAllSprings();
        List<ModifiedSpringChain.SpringData> verticalSprings = springsHolder.getVerticalSpringChain().getAllSprings();
        int currentX = 0;
        int currentY = 0;

        for (ModifiedSpringChain.SpringData horizontalSpring : horizontalSprings) {
            horizontalSpring.getSpring().setSpringConfig(SpringConfigsHolder.CONVERGING);
            horizontalSpring.getSpring().setEndValue(currentX);
            ChatHead chatHead = (ChatHead) horizontalSpring.getKey();
            positions.put(chatHead, new Point(currentX, currentY));
            currentX += 200;
        }
        for (ModifiedSpringChain.SpringData verticalSpring : verticalSprings) {
            verticalSpring.getSpring().setSpringConfig(SpringConfigsHolder.CONVERGING);
            verticalSpring.getSpring().setEndValue(currentY);
        }
        if (activeChatHead != null) {
            selectTab(activeChatHead);
        }

    }

    @Override
    public void onDeactivate(ChatHead activeChatHead, int maxWidth, int maxHeight, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        hideView(activeChatHead);
    }

    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {
        positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);

        if (wasDragging) {
            return true;
        } else {
            if (activeChatHead != currentTab) {
                selectTab(activeChatHead);
                return true;
            }
            return false;

        }
    }

    private void selectTab(ChatHead activeChatHead) {
        currentTab = activeChatHead;
        pointTo(activeChatHead);
    }

    private void positionToOriginal(ChatHead activeChatHead, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        Point point = positions.get(activeChatHead);
        if (point != null) {
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
            activeHorizontalSpring.setVelocity(0);
            activeHorizontalSpring.setEndValue(point.x);
            activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CONVERGING);
            activeVerticalSpring.setVelocity(0);
            activeVerticalSpring.setEndValue(point.y);
        }
    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** Bounds Check **/
        if (spring == activeHorizontalSpring && !isDragging) {
            double xPosition = activeHorizontalSpring.getCurrentValue();
            if (xPosition + activeChatHead.getMeasuredWidth() > maxWidth && !isDragging && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CONVERGING) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (xPosition < 0 && !isDragging && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CONVERGING) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
        } else if (spring == activeVerticalSpring && !isDragging) {
            double yPosition = activeVerticalSpring.getCurrentValue();

            if (yPosition + activeChatHead.getMeasuredHeight() > maxHeight && !isDragging && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CONVERGING) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (yPosition < 0 && !isDragging && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CONVERGING) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }

        }

        if (totalVelocity < 500 && !isDragging) {
            positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
        }

        if (spring == activeVerticalSpring || spring == activeHorizontalSpring) {
            Point point = positions.get(activeChatHead);
            if (!isDragging && activeHorizontalSpring.isAtRest() && activeVerticalSpring.isAtRest()) {
                showView(activeChatHead);
            } else if (currentTab == activeChatHead) {
                hideView(activeChatHead);
            }
        }
    }

    private UpArrowLayout getArrowLayout() {
        if (arrowLayout == null) {
            LayoutInflater  inflater = (LayoutInflater)container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.arrow_layout,container,true);
            arrowLayout = (UpArrowLayout) view.findViewById(R.id.arrow_layout);

        }
        return arrowLayout;
    }

    private void hideView(ChatHead activeChatHead) {
        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.GONE);
    }

    private void showView(ChatHead activeChatHead) {

        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.VISIBLE);


    }

    private void pointTo(ChatHead activeChatHead) {
        UpArrowLayout arrowLayout = getArrowLayout();
        replaceInnerView(activeChatHead);
        Point point = positions.get(activeChatHead);
        arrowLayout.pointTo(point.x + activeChatHead.getMeasuredWidth() / 2, point.y + activeChatHead.getMeasuredHeight());
        arrowLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,container.getMeasuredHeight() - point.y - activeChatHead.getMeasuredHeight()));
    }

    private void replaceInnerView(ChatHead activeChatHead) {
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag(activeChatHead.getKey().toString());
        if(fragment==null)
        {
            fragment = adapter.getFragment(activeChatHead.getKey(),activeChatHead);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(getArrowLayout().getId(),fragment,activeChatHead.getKey().toString());
        transaction.commitAllowingStateLoss();

    }



    @Override
    public void onChatHeadAdded(ChatHead chatHead, ChatHeadSpringsHolder springsHolder) {
        onActivate(container, null, springsHolder, maxWidth, maxHeight);
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed, ChatHeadSpringsHolder springsHolder) {
        onActivate(container, null, springsHolder, maxWidth, maxHeight);
        if (currentTab == removed) {
            ChatHead nextBestChatHead = getNextBestChatHead();
            if (nextBestChatHead != null)
                selectTab(nextBestChatHead);
            else
                container.toggleArrangement(null);
        }

    }

    @Override
    public void onCapture(ChatHeadContainer container, ChatHead activeChatHead) {
        container.removeChatHead(activeChatHead.getKey());
    }

    @Override
    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {
        this.adapter = chatHeadViewAdapter;
    }


    public ChatHead getNextBestChatHead() {
        ChatHead nextBestChatHead = null;
        for (Map.Entry<T, ChatHead> entry : container.getChatHeads().entrySet()) {
            ChatHead head = entry.getValue();
            if (nextBestChatHead == null) {
                nextBestChatHead = head;
            } else if (head.getUnreadCount() >= nextBestChatHead.getUnreadCount()) {
                nextBestChatHead = head;
            }
        }
        return nextBestChatHead;
    }


    public FragmentManager getFragmentManager() {
        if(fragmentManager == null)
        {
            fragmentManager = adapter.getFragmentManager();
            if(fragmentManager == null) throw new IllegalStateException(FragmentManager.class.getSimpleName()+" returned from "+ChatHeadViewAdapter.class.getSimpleName()+" should not be null");
        }
        return fragmentManager;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }
}
