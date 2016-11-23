package com.flipkart.chatheads.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.rebound.SpringSystem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by kiran.kumar on 27/10/16.
 */

public interface ChatHeadManager<T extends Serializable> {
    ChatHeadListener getListener();

    void setListener(ChatHeadListener listener);

    List<ChatHead<T>> getChatHeads();

    ChatHeadViewAdapter getViewAdapter();

    void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter);

    ChatHeadCloseButton getCloseButton();

    Class<? extends ChatHeadArrangement> getArrangementType();

    ChatHeadArrangement getActiveArrangement();

    /**
     * Selects the chat head. Very similar to performing touch up on it.
     *
     * @param chatHead
     */
    void selectChatHead(ChatHead chatHead);

    void selectChatHead(T key);

    /**
     * Should be called when measuring of the container is done.
     * Typically called from onMeasure or onLayout
     * Only when {@link ChatHeadContainer#getContainerHeight()} && {@link ChatHeadContainer#getContainerWidth()} returns a positive value will arrangements start working
     *
     * @param height
     * @param width
     */
    void onMeasure(int height, int width);

    /**
     * Adds and returns the created chat head
     *
     * @param isSticky If sticky is true, then this chat head will never be auto removed when size exceeds.
     *                 Sticky chat heads can never be removed
     * @return
     */
    ChatHead<T> addChatHead(T key, boolean isSticky, boolean animated);

    ChatHead<T> findChatHeadByKey(T key);

    void reloadDrawable(T key);

    /**
     * @param userTriggered if true this means that the chat head was removed by user action (drag to bottom)
     */
    void removeAllChatHeads(boolean userTriggered);

    /**
     * Removed the chat head and calls the onChatHeadRemoved listener
     *
     * @param key
     * @param userTriggered if true this means that the chat head was removed by user action (drag to bottom)
     * @return
     */
    boolean removeChatHead(T key, boolean userTriggered);

    ChatHeadOverlayView getOverlayView();

    void captureChatHeads(ChatHead causingChatHead);

    ChatHeadArrangement getArrangement(Class<? extends ChatHeadArrangement> arrangementType);

    void setArrangement(Class<? extends ChatHeadArrangement> arrangement, Bundle extras);

    void setArrangement(Class<? extends ChatHeadArrangement> arrangement, Bundle extras, boolean animated);

    void setOnItemSelectedListener(OnItemSelectedListener<T> onItemSelectedListener);

    boolean onItemSelected(ChatHead<T> chatHead);

    void onItemRollOver(ChatHead<T> chatHead);

    void onItemRollOut(ChatHead<T> chatHead);

    void onCloseButtonAppear();

    void onCloseButtonDisappear();

    void recreateView(T key);

    SpringSystem getSpringSystem();

    View attachView(ChatHead<T> activeChatHead, ViewGroup parent);

    void detachView(ChatHead<T> chatHead, ViewGroup parent);

    void removeView(ChatHead<T> chatHead, ViewGroup parent);

    ChatHeadConfig getConfig();

    void setConfig(ChatHeadConfig config);

    double getDistanceCloseButtonFromHead(float rawX, float rawY);

    void hideOverlayView(boolean animated);

    void showOverlayView(boolean animated);

    int[] getChatHeadCoordsForCloseButton(ChatHead chatHead);

    void bringToFront(ChatHead chatHead);

    UpArrowLayout getArrowLayout();

    ChatHeadContainer getChatHeadContainer();

    DisplayMetrics getDisplayMetrics();

    int getMaxWidth();

    int getMaxHeight();

    Context getContext();

    Parcelable onSaveInstanceState(Parcelable superState);

    void onRestoreInstanceState(Parcelable state);

    void onSizeChanged(int w, int h, int oldw, int oldh);


    interface OnItemSelectedListener<T> {
        /**
         * Will be called whenever a chat head is clicked.
         * If you return false from here, the arrangement will continue whatever its supposed to do.
         * If you return true from here, the arrangement will stop the action it normally does after click.
         *
         * @param key
         * @param chatHead
         * @return true if you want to take control. false if you dont care.
         */
        boolean onChatHeadSelected(T key, ChatHead chatHead);

        void onChatHeadRollOver(T key, ChatHead chatHead);

        void onChatHeadRollOut(T key, ChatHead chatHead);
    }
}
