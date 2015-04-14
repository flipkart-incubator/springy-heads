package com.flipkart.chatheads.ui.arrangements;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.view.View;

import com.facebook.rebound.Spring;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringChain;
import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadArrangement;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.SpringConfigsHolder;
import com.flipkart.chatheads.ui.UpArrowLayout;

import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MaximizedArrangement<T> extends ChatHeadArrangement {
    public static final String BUNDLE_HERO_INDEX_KEY = "hero_index";
    private final Map<ChatHead, Point> positions = new ArrayMap<>();
    private ChatHeadContainer<T> container;
    private int maxWidth;
    private int maxHeight;
    private ChatHead currentTab = null;
    private UpArrowLayout arrowLayout;
    private FragmentManager fragmentManager;
    private int maxDistanceFromOriginal;
    private int topPadding;
    private Fragment currentFragment;


    public MaximizedArrangement(ChatHeadContainer<T> container) {
        this.container = container;
    }

    @Override
    public void setContainer(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void onActivate(ChatHeadContainer container, Bundle extras, int maxWidth, int maxHeight) {
        this.container = container;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        List<ChatHead> chatHeads = container.getChatHeads();
        int heroIndex = 0;
        if(extras!=null)
            heroIndex = extras.getInt(BUNDLE_HERO_INDEX_KEY, -1);
        if (heroIndex < 0 || heroIndex > chatHeads.size() - 1) {
            heroIndex = 0;
        }
        currentTab = chatHeads.get(heroIndex);
        maxDistanceFromOriginal = ChatHeadUtils.dpToPx(container.getContext(), 10);

        int widthPerHead = ChatHeadUtils.dpToPx(container.getContext(), (int) (1.1 * ChatHead.DIAMETER));
        topPadding = ChatHeadUtils.dpToPx(container.getContext(), 5);
        int leftIndent = maxWidth - (chatHeads.size() * widthPerHead);
        for (int i = 0; i < chatHeads.size(); i++) {
            ChatHead chatHead = chatHeads.get(i);
            Spring horizontalSpring = chatHead.getHorizontalSpring();
            int xPos = leftIndent + (i * widthPerHead);//align right
            horizontalSpring.setAtRest();
            horizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            horizontalSpring.setEndValue(xPos);
            Spring verticalSpring = chatHead.getVerticalSpring();
            verticalSpring.setAtRest();
            verticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            verticalSpring.setEndValue(topPadding);
            positions.put(chatHead, new Point(xPos, topPadding));

        }
        container.getCloseButton().setEnabled(true);
        container.getOverlayView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivate();
            }
        });
        container.showOverlayView();
        selectChatHead(currentTab);
    }

    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {

        if (currentFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (!currentFragment.isDetached()) {
                fragmentTransaction.detach(currentFragment);
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
        hideView();
        container.hideOverlayView();
    }

    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {


        activeHorizontalSpring.setVelocity(xVelocity);
        activeVerticalSpring.setVelocity(yVelocity);


        if (wasDragging) {
            return true;
        } else {
            if (activeChatHead != currentTab) {
                boolean handled = container.onItemSelected(activeChatHead);
                if (!handled) {
                    selectTab(activeChatHead);
                    return true;
                }
            }
            boolean handled = container.onItemSelected(activeChatHead);
            if (!handled) {
                deactivate();
            }
            return handled;
        }
    }

    private void selectTab(final ChatHead<T> activeChatHead) {
        currentTab = activeChatHead;
        container.post(new Runnable() {
            @Override
            public void run() {
                pointTo(activeChatHead);
                showView(activeChatHead, 0, 0, 0);
            }
        });
    }

    private void positionToOriginal(ChatHead activeChatHead, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        if (activeChatHead.isSticky()) {
            deactivate();
        } else {
            if (activeChatHead.getState() == ChatHead.State.FREE) {
                Point point = positions.get(activeChatHead);
                if (point != null) {
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setVelocity(0);
                    activeHorizontalSpring.setEndValue(point.x);
                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeVerticalSpring.setVelocity(0);
                    activeVerticalSpring.setEndValue(point.y);
                }
            }
        }
    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** Bounds Check **/
        if (spring == activeHorizontalSpring && !isDragging) {
            double xPosition = activeHorizontalSpring.getCurrentValue();
            if (xPosition + activeChatHead.getMeasuredWidth() > maxWidth && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (xPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
        } else if (spring == activeVerticalSpring && !isDragging) {
            double yPosition = activeVerticalSpring.getCurrentValue();

            if (yPosition + activeChatHead.getMeasuredHeight() > maxHeight && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (yPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }

        }

        /** position it back **/
        if (!isDragging && totalVelocity < ChatHeadUtils.dpToPx(container.getContext(), 50) && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING) {
            positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);

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
            arrowLayout = (UpArrowLayout) container.findViewById(R.id.arrow_layout);

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
        //arrowLayout.setAlpha(1f - ((float) distanceFromOriginal / (float) maxDistanceFromOriginal));
    }

    private void pointTo(ChatHead<T> activeChatHead) {
        UpArrowLayout arrowLayout = getArrowLayout();
        addInnerView(activeChatHead);
        Point point = positions.get(activeChatHead);
        if (point != null) {
            int padding = ChatHeadUtils.dpToPx(container.getContext(), 5);
            arrowLayout.pointTo(point.x + activeChatHead.getMeasuredWidth() / 2, point.y + activeChatHead.getMeasuredHeight() + padding);
        }
    }

    public Fragment getFragment(ChatHead<T> activeChatHead, boolean createIfRequired) {
        Fragment fragment = getFragmentManager().findFragmentByTag(activeChatHead.getKey().toString());
        if (fragment == null && createIfRequired) {
            fragment = container.getViewAdapter().getFragment(activeChatHead.getKey(), activeChatHead);
        }
        return fragment;
    }

    private void addInnerView(ChatHead<T> activeChatHead) {

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        Fragment fragment = getFragmentManager().findFragmentByTag(activeChatHead.getKey().toString());

        if (fragment == null) {
            fragment = container.getViewAdapter().getFragment(activeChatHead.getKey(), activeChatHead);
            transaction.add(getArrowLayout().getId(), fragment, activeChatHead.getKey().toString());
        } else {
            if (fragment.isDetached()) {
                transaction.attach(fragment);
            }
        }

        if (currentFragment != fragment && currentFragment != null) {
            transaction.detach(currentFragment);
        }
        currentFragment = fragment;
        transaction.commitAllowingStateLoss();
        manager.executePendingTransactions();
    }

    private void removeInnerView(ChatHead removed) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = getFragment(removed, false);
        if (fragment == null) {
            //we dont have it in our cache. So we create it and add it
        } else {
            //we have added it already sometime earlier. So re-attach it.
            transaction.remove(fragment);

        }
        if (currentFragment == fragment) currentFragment = null;
        transaction.commitAllowingStateLoss();
        manager.executePendingTransactions();
    }


    @Override
    public void onChatHeadAdded(final ChatHead chatHead) {
        //we post so that chat head measurement is done
        Spring spring = chatHead.getHorizontalSpring();
        spring.setCurrentValue(maxWidth).setAtRest();
        spring = chatHead.getVerticalSpring();
        spring.setCurrentValue(topPadding).setAtRest();

        container.post(new Runnable() {
            @Override
            public void run() {
                onActivate(container, null, maxWidth, maxHeight);
            }
        });
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        removeInnerView(removed);
        if (currentTab == removed) {
            ChatHead nextBestChatHead = getNextBestChatHead();
            if (nextBestChatHead != null) {
                selectTab(nextBestChatHead);
            }
        }
        onActivate(container, null, maxWidth, maxHeight);

    }


    @Override
    public void onCapture(ChatHeadContainer container, ChatHead activeChatHead) {
        container.removeChatHead(activeChatHead.getKey());
    }

    @Override
    public void selectChatHead(final ChatHead chatHead) {
        selectTab(chatHead);
    }


    private ChatHead getNextBestChatHead() {
        ChatHead nextBestChatHead = null;
        for (ChatHead head: container.getChatHeads()) {
            if (nextBestChatHead == null) {
                nextBestChatHead = head;
            } else if (head.getUnreadCount() >= nextBestChatHead.getUnreadCount()) {
                nextBestChatHead = head;
            }
        }
        return nextBestChatHead;
    }


    private void deactivate() {
        int heroIndex = 0;
        List<ChatHead<T>> chatHeads = container.getChatHeads();
        int i = 0;
        for (ChatHead<T> chatHead : chatHeads) {
            if (currentTab == chatHead) {
                heroIndex = i;
            }
            i++;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(MinimizedArrangement.BUNDLE_HERO_INDEX_KEY, heroIndex);
        container.setArrangement(MinimizedArrangement.class, bundle);
    }


    private FragmentManager getFragmentManager() {
        if (fragmentManager == null) {
            if (container.getViewAdapter() == null)
                throw new IllegalStateException(ChatHeadViewAdapter.class.getSimpleName() + " should not be null");
            fragmentManager = container.getViewAdapter().getFragmentManager();
            if (fragmentManager == null)
                throw new IllegalStateException(FragmentManager.class.getSimpleName() + " returned from " + ChatHeadViewAdapter.class.getSimpleName() + " should not be null");
        }
        return fragmentManager;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void bringToFront(ChatHead chatHead) {
        //nothing to do, everything is in front.
    }
}