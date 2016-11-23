package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
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
    private ChatHeadManager<T> manager;
    private int maxWidth;
    private int maxHeight;
    private ChatHead currentChatHead = null;
    private UpArrowLayout arrowLayout;
    private int maxDistanceFromOriginal;
    private int topPadding;
    private boolean isActive = false;
    private boolean isTransitioning = false;
    private Bundle extras;

    public MaximizedArrangement(ChatHeadManager<T> manager) {
        this.manager = manager;
    }


    @Override
    public void setContainer(ChatHeadManager container) {
        this.manager = container;
    }

    @Override
    public void onActivate(ChatHeadManager container, Bundle extras, int maxWidth, int maxHeight, boolean animated) {
        isTransitioning = true;
        this.manager = container;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        MIN_VELOCITY_TO_POSITION_BACK = ChatHeadUtils.dpToPx(container.getDisplayMetrics(), 50);
        MAX_DISTANCE_FROM_ORIGINAL = ChatHeadUtils.dpToPx(container.getContext(), 10);
        isActive = true;
        List<ChatHead> chatHeads = container.getChatHeads();
        int heroIndex = 0;
        this.extras = extras;
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
            currentChatHead.getVerticalSpring().addListener(new SimpleSpringListener() {
                @Override
                public void onSpringAtRest(Spring spring) {
                    super.onSpringAtRest(spring);
                    if (isTransitioning) {
                        isTransitioning = false;
                    }
                    currentChatHead.getVerticalSpring().removeListener(this);
                }
            });
            currentChatHead.getHorizontalSpring().addListener(new SimpleSpringListener() {
                @Override
                public void onSpringAtRest(Spring spring) {
                    super.onSpringAtRest(spring);
                    if (isTransitioning) {
                        isTransitioning = false;
                    }
                    currentChatHead.getHorizontalSpring().removeListener(this);
                }
            });
        }
    }


    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        if (currentChatHead != null) {
            manager.detachView(currentChatHead, getArrowLayout());
        }
        hideView();
        manager.hideOverlayView(true);
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
                boolean handled = manager.onItemSelected(activeChatHead);
                if (!handled) {
                    selectTab(activeChatHead);
                    return true;
                }
            }
            boolean handled = manager.onItemSelected(activeChatHead);
            if (!handled) {
                deactivate();
            }
            return handled;
        }
    }

    private void selectTab(final ChatHead<T> activeChatHead) {
        if (currentChatHead != activeChatHead) {
            detach(currentChatHead);
            currentChatHead = activeChatHead;
        }
        pointTo(activeChatHead);
        showOrHideView(activeChatHead);
    }

    private void detach(ChatHead chatHead) {
        manager.detachView(chatHead, getArrowLayout());
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
            if (xPosition + manager.getConfig().getHeadWidth() > maxWidth && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (xPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
        } else if (spring == activeVerticalSpring && !isDragging) {
            double yPosition = activeVerticalSpring.getCurrentValue();

            if (yPosition + manager.getConfig().getHeadHeight() > maxHeight && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
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
            int[] coords = manager.getChatHeadCoordsForCloseButton(activeChatHead);
            double distanceCloseButtonFromHead = manager.getDistanceCloseButtonFromHead((float) activeHorizontalSpring.getCurrentValue() + manager.getConfig().getHeadWidth() / 2, (float) activeVerticalSpring.getCurrentValue() + manager.getConfig().getHeadHeight() / 2);

            if (distanceCloseButtonFromHead < activeChatHead.CLOSE_ATTRACTION_THRESHOLD && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING && activeVerticalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING && !activeChatHead.isSticky()) {

                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeChatHead.setState(ChatHead.State.CAPTURED);
            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CAPTURING) {
                activeHorizontalSpring.setAtRest();
                activeVerticalSpring.setAtRest();
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CAPTURING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CAPTURING);
                activeHorizontalSpring.setEndValue(coords[0]);
                activeVerticalSpring.setEndValue(coords[1]);

            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED && activeVerticalSpring.isAtRest()) {
                manager.getCloseButton().disappear(false, true);
                manager.captureChatHeads(activeChatHead);
            }
            if (!activeVerticalSpring.isAtRest() && !isTransitioning) {
                manager.getCloseButton().appear();
            } else {
                manager.getCloseButton().disappear(true, true);
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
            arrowLayout = manager.getArrowLayout();
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

    public static void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent && parent.indexOfChild(child) != 0) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    private void pointTo(ChatHead<T> activeChatHead) {
        UpArrowLayout arrowLayout = getArrowLayout();
        getArrowLayout().removeAllViews();
        manager.attachView(activeChatHead, arrowLayout);
        sendViewToBack(manager.getOverlayView());
        Point point = positions.get(activeChatHead);
        if (point != null) {
            int padding = manager.getConfig().getHeadVerticalSpacing(maxWidth, maxHeight);
            arrowLayout.pointTo(point.x + manager.getConfig().getHeadWidth() / 2, point.y + manager.getConfig().getHeadHeight() + padding);
        }
    }


    @Override
    public void onChatHeadAdded(final ChatHead chatHead, final boolean animated) {
        //we post so that chat head measurement is done
        Spring spring = chatHead.getHorizontalSpring();
        spring.setCurrentValue(maxWidth).setAtRest();
        spring = chatHead.getVerticalSpring();
        spring.setCurrentValue(topPadding).setAtRest();
        onActivate(manager, getBundleWithHero(), maxWidth, maxHeight, animated);

    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        manager.detachView(removed, getArrowLayout());
        manager.removeView(removed, getArrowLayout());
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
            onActivate(manager, getBundleWithHero(), maxWidth, maxHeight, true);
        } else {
            deactivate();
        }

    }


    @Override
    public void onCapture(ChatHeadManager container, ChatHead activeChatHead) {
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
        for (ChatHead head : manager.getChatHeads()) {
            if (nextBestChatHead == null) {
                nextBestChatHead = head;
            } else if (head.getUnreadCount() >= nextBestChatHead.getUnreadCount()) {
                nextBestChatHead = head;
            }
        }
        return nextBestChatHead;
    }

    private Bundle getBundleWithHero() {
        Bundle bundle = extras;
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(MinimizedArrangement.BUNDLE_HERO_INDEX_KEY, getHeroIndex());
        return bundle;
    }

    private void deactivate() {
        manager.setArrangement(MinimizedArrangement.class, getBundleWithHero());
        hideView();
    }

    /**
     * @return the index of the selected chat head a.k.a the hero
     */
    @Override
    public Integer getHeroIndex() {
        int heroIndex = 0;
        List<ChatHead<T>> chatHeads = manager.getChatHeads();
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
        for (ChatHead<T> chatHead : manager.getChatHeads()) {
            //we dont remove sticky chat heads as well as the currently selected chat head
            if (!chatHead.isSticky() && chatHead != currentChatHead) {
                manager.removeChatHead(chatHead.getKey(), false);
                break;
            }
        }
    }


    @Override
    public void bringToFront(final ChatHead chatHead) {
        //nothing to do, everything is in front.
        selectChatHead(chatHead);
    }

    @Override
    public void onReloadFragment(ChatHead chatHead) {
        if (currentChatHead != null && chatHead == currentChatHead) {
            manager.attachView(chatHead, getArrowLayout());
        }
    }

    @Override
    public boolean shouldShowCloseButton(ChatHead chatHead) {
        if (chatHead.isSticky()) return false;
        return true;
    }

}