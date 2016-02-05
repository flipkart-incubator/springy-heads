/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 */

package com.flipkart.chatheads.reboundextensions;

import android.support.v4.util.ArrayMap;
import android.view.ViewGroup;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * SpringChain is a helper class for creating spring animations with multiple springs in a chain.
 * Chains of springs can be used to create cascading animations that maintain individual physics
 * state for each member of the chain. One spring in the chain is chosen to be the control spring.
 * Springs before and after the control spring in the chain are pulled along by their predecessor.
 * You can change which spring is the control spring at any point by calling
 */
public class ChatHeadSpringChain implements SpringListener {

    /**
     * Add these spring configs to the registry to support live tuning through the
     * {@link com.facebook.rebound.ui.SpringConfiguratorView}
     */
    private static final SpringConfigRegistry registry = SpringConfigRegistry.getInstance();
    private static int DEFAULT_MAIN_TENSION = 40;
    private static int DEFAULT_MAIN_FRICTION = 6;
    private static int DEFAULT_ATTACHMENT_TENSION = 190;
    private static int DEFAULT_ATTACHMENT_FRICTION = 20;
    private static int id = 0;
    private final SpringSystem mSpringSystem = SpringSystem.create();
    private final List<SpringData> mSprings = new ArrayList<SpringData>();
    private final Map<ChatHead, SpringData> mKeyMapping = new ArrayMap<ChatHead, SpringData>();
    private final Map<Spring, SpringData> mSpringMapping = new ArrayMap<Spring, SpringData>();
    // The main spring config defines the tension and friction for the control spring. Keeping these
    // values separate allows the behavior of the trailing springs to be different than that of the
    // control point.
    private final SpringConfig mMainSpringConfig;
    // The attachment spring config defines the tension and friction for the rest of the springs in
    // the chain.
    private final SpringConfig mAttachmentSpringConfig;
    private final ChatHeadContainer mContainer;
    private double delta;
    private int mControlSpringIndex = -1;

    private ChatHeadSpringChain(ChatHeadContainer container) {
        mContainer = container;
        mMainSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(DEFAULT_MAIN_TENSION, DEFAULT_MAIN_FRICTION);
        mAttachmentSpringConfig =
                SpringConfig.fromOrigamiTensionAndFriction(DEFAULT_ATTACHMENT_TENSION, DEFAULT_ATTACHMENT_FRICTION);
        registry.addSpringConfig(mMainSpringConfig, "main spring " + id++);
        registry.addSpringConfig(mAttachmentSpringConfig, "attachment spring " + id++);
    }


    /**
     * Factory method for creating a new SpringChain with default SpringConfig.
     *
     * @return the newly created SpringChain
     */
    public static ChatHeadSpringChain create(ChatHeadContainer container) {
        return new ChatHeadSpringChain(container);
    }

    public SpringSystem getSpringSystem() {
        return mSpringSystem;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public SpringConfig getMainSpringConfig() {
        return mMainSpringConfig;
    }

    public SpringConfig getAttachmentSpringConfig() {
        return mAttachmentSpringConfig;
    }


    /**
     * Add a spring to the chain that will callback to the provided listener.
     *
     * @param listener the listener to notify for this Spring in the chain
     * @return this SpringChain for chaining
     */
    public SpringData addSpring(final ChatHeadContainer container, final ChatHead chatHead, final SpringListener listener, boolean isSticky) {
        // We listen to each spring added to the SpringChain and dynamically chain the springs together
        // whenever the control spring state is modified.
        Spring spring = mSpringSystem
                .createSpring()
                .addListener(this)
                .setSpringConfig(mAttachmentSpringConfig);
        //spring.setRestDisplacementThreshold(5);
        int nextIndex = mSprings.size();
        SpringData data = new SpringData(nextIndex, chatHead, spring, listener, isSticky);
        mSprings.add(data);
        mKeyMapping.put(chatHead, data);
        mSpringMapping.put(spring, data);
        if (getControlSpring() != null)
            spring.setCurrentValue(getControlSpring().getCurrentValue());
        moveControlSpringToEnd();
        if (getControlSpring() != null)
            getControlSpring().setEndValue(getControlSpring().getCurrentValue());
        return data;
    }

    public Spring removeSpring(final ChatHead key) {
        SpringData data = mKeyMapping.get(key);
        if (data != null) {
            if(data.getKey().getParent()!=null)
            {
                ((ViewGroup)data.getKey().getParent()).removeView(data.getKey());
            }
            int i = mSprings.indexOf(data);
            mSprings.remove(data);
            mKeyMapping.remove(key);
            mSpringMapping.remove(data.getSpring());
            Spring controlSpring = getControlSpring();
            SpringData springData = mSpringMapping.get(controlSpring);

            Collections.sort(mSprings, new Comparator<SpringData>() {
                @Override
                public int compare(SpringData lhs, SpringData rhs) {
                    return lhs.getIndex() - rhs.getIndex();
                }
            });
            for (SpringData mSpring : mSprings) {
                mSpring.setIndex(mSprings.indexOf(mSpring));
            }
            if(i == mControlSpringIndex) {
                mControlSpringIndex = mSprings.size()-1;
            }
            else
            {
                mControlSpringIndex = mSprings.indexOf(springData);
            }

            moveControlSpringToEnd();
            return data.getSpring();
        }

        return null;
    }

    public void activateFollowControlSpring() {
        Spring controlSpring = getControlSpring();
        if (controlSpring != null) {
            onSpringUpdate(controlSpring);
        }
    }

    /**
     * Useful to move the control spring to end
     */
    public void moveControlSpringToEnd() {
        if (mControlSpringIndex < 0 || mControlSpringIndex >= mSprings.size()) {
            return;
        }
        SpringData data = mSprings.get(mControlSpringIndex);

        if (data != null) {
            mSprings.remove(mControlSpringIndex);
            mSprings.add(data);
            data.getKey().bringToFront();
            mControlSpringIndex = mSprings.size() - 1;
        }
    }

    /**
     * Retrieve the control spring so you can manipulate it to drive the positions of the other
     * springs.
     *
     * @return the control spring.
     */
    public Spring getControlSpring() {
        if (mControlSpringIndex >= 0 && mControlSpringIndex < mSprings.size()) {
            return mSprings.get(mControlSpringIndex).getSpring();
        }
        return null;
    }

    /**
     * Set the control spring. This spring will drive the positions of all the springs
     * before and after it in the list when moved.
     */
    public ChatHeadSpringChain setControlSpring(Object key) {

        SpringData data = mKeyMapping.get(key);
        mControlSpringIndex = mSprings.indexOf(data);
        if (data == null)
            return this;


        for (Spring spring : mSpringSystem.getAllSprings()) {
            spring.setSpringConfig(mAttachmentSpringConfig);
        }
        data.getKey().bringToFront();
        getControlSpring().setSpringConfig(mMainSpringConfig);

        return this;
    }

    /**
     * Retrieve the list of springs in the chain.
     *
     * @return the list of springs
     */
    public List<SpringData> getAllSprings() {
        return mSprings;
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        // Get the control spring index and update the endValue of each spring above and below it in the
        // spring collection triggering a cascading effect.
        SpringData data = mSpringMapping.get(spring);
        if (data == null) return;
        int idx = mSprings.indexOf(data);
        SpringListener listener = data.getListener();
        int above = -1;
        int below = -1;
        if (idx == mControlSpringIndex) {
            below = idx - 1;
            above = idx + 1;
        } else if (idx < mControlSpringIndex) {
            below = idx - 1;
        } else if (idx > mControlSpringIndex) {
            above = idx + 1;
        }
        if (above > -1 && above < mSprings.size()) {
            /** code change for FK app **/
            mSprings.get(above).getSpring().setEndValue(spring.getCurrentValue() + delta);
        }
        if (below > -1 && below < mSprings.size()) {
            /** code change for FK app **/
            mSprings.get(below).getSpring().setEndValue(spring.getCurrentValue() - delta);
        }
        listener.onSpringUpdate(spring);
    }

    @Override
    public void onSpringAtRest(Spring spring) {
        SpringData data = mSpringMapping.get(spring);
        if (data != null)
            data.getListener().onSpringAtRest(spring);
    }

    @Override
    public void onSpringActivate(Spring spring) {
        SpringData data = mSpringMapping.get(spring);
        if (data != null)
            data.getListener().onSpringActivate(spring);
    }

    @Override
    public void onSpringEndStateChange(Spring spring) {
        SpringData data = mSpringMapping.get(spring);
        if (data != null)
            data.getListener().onSpringEndStateChange(spring);
    }

    public SpringData getSpring(Object key) {
        return mKeyMapping.get(key);
    }

    public class SpringData {

        private final boolean mSticky;
        private Spring mSpring;
        private ChatHead mKey;
        private int mIndex;
        private SpringListener mListener;

        public SpringData(int index, ChatHead key, Spring spring, SpringListener listener, boolean isSticky) {
            mKey = key;
            mIndex = index;
            mSpring = spring;
            mListener = listener;
            mSticky = isSticky;
        }

        public boolean isSticky() {
            return mSticky;
        }

        public Spring getSpring() {
            return mSpring;
        }

        public ChatHead getKey() {
            return mKey;
        }

        public SpringListener getListener() {
            return mListener;
        }

        public int getIndex() {
            return mIndex;
        }

        public void setIndex(int index) {
            mIndex = index;
        }

        @Override
        public int hashCode() {
            return mSpring.hashCode();
        }
    }
}
