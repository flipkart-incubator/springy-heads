package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.View;

import com.facebook.rebound.Spring;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;

import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MaximizedArrangement<T> extends ChatHeadArrangement {
    public static final String BUNDLE_HERO_INDEX_KEY = "hero_index";
    private final Map<ChatHead, Point> positions = new ArrayMap<>();
    private ChatHeadContainer<T> container;
    private int maxWidth;
    private int maxHeight;
    private ChatHead currentChatHead = null;
    private UpArrowLayout arrowLayout;
    private int maxDistanceFromOriginal;
    private int topPadding;


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
        if (extras != null)
            heroIndex = extras.getInt(BUNDLE_HERO_INDEX_KEY, -1);
        if (heroIndex < 0 || heroIndex > chatHeads.size() - 1) {
            heroIndex = 0;
        }
        if(chatHeads.size()>0) {
            currentChatHead = chatHeads.get(heroIndex);
            maxDistanceFromOriginal = ChatHeadUtils.dpToPx(container.getContext(), 10);

            int spacing = container.getConfig().getHeadHorizontalSpacing();
            int widthPerHead = container.getConfig().getHeadWidth();
            topPadding = ChatHeadUtils.dpToPx(container.getContext(), 5);
            int leftIndent = maxWidth - (chatHeads.size() * (widthPerHead + spacing));
            for (int i = 0; i < chatHeads.size(); i++) {
                ChatHead chatHead = chatHeads.get(i);
                Spring horizontalSpring = chatHead.getHorizontalSpring();
                int xPos = leftIndent + (i * (widthPerHead + spacing));//align right
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
            selectChatHead(currentChatHead);
        }
    }

    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        container.detachFragment(currentChatHead);
        hideView();
        container.hideOverlayView();
    }

    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {


        if (activeChatHead.getState() == ChatHead.State.FREE) {
            activeHorizontalSpring.setVelocity(xVelocity);
            activeVerticalSpring.setVelocity(yVelocity);
        }


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

            if (currentChatHead == activeChatHead) {
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

        if (!isDragging) {

            /** Capturing check **/


            int[] coords = container.getChatHeadCoordsForCloseButton(activeChatHead);
            double distanceCloseButtonFromHead = container.getDistanceCloseButtonFromHead((float) activeHorizontalSpring.getCurrentValue() + activeChatHead.getMeasuredWidth() / 2, (float) activeVerticalSpring.getCurrentValue() + activeChatHead.getMeasuredHeight() / 2);

            if (distanceCloseButtonFromHead < activeChatHead.CLOSE_ATTRACTION_THRESHOLD && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING && activeVerticalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING) {

                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeChatHead.setState(ChatHead.State.CAPTURED);
            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED) {
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
        container.addFragment(activeChatHead, arrowLayout);
        Point point = positions.get(activeChatHead);
        if (point != null) {
            int padding = container.getConfig().getHeadVerticalSpacing();
            arrowLayout.pointTo(point.x + activeChatHead.getMeasuredWidth() / 2, point.y + activeChatHead.getMeasuredHeight() + padding);
        }
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
        container.removeFragment(removed);
        if (currentChatHead == removed) {
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
        for (ChatHead head : container.getChatHeads()) {
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
            if (currentChatHead == chatHead) {
                heroIndex = i;
            }
            i++;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(MinimizedArrangement.BUNDLE_HERO_INDEX_KEY, heroIndex);
        container.setArrangement(MinimizedArrangement.class, bundle);
    }


    @Override
    public void bringToFront(ChatHead chatHead) {
        //nothing to do, everything is in front.
    }

    @Override
    public void onReloadFragment(ChatHead chatHead) {
        if (chatHead == currentChatHead) {
            container.addFragment(chatHead, getArrowLayout());
        }
    }

    @Override
    public boolean shouldShowCloseButton(ChatHead chatHead) {
        if (chatHead.isSticky()) return false;
        return true;
    }
}