package com.flipkart.chatheads.ui;

import java.io.Serializable;

/**
 * Created by kiran.kumar on 06/05/15.
 */
public interface ChatHeadListener<T> {
    void onChatHeadAdded(T key);
    void onChatHeadRemoved(T key, boolean userTriggered);
    void onChatHeadArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement);
    <T extends Serializable> void onChatHeadAnimateEnd(ChatHead<T> chatHead);
    <T extends Serializable> void onChatHeadAnimateStart(ChatHead chatHead);
}
