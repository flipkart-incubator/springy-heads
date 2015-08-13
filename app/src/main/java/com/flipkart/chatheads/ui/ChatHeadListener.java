package com.flipkart.chatheads.ui;

/**
 * Created by kiran.kumar on 06/05/15.
 */
public interface ChatHeadListener<T> {
    void onChatHeadAdded(T key);
    void onChatHeadRemoved(T key, boolean userTriggered);
    void onChatHeadArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement);
}
