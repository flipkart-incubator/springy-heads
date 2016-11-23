package com.flipkart.chatheads.ui;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

/**
 * An adapter to provide views. Inspired by {@link android.support.v4.view.PagerAdapter}
 */
public interface ChatHeadViewAdapter<T> {

    /**
     * Based on the key, this should instantiate and return a View. This view will be shown once {@link MaximizedArrangement} is activated.. Make sure you have added the view into the specified parent ViewGroup.
     * Cache the view so that you can either detach it or remove it later.
     */
    View attachView(T key, ChatHead<? extends Serializable> chatHead, ViewGroup parent);

    /**
     * This will be called when the view has to be temporarily detached. {@link #attachView(Object, ChatHead, ViewGroup)} will be called if view has to be reattached.
     * You would typically remove the view from parent here, but wont reclaim resources yet.
     * If a chat head is removed, this method will be called followed by {@link #removeView(Object, ChatHead, ViewGroup)}
     */
    void detachView(T key, ChatHead<? extends Serializable> chatHead, ViewGroup parent);


    /**
     * This will be called when a chat head has been removed forever. In this callback you can reclaim any resources you have allocated for this chat head.
     * Also make sure you remove the view you returned from {@link #attachView(Object, ChatHead, ViewGroup)} from the specified parent.
     */
    void removeView(T key, ChatHead<? extends Serializable> chatHead, ViewGroup parent);

    /**
     * Should return the view used to represent a chat "head". Typically a rounded imageview. Use {@link ChatHeadManager#reloadDrawable(Serializable)} if you want to reload.
     */
    Drawable getChatHeadDrawable(T key);

}
