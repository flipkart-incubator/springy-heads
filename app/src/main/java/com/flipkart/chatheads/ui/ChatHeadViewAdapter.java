package com.flipkart.chatheads.ui;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

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
    public Fragment getFragment(T key, ChatHead<T> chatHead);


    /**
     * Should return the view used to represent a chat "head". Typically a rounded imageview.
     * @param key
     * @return
     */
    public Drawable getChatHeadDrawable(T key);
}
