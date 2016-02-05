package com.flipkart.chatheads.ui;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import java.io.Serializable;

/**
 * Created by kirankumar on 16/02/15.
 */
public interface ChatHeadViewAdapter<T> {

    /**
     * Return the fragment manager. This manager will be asked once and used throughout chat heads library.
     */
    public FragmentManager getFragmentManager();

    /**
     * Based on the key, this should instantiate and return a fragment. This fragment will be removed when the chathead is removed.
     */
    public Fragment instantiateFragment(T key, ChatHead<? extends Serializable> chatHead);

    /**
     * Should return the view used to represent a chat "head". Typically a rounded imageview.
     * @param key
     * @return
     */
    public Drawable getChatHeadDrawable(T key);

    /**
     * Used for circular arrangement where a view is drawn under the touch point
     * @return
     */
    public Drawable getPointerDrawable();

    public View getTitleView(T key, ChatHead chatHead);
}
