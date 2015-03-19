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

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

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
public class ModifiedSpringChain implements SpringListener {

    /**
     * Add these spring configs to the registry to support live tuning through the
     * {@link com.facebook.rebound.ui.SpringConfiguratorView}
     */
    private static final SpringConfigRegistry registry = SpringConfigRegistry.getInstance();
    private static final int DEFAULT_MAIN_TENSION = 40;
    private static final int DEFAULT_MAIN_FRICTION = 6;
    private static final int DEFAULT_ATTACHMENT_TENSION = 190;
    private static final int DEFAULT_ATTACHMENT_FRICTION = 20;
    private static int id = 0;
    private final SpringSystem mSpringSystem = SpringSystem.create();
    private final List<SpringData> mSprings = new ArrayList<SpringData>();
    private final Map<Object, SpringData> mKeyMapping = new ArrayMap<Object, SpringData>();
    private final Map<Spring, SpringData> mSpringMapping = new ArrayMap<Spring, SpringData>();
    // The main spring config defines the tension and friction for the control spring. Keeping these
    // values separate allows the behavior of the trailing springs to be different than that of the
    // control point.
    private final SpringConfig mMainSpringConfig;
    // The attachment spring config defines the tension and friction for the rest of the springs in
    // the chain.
    private final SpringConfig mAttachmentSpringConfig;
    private double delta;
    private int mControlSpringIndex = -1;
    private ModifiedSpringChain() {
        this(
                DEFAULT_MAIN_TENSION,
                DEFAULT_MAIN_FRICTION,
                DEFAULT_ATTACHMENT_TENSION,
                DEFAULT_ATTACHMENT_FRICTION);
    }

    private ModifiedSpringChain(
            int mainTension,
            int mainFriction,
            int attachmentTension,
            int attachmentFriction) {
        mMainSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(mainTension, mainFriction);
        mAttachmentSpringConfig =
                SpringConfig.fromOrigamiTensionAndFriction(attachmentTension, attachmentFriction);
        registry.addSpringConfig(mMainSpringConfig, "main spring " + id++);
        registry.addSpringConfig(mAttachmentSpringConfig, "attachment spring " + id++);
    }

    /**
     * Factory method for creating a new SpringChain with default SpringConfig.
     *
     * @return the newly created SpringChain
     */
    public static ModifiedSpringChain create() {
        return new ModifiedSpringChain();
    }

    /**
     * Factory method for creating a new SpringChain with the provided SpringConfig.
     *
     * @param mainTension        tension for the main spring
     * @param mainFriction       friction for the main spring
     * @param attachmentTension  tension for the attachment spring
     * @param attachmentFriction friction for the attachment spring
     * @return the newly created SpringChain
     */
    public static ModifiedSpringChain create(
            int mainTension,
            int mainFriction,
            int attachmentTension,
            int attachmentFriction) {
        return new ModifiedSpringChain(mainTension, mainFriction, attachmentTension, attachmentFriction);
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
    public Spring addSpring(final Object key, final SpringListener listener) {
        // We listen to each spring added to the SpringChain and dynamically chain the springs together
        // whenever the control spring state is modified.
        Spring spring = mSpringSystem
                .createSpring()
                .addListener(this)
                .setSpringConfig(mAttachmentSpringConfig);
        //spring.setRestDisplacementThreshold(5);
        int nextIndex = mSprings.size();
        SpringData data = new SpringData(nextIndex, key, spring, listener);
        mSprings.add(data);
        mKeyMapping.put(key, data);
        mSpringMapping.put(spring, data);
        return spring;
    }

    public Spring removeSpring(final Object key) {
        SpringData data = mKeyMapping.get(key);
        if (data != null) {
            int i = mSprings.indexOf(data);
            mSprings.remove(data);
            mKeyMapping.remove(key);
            mSpringMapping.remove(data.getSpring());
            if (i == mControlSpringIndex) {
                mControlSpringIndex = mSprings.size() - 1;
            }
            Collections.sort(mSprings, new Comparator<SpringData>() {
                @Override
                public int compare(SpringData lhs, SpringData rhs) {
                    return lhs.getIndex() - rhs.getIndex();
                }
            });
            int index = 0;
            for (SpringData mSpring : mSprings) {
                mSpring.setIndex(index);
                index++;
            }

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
        SpringData data = mSprings.get(mControlSpringIndex);
        if (data != null) {
            mSprings.remove(mControlSpringIndex);
            mSprings.add(data);
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
    public ModifiedSpringChain setControlSpring(Object key) {

        SpringData data = mKeyMapping.get(key);
        mControlSpringIndex = mSprings.indexOf(data);
        if (data == null)
            return this;


        for (Spring spring : mSpringSystem.getAllSprings()) {
            spring.setSpringConfig(mAttachmentSpringConfig);
        }
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

    public class SpringData {

        private Spring mSpring;
        private Object mKey;
        private int mIndex;
        private SpringListener mListener;
        public SpringData(int index, Object key, Spring spring, SpringListener listener) {
            mKey = key;
            mIndex = index;
            mSpring = spring;
            mListener = listener;
        }

        public Spring getSpring() {
            return mSpring;
        }

        public Object getKey() {
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
