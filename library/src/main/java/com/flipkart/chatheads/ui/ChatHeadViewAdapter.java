package com.flipkart.chatheads.ui;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;

/**
 * Created by kirankumar on 16/02/15.
 */
public interface ChatHeadViewAdapter<T> {

    /**
     * Based on the key, this should instantiate and return a View. This View will be removed when the chathead is removed.
     */
    public View createView(T key, ChatHead<? extends Serializable> chatHead, ViewGroup parent);

    /**
     * Should return the view used to represent a chat "head". Typically a rounded imageview.
     * @param key
     * @return
     */
    public Drawable getChatHeadDrawable(T key);


}
