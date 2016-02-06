package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.View;

import com.facebook.rebound.Spring;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.ChatHeadUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MaximizedArrangement<T extends Serializable> extends ChatHeadArrangement {
    public static final String BUNDLE_HERO_INDEX_KEY = "hero_index";
    private static double MAX_DISTANCE_FROM_ORIGINAL;
    private static int MIN_VELOCITY_TO_POSITION_BACK;
    private final Map<ChatHead, Point> positions = new ArrayMap<>();
    private ChatHeadContainer<T> container;
    private int maxWidth;
    private int maxHeight;
    private ChatHead currentChatHead = null;
    private UpArrowLayout arrowLayout;
    private int maxDistanceFromOriginal;
    private int topPadding;
    private boolean isActive = false;


    public MaximizedArrangement(ChatHeadContainer<T> container) {
        this.container = container;
    }


    @Override
    public void setContainer(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void onActivate(ChatHeadContainer container, Bundle extras, int maxWidth, int maxHeight, boolean animated) {
        this.container = container;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        MIN_VELOCITY_TO_POSITION_BACK = ChatHeadUtils.dpToPx(container.getDisplayMetrics(), 50);
        MAX_DISTANCE_FROM_ORIGINAL = ChatHeadUtils.dpToPx(container.getContext(), 10);
        isActive = true;
        List<ChatHead> chatHeads = container.getChatHeads();
        int heroIndex = 0;
        if (extras != null)
            heroIndex = extras.getInt(BUNDLE_HERO_INDEX_KEY, -1);
        if (heroIndex < 0 && currentChatHead != null) {
            heroIndex = getHeroIndex(); //this means we have a current chat head and we carry it forward
        }
        if (heroIndex < 0 || heroIndex > chatHeads.size() - 1) {
            heroIndex = 0;
        }
        if (chatHeads.size() > 0 && heroIndex < chatHeads.size()) {
            currentChatHead = chatHeads.get(heroIndex);
            maxDistanceFromOriginal = (int) MAX_DISTANCE_FROM_ORIGINAL;

            int spacing = container.getConfig().getHeadHorizontalSpacing(maxWidth, maxHeight);
            int widthPerHead = container.getConfig().getHeadWidth();
            topPadding = ChatHeadUtils.dpToPx(container.getContext(), 5);
            int leftIndent = maxWidth - (chatHeads.size() * (widthPerHead + spacing));
            for (int i = 0; i < chatHeads.size(); i++) {
                ChatHead chatHead = chatHeads.get(i);
                Spring horizontalSpring = chatHead.getHorizontalSpring();
                int xPos = leftIndent + (i * (widthPerHead + spacing));//align right
                positions.put(chatHead, new Point(xPos, topPadding));
                horizontalSpring.setAtRest();
                horizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                horizontalSpring.setEndValue(xPos);
                if (!animated) {
                    horizontalSpring.setCurrentValue(xPos);
                }
                Spring verticalSpring = chatHead.getVerticalSpring();
                verticalSpring.setAtRest();
                verticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                verticalSpring.setEndValue(topPadding);
                if (!animated) {
                    verticalSpring.setCurrentValue(topPadding);
                }

            }
            container.getCloseButton().setEnabled(true);
            container.getOverlayView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deactivate();
                }
            });
            container.showOverlayView(animated);
            selectChatHead(currentChatHead);
        }
    }


    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        if (currentChatHead != null) {
            container.detachFragment(currentChatHead);
        }
        hideView();
        container.hideOverlayView(true);
        positions.clear();
        isActive = false;
    }


    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {


        if (xVelocity == 0 && yVelocity == 0) {
            // this is a hack. If both velocities are 0, onSprintUpdate is not called and the chat head remains whereever it is
            // so we give a a negligible velocity to artificially fire onSpringUpdate
            xVelocity = 1;
            yVelocity = 1;
        }

        activeHorizontalSpring.setVelocity(xVelocity);
        activeVerticalSpring.setVelocity(yVelocity);


        if (wasDragging) {
            return true;
        } else {
            if (activeChatHead != currentChatHead) {
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
        currentChatHead = activeChatHead;
        showOrHideView(activeChatHead);
        pointTo(activeChatHead);
    }

    private void positionToOriginal(ChatHead activeChatHead, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        if (activeChatHead.isSticky()) {
            Point point = positions.get(activeChatHead);
            if (point != null) {
                double distanceFromOriginal = Math.hypot(point.x - activeHorizontalSpring.getCurrentValue(), point.y - activeVerticalSpring.getCurrentValue());
                if (distanceFromOriginal > MAX_DISTANCE_FROM_ORIGINAL) {
                    deactivate();
                    return;

                }
            }
        }

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

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** Bounds Check **/
        if (spring == activeHorizontalSpring && !isDragging) {
            double xPosition = activeHorizontalSpring.getCurrentValue();
            if (xPosition + container.getConfig().getHeadWidth() > maxWidth && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (xPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
        } else if (spring == activeVerticalSpring && !isDragging) {
            double yPosition = activeVerticalSpring.getCurrentValue();

            if (yPosition + container.getConfig().getHeadHeight() > maxHeight && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (yPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }

        }

        /** position it back **/
        if (!isDragging && totalVelocity < MIN_VELOCITY_TO_POSITION_BACK && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING) {
            positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);

        }

        if (activeChatHead == currentChatHead)

            showOrHideView(activeChatHead);

        if (!isDragging) {
            /** Capturing check **/
            int[] coords = container.getChatHeadCoordsForCloseButton(activeChatHead);
            double distanceCloseButtonFromHead = container.getDistanceCloseButtonFromHead((float) activeHorizontalSpring.getCurrentValue() + container.getConfig().getHeadWidth() / 2, (float) activeVerticalSpring.getCurrentValue() + container.getConfig().getHeadHeight() / 2);

            if (distanceCloseButtonFromHead < activeChatHead.CLOSE_ATTRACTION_THRESHOLD && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING && activeVerticalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING && !activeChatHead.isSticky()) {

                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeChatHead.setState(ChatHead.State.CAPTURED);
            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED && activeHorizontalSpring.getSpringConfig()!= SpringConfigsHolder.CAPTURING) {
                activeHorizontalSpring.setAtRest();
                activeVerticalSpring.setAtRest();
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CAPTURING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CAPTURING);
                activeHorizontalSpring.setEndValue(coords[0]);
                activeVerticalSpring.setEndValue(coords[1]);

            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED && activeVerticalSpring.isAtRest()) {
                container.getCloseButton().disappear(false, true);
                container.captureChatHeads(activeChatHead);
            }
            if (!activeVerticalSpring.isAtRest()) {
                container.getCloseButton().appear();
            } else {
                container.getCloseButton().disappear(true, true);
            }
        }
    }

    private void showOrHideView(ChatHead activeChatHead) {
        Point point = positions.get(activeChatHead);
        if (point != null) {
            double dx = activeChatHead.getHorizontalSpring().getCurrentValue() - point.x;
            double dy = activeChatHead.getVerticalSpring().getCurrentValue() - point.y;
            double distanceFromOriginal = Math.hypot(dx, dy);
            if (distanceFromOriginal < maxDistanceFromOriginal) {
                showView(activeChatHead, dx, dy, distanceFromOriginal);
            } else {
                hideView();
            }
        }

    }

    private UpArrowLayout getArrowLayout() {
        if (arrowLayout == null) {
            arrowLayout = (UpArrowLayout) container.findViewById(R.id.arrow_layout);

        }
        return arrowLayout;
    }

    private boolean isViewHidden() {
        UpArrowLayout arrowLayout = getArrowLayout();
        if (arrowLayout != null) {
            return arrowLayout.getVisibility() == View.GONE;
        }
        return true;
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
        container.addFragment(activeChatHead, arrowLayout);
        Point point = positions.get(activeChatHead);
        if (point != null) {
            int padding = container.getConfig().getHeadVerticalSpacing(maxWidth, maxHeight);
            arrowLayout.pointTo(point.x + container.getConfig().getHeadWidth() / 2, point.y + container.getConfig().getHeadHeight() + padding);
        }
    }


    @Override
    public void onChatHeadAdded(final ChatHead chatHead, final boolean animated) {
        //we post so that chat head measurement is done
        Spring spring = chatHead.getHorizontalSpring();
        spring.setCurrentValue(maxWidth).setAtRest();
        spring = chatHead.getVerticalSpring();
        spring.setCurrentValue(topPadding).setAtRest();
        onActivate(container, getBundleWithHero(), maxWidth, maxHeight, animated);

    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        container.removeFragment(removed);
        positions.remove(removed);
        boolean isEmpty = false;
        if (currentChatHead == removed) {
            ChatHead nextBestChatHead = getNextBestChatHead();
            if (nextBestChatHead != null) {
                isEmpty = false;
                selectTab(nextBestChatHead);
            } else {
                isEmpty = true;
            }
        }
        if (!isEmpty) {
            onActivate(container, getBundleWithHero(), maxWidth, maxHeight, true);
        } else {
            deactivate();
        }

    }


    @Override
    public void onCapture(ChatHeadContainer container, ChatHead activeChatHead) {
        if (!activeChatHead.isSticky()) {
            container.removeChatHead(activeChatHead.getKey(), true);
        }
    }

    @Override
    public void selectChatHead(final ChatHead chatHead) {
        selectTab(chatHead);
    }


    private ChatHead getNextBestChatHead() {
        ChatHead nextBestChatHead = null;
        for (ChatHead head : container.getChatHeads()) {
            if (nextBestChatHead == null) {
                nextBestChatHead = head;
            } else if (head.getUnreadCount() >= nextBestChatHead.getUnreadCount()) {
                nextBestChatHead = head;
            }
        }
        return nextBestChatHead;
    }

    private Bundle getBundleWithHero() {
        Bundle bundle = new Bundle();
        bundle.putInt(MinimizedArrangement.BUNDLE_HERO_INDEX_KEY, getHeroIndex());
        return bundle;
    }

    private void deactivate() {
        container.setArrangement(MinimizedArrangement.class, getBundleWithHero());
        hideView();
    }

    /**
     * @return the index of the selected chat head a.k.a the hero
     */
    @Override
    public Integer getHeroIndex() {
        int heroIndex = 0;
        List<ChatHead<T>> chatHeads = container.getChatHeads();
        int i = 0;
        for (ChatHead<T> chatHead : chatHeads) {
            if (currentChatHead == chatHead) {
                heroIndex = i;
            }
            i++;
        }
        return heroIndex;
    }

    @Override
    public void onConfigChanged(ChatHeadConfig newConfig) {

    }

    @Override
    public Bundle getRetainBundle() {
        return getBundleWithHero();
    }

    @Override
    public boolean canDrag(ChatHead chatHead) {
        if (chatHead.isSticky()) return false;
        return true;
    }

    @Override
    public void removeOldestChatHead() {
        for (ChatHead<T> chatHead : container.getChatHeads()) {
            //we dont remove sticky chat heads as well as the currently selected chat head
            if (!chatHead.isSticky() && chatHead != currentChatHead) {
                container.removeChatHead(chatHead.getKey(), false);
                break;
            }
        }
    }


    @Override
    public void bringToFront(final ChatHead chatHead) {
        //nothing to do, everything is in front.
        container.post(new Runnable() {
            @Override
            public void run() {
                selectChatHead(chatHead);
            }
        });
    }

    @Override
    public void onReloadFragment(ChatHead chatHead) {
        if (currentChatHead != null && chatHead == currentChatHead) {
            container.addFragment(chatHead, getArrowLayout());
        }
    }

    @Override
    public boolean shouldShowCloseButton(ChatHead chatHead) {
        if (chatHead.isSticky()) return false;
        return true;
    }

}