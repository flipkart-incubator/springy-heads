package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.rebound.Spring;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.reboundextensions.ModifiedSpringChain;

import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MaximizedArrangement<T> extends ChatHeadArrangement {
    private final Map<ChatHead, Point> positions = new ArrayMap<>();
    private ChatHeadViewAdapter<T> adapter;
    private ChatHeadContainer<T> container;
    private int maxWidth;
    private int maxHeight;
    private ChatHead currentTab = null;
    private UpArrowLayout arrowLayout;
    private FragmentManager fragmentManager;
    private final Map<T, Fragment> fragments = new ArrayMap<T, Fragment>();
    private int maxDistanceFromOriginal;

    @Override
    public void onActivate(ChatHeadContainer container, ChatHeadSpringsHolder springsHolder, int maxWidth, int maxHeight) {
        this.container = container;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        springsHolder.setChaining(false);
        List<ModifiedSpringChain.SpringData> horizontalSprings = springsHolder.getHorizontalSpringChain().getAllSprings();
        List<ModifiedSpringChain.SpringData> verticalSprings = springsHolder.getVerticalSpringChain().getAllSprings();
        maxDistanceFromOriginal = ChatHeadUtils.dpToPx(container.getContext(), 50);

        int widthPerHead = ChatHeadUtils.dpToPx(container.getContext(), 75);
        ChatHead<T> lastChatHead = null;
        int leftIndent = maxWidth - (horizontalSprings.size() * widthPerHead);
        for (int i = 0; i < horizontalSprings.size(); i++) {
            ModifiedSpringChain.SpringData horizontalSpring = horizontalSprings.get(i);
            int xPos = leftIndent + (horizontalSpring.getIndex() * widthPerHead);//align right
            horizontalSpring.getSpring().setAtRest();
            horizontalSpring.getSpring().setSpringConfig(SpringConfigsHolder.CONVERGING);
            horizontalSpring.getSpring().setEndValue(xPos);
            ChatHead chatHead = (ChatHead) horizontalSpring.getKey();
            positions.put(chatHead, new Point(xPos, 0));
            lastChatHead = chatHead;
        }
        for (int i = 0; i < verticalSprings.size(); i++) {
            ModifiedSpringChain.SpringData verticalSpring = verticalSprings.get(i);
            verticalSpring.getSpring().setAtRest();
            verticalSpring.getSpring().setSpringConfig(SpringConfigsHolder.CONVERGING);
            verticalSpring.getSpring().setEndValue(0);
        }
        if (lastChatHead != null) {
            selectTab(lastChatHead);
        }
    }

    @Override
    public void onDeactivate(int maxWidth, int maxHeight, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        hideView();
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

    private void selectTab(final ChatHead<T> activeChatHead) {
        currentTab = activeChatHead;
        container.post(new Runnable() {
            @Override
            public void run() {
                pointTo(activeChatHead);
            }
        });
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
            if (xPosition + activeChatHead.getMeasuredWidth() > maxWidth && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CONVERGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (xPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CONVERGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
        } else if (spring == activeVerticalSpring && !isDragging) {
            double yPosition = activeVerticalSpring.getCurrentValue();

            if (yPosition + activeChatHead.getMeasuredHeight() > maxHeight && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CONVERGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (yPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CONVERGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }

        }


        if (spring == activeVerticalSpring || spring == activeHorizontalSpring) {

            if (currentTab == activeChatHead) {
                Point point = positions.get(activeChatHead);
                if (point != null) {
                    double dx = activeHorizontalSpring.getCurrentValue() - point.x;
                    double dy = activeVerticalSpring.getCurrentValue() - point.y;
                    double distanceFromOriginal = Math.hypot(dx, dy);
                    if (distanceFromOriginal < maxDistanceFromOriginal) {
                        showView(activeChatHead, dx, dy, distanceFromOriginal);
                    } else {
                        hideView();
                    }
                }
            }
        }
    }

    private UpArrowLayout getArrowLayout() {
        if (arrowLayout == null) {
            LayoutInflater inflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.arrow_layout, container, true);
            arrowLayout = (UpArrowLayout) view.findViewById(R.id.arrow_layout);

        }
        return arrowLayout;
    }

    private void hideView() {
        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.GONE);
    }

    private void showView(ChatHead activeChatHead, double dx, double dy, double distanceFromOriginal) {
        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.VISIBLE);
        arrowLayout.setTranslationX((float) dx);
        arrowLayout.setTranslationY((float) dy);
        arrowLayout.setAlpha(1f - ((float) distanceFromOriginal / (float) maxDistanceFromOriginal));
    }

    private void pointTo(ChatHead<T> activeChatHead) {
        UpArrowLayout arrowLayout = getArrowLayout();
        replaceInnerView(activeChatHead);
        Point point = positions.get(activeChatHead);
        if (point != null) {
            arrowLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, container.getMeasuredHeight() - point.y - activeChatHead.getMeasuredHeight()));
            arrowLayout.pointTo(point.x + activeChatHead.getMeasuredWidth() / 2, point.y + activeChatHead.getMeasuredHeight());
        }
    }

    private void replaceInnerView(ChatHead<T> activeChatHead) {
        FragmentManager manager = getFragmentManager();
        Fragment fragment = fragments.get(activeChatHead.getKey());
        if (fragment == null) {
            fragment = adapter.getFragment(activeChatHead.getKey(), activeChatHead);
            fragments.put(activeChatHead.getKey(), fragment);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(getArrowLayout().getId(), fragment, activeChatHead.getKey().toString());
        transaction.commitAllowingStateLoss();
        manager.executePendingTransactions();
    }


    @Override
    public void onChatHeadAdded(final ChatHead chatHead, final ChatHeadSpringsHolder springsHolder) {
        //we post so that chat head measurement is done
        ModifiedSpringChain.SpringData horizontalSpring = springsHolder.getHorizontalSpring(chatHead);
        Spring spring = horizontalSpring.getSpring();
        spring.setCurrentValue(maxWidth).setAtRest();
        container.post(new Runnable() {
            @Override
            public void run() {
                onActivate(container, springsHolder, maxWidth, maxHeight);
            }
        });
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed, ChatHeadSpringsHolder springsHolder) {
        onActivate(container, springsHolder, maxWidth, maxHeight);
        fragments.remove(removed.getKey());
        if (currentTab == removed) {
            ChatHead nextBestChatHead = getNextBestChatHead();
            if (nextBestChatHead != null)
                selectTab(nextBestChatHead);
            else
                container.toggleArrangement();
        }
    }

    @Override
    public void onCapture(ChatHeadContainer container, ChatHead activeChatHead) {
        container.removeChatHead(activeChatHead.getKey());
    }

    @Override
    public void selectChatHead(final ChatHead chatHead) {
        selectTab(chatHead);
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
        if (fragmentManager == null) {
            fragmentManager = adapter.getFragmentManager();
            if (fragmentManager == null)
                throw new IllegalStateException(FragmentManager.class.getSimpleName() + " returned from " + ChatHeadViewAdapter.class.getSimpleName() + " should not be null");
        }
        return fragmentManager;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }
}