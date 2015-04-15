package com.flipkart.chatheads.ui.arrangements;

import android.os.Bundle;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringChain;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadArrangement;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.SpringConfigsHolder;

import java.util.List;

public class MinimizedArrangement extends ChatHeadArrangement {

    public static final String BUNDLE_HERO_INDEX_KEY = "hero_index";
    private float DELTA = 0;
    private float currentDelta = 0;
    private int currentX = 0;
    private int currentY = -Integer.MAX_VALUE;
    private int maxWidth;
    private int maxHeight;
    private ChatHeadContainer container;
    private SpringChain horizontalSpringChain;
    private SpringChain verticalSpringChain;
    private ChatHead hero;
    private SpringListener horizontalHeroListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            currentDelta = (float) ((float)DELTA * (maxWidth/2 - spring.getCurrentValue())/(maxWidth/2));
            horizontalSpringChain.getControlSpring().setCurrentValue(spring.getCurrentValue());
            setCurrentX((int) spring.getCurrentValue());
        }
    };
    private SpringListener verticalHeroListener = new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
            verticalSpringChain.getControlSpring().setCurrentValue(spring.getCurrentValue());
            setCurrentY((int) spring.getCurrentValue());
        }
    };
    public MinimizedArrangement(ChatHeadContainer container) {
        DELTA = ChatHeadUtils.dpToPx(container.getContext(),5);
        this.container = container;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }

    @Override
    public void setContainer(ChatHeadContainer container) {
        this.container = container;
    }

    @Override
    public void onActivate(ChatHeadContainer container, Bundle extras, int maxWidth, int maxHeight) {
        if (horizontalSpringChain != null || verticalSpringChain != null) {
            onDeactivate(maxWidth, maxHeight);
        }
        horizontalSpringChain = SpringChain.create();
        verticalSpringChain = SpringChain.create();
        int heroIndex = 0;
        if (extras != null)
            heroIndex = extras.getInt(BUNDLE_HERO_INDEX_KEY, -1);
        List<ChatHead> chatHeads = container.getChatHeads();
        if (heroIndex < 0 || heroIndex > chatHeads.size() - 1) {
            heroIndex = 0;
        }
        if(heroIndex<chatHeads.size()) {
            hero = chatHeads.get(heroIndex);

            for (int i = 0; i < chatHeads.size(); i++) {
                final ChatHead chatHead = chatHeads.get(i);
                if (chatHead != hero) {
                    horizontalSpringChain.addSpring(new SimpleSpringListener() {
                        @Override
                        public void onSpringUpdate(Spring spring) {
                            int index = horizontalSpringChain.getAllSprings().indexOf(spring);
                            int diff = index - horizontalSpringChain.getAllSprings().size() + 1;
                            chatHead.getHorizontalSpring().setCurrentValue(spring.getCurrentValue() + diff * currentDelta);
                        }
                    });
                    Spring currentSpring = horizontalSpringChain.getAllSprings().get(horizontalSpringChain.getAllSprings().size() - 1);
                    currentSpring.setCurrentValue(chatHead.getHorizontalSpring().getCurrentValue());
                    verticalSpringChain.addSpring(new SimpleSpringListener() {
                        @Override
                        public void onSpringUpdate(Spring spring) {
                            chatHead.getVerticalSpring().setCurrentValue(spring.getCurrentValue());
                        }
                    });
                    currentSpring = verticalSpringChain.getAllSprings().get(verticalSpringChain.getAllSprings().size() - 1);
                    currentSpring.setCurrentValue(chatHead.getVerticalSpring().getCurrentValue());
                    chatHead.bringToFront();
                } else {
                    hero = chatHead;
                }


            }
            if (currentY < 0) {
                currentY = (int) (maxHeight * 0.8);
            }
            if (hero != null) {
                hero.bringToFront();
                horizontalSpringChain.addSpring(new SimpleSpringListener() {
                });
                verticalSpringChain.addSpring(new SimpleSpringListener() {
                });
                horizontalSpringChain.setControlSpringIndex(chatHeads.size() - 1);
                verticalSpringChain.setControlSpringIndex(chatHeads.size() - 1);

                hero.getHorizontalSpring().addListener(horizontalHeroListener);
                hero.getVerticalSpring().addListener(verticalHeroListener);

                hero.getHorizontalSpring().setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                hero.getHorizontalSpring().setEndValue(currentX);
                hero.getVerticalSpring().setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                hero.getVerticalSpring().setEndValue(currentY);
            }


            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            container.getCloseButton().setEnabled(false);
        }
//        if(springsHolder.getActiveHorizontalSpring()!=null && springsHolder.getActiveVerticalSpring()!=null) {
//            handleTouchUp(null, 0, 0, springsHolder.getActiveHorizontalSpring(), springsHolder.getActiveVerticalSpring(), true);
//        }
    }

    @Override
    public void onChatHeadAdded(ChatHead chatHead) {
        if(hero!=null) {
            chatHead.getHorizontalSpring().setCurrentValue(hero.getHorizontalSpring().getCurrentValue() - currentDelta);
            chatHead.getVerticalSpring().setCurrentValue(hero.getVerticalSpring().getCurrentValue());
        }

        onActivate(container, null, maxWidth, maxHeight);
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        onActivate(container, null, maxWidth, maxHeight);
    }

    @Override
    public void onCapture(ChatHeadContainer container, ChatHead activeChatHead) {
        // we dont care about the active ones
        //container.removeAllChatHeads();
    }

    @Override
    public void selectChatHead(ChatHead chatHead) {
        //container.toggleArrangement();
    }

    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        if (hero != null) {
            hero.getHorizontalSpring().removeListener(horizontalHeroListener);
            hero.getVerticalSpring().removeListener(verticalHeroListener);
        }
        List<Spring> allSprings = horizontalSpringChain.getAllSprings();
        for (Spring spring : allSprings) {
            spring.destroy();
        }
        allSprings = verticalSpringChain.getAllSprings();
        for (Spring spring : allSprings) {
            spring.destroy();
        }
        horizontalSpringChain = null;
        verticalSpringChain = null;
    }

    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {

        if (Math.abs(xVelocity) < ChatHeadUtils.dpToPx(container.getContext(), 50)) {
            if (activeHorizontalSpring.getCurrentValue() < (maxWidth - activeHorizontalSpring.getCurrentValue())) {
                xVelocity = -1;
            } else {
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
        int heroIndex = 0;
        List<ChatHead> chatHeads = container.getChatHeads();
        int i = 0;
        for (ChatHead chatHead : chatHeads) {
            if (hero == chatHead) {
                heroIndex = i;
            }
            i++;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(MaximizedArrangement.BUNDLE_HERO_INDEX_KEY, heroIndex);
        container.setArrangement(MaximizedArrangement.class, bundle);
    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** This method does a bounds Check **/
        double xVelocity = activeHorizontalSpring.getVelocity();
        double yVelocity = activeVerticalSpring.getVelocity();
        if (!isDragging && Math.abs(totalVelocity) < ChatHeadUtils.dpToPx(container.getContext(), 600)) {
            if (spring == activeHorizontalSpring) {

                double xPosition = activeHorizontalSpring.getCurrentValue();
                if (xPosition + activeChatHead.getMeasuredWidth() > maxWidth && activeHorizontalSpring.getVelocity()>0) {
                    //outside the right bound
                    //System.out.println("outside the right bound !! xPosition = " + xPosition);
                    int newPos = maxWidth - activeChatHead.getMeasuredWidth();
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setEndValue(newPos);
                } else if (xPosition < 0 && activeHorizontalSpring.getVelocity()<0) {
                    //outside the left bound
                    //System.out.println("outside the left bound !! xPosition = " + xPosition);
                    activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeHorizontalSpring.setEndValue(0);

                } else {
                    //within bound


                }
            } else if (spring == activeVerticalSpring) {
                double yPosition = activeVerticalSpring.getCurrentValue();
                if (yPosition + activeChatHead.getMeasuredHeight() > maxHeight && activeVerticalSpring.getVelocity()>0) {
                    //outside the bottom bound
                    //System.out.println("outside the bottom bound !! yPosition = " + yPosition);

                    activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                    activeVerticalSpring.setEndValue(maxHeight - activeChatHead.getMeasuredHeight());
                } else if (yPosition < 0 && activeVerticalSpring.getVelocity()<0) {
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

    @Override
    public void bringToFront(ChatHead chatHead) {
        int index = container.getChatHeads().indexOf(chatHead);
        Bundle b = new Bundle();
        b.putInt(BUNDLE_HERO_INDEX_KEY,index);
        onActivate(container, b, container.getMaxWidth(), container.getMaxHeight());
    }
}
