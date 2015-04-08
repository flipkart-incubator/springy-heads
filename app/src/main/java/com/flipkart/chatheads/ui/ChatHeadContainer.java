package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.flipkart.chatheads.R;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ModifiedSpringChain;
import com.flipkart.chatheads.ui.arrangements.CircularArrangement;
import com.flipkart.chatheads.ui.arrangements.MaximizedArrangement;
import com.flipkart.chatheads.ui.arrangements.MinimizedArrangement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ChatHeadContainer<T> extends FrameLayout {

    private static final int MAX_CHAT_HEADS = 5;
    private int maxWidth;
    private int maxHeight;
    private ChatHeadCloseButton closeButton;
    private ChatHeadSpringsHolder springsHolder;
    private Map<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangements = new HashMap<>(3);
    private ChatHeadArrangement activeArrangement;
    private Map<T, ChatHead> chatHeads = new LinkedHashMap<>();
    private ChatHeadViewAdapter viewAdapter;
    private View overlayView;
    private OnItemSelectedListener<T> itemSelectedListener;
    private boolean overlayVisible;

    public ChatHeadContainer(Context context) {
        super(context);
        init(context);
    }

    public ChatHeadContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatHeadContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ChatHeadViewAdapter getViewAdapter() {
        return viewAdapter;
    }

    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {
        this.viewAdapter = chatHeadViewAdapter;
    }

    public ChatHeadCloseButton getCloseButton() {
        return closeButton;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public Class<? extends ChatHeadArrangement> getArrangementType() {
        return activeArrangement.getClass();
    }

    public ChatHeadArrangement getActiveArrangement() {
        return activeArrangement;
    }

    public Map<T, ChatHead> getChatHeads() {
        return chatHeads;
    }

    void selectSpring(ChatHead chatHead) {
        springsHolder.selectSpring(chatHead);
        chatHead.bringToFront();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Selects the chat head. Very similar to performing touch up on it.
     *
     * @param chatHead
     */
    public void selectChatHead(ChatHead chatHead) {
        activeArrangement.selectChatHead(chatHead);
    }

    public void selectChatHead(T key) {
        ChatHead chatHead = chatHeads.get(key);
        selectChatHead(chatHead);
    }

    public void getFragment(T key) {
        //return maximizedArrangement.getFragment(chatHeads.get(key));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        maxWidth = getMeasuredWidth();
        maxHeight = getMeasuredHeight();

    }

    /**
     * Adds and returns the created chat head
     *
     * @param isSticky If sticky is true, then this chat head will never be auto removed when size exceeds.
     *                 Sticky chat heads can never be removed
     * @return
     */
    public ChatHead<T> addChatHead(T key, boolean isSticky) {
        final ChatHead<T> chatHead = new ChatHead(this, springsHolder, getContext(), isSticky);
        chatHead.setKey(key);
        chatHeads.put(key, chatHead);
        addView(chatHead);
        springsHolder.addChatHead(chatHead, chatHead, isSticky);
        if (springsHolder.getHorizontalSpringChain().getAllSprings().size() > MAX_CHAT_HEADS) {
            ModifiedSpringChain.SpringData oldestSpring = springsHolder.getOldestSpring(springsHolder.getHorizontalSpringChain(), true);
            ChatHead<T> chatHeadToRemove = (ChatHead) oldestSpring.getKey();
            removeChatHead(chatHeadToRemove.getKey());
        }
        reloadDrawable(key);
        springsHolder.selectSpring(chatHead);

        if (activeArrangement != null)
            activeArrangement.onChatHeadAdded(chatHead, springsHolder);

        return chatHead;
    }

    public void reloadDrawable(T key) {
        Drawable chatHeadDrawable = viewAdapter.getChatHeadDrawable(key);
        if (chatHeadDrawable != null) {
            chatHeads.get(key).setImageDrawable(viewAdapter.getChatHeadDrawable(key));
        }
    }

    public boolean removeChatHead(T key) {
        ChatHead chatHead = chatHeads.get(key);
        if (chatHead != null && chatHead.getParent() != null && !chatHead.isSticky()) {
            removeView(chatHead);
            chatHeads.remove(key);
            springsHolder.removeChatHead(chatHead);
            if (activeArrangement != null)
                activeArrangement.onChatHeadRemoved(chatHead, springsHolder);
            return true;
        }
        return false;
    }

    public View getOverlayView() {
        return overlayView;
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.arrow_layout, this, true);
        UpArrowLayout arrowLayout = (UpArrowLayout) findViewById(R.id.arrow_layout);
        arrowLayout.setVisibility(View.GONE);
        springsHolder = new ChatHeadSpringsHolder();
        closeButton = new ChatHeadCloseButton(getContext());
        addView(closeButton);
        arrangements.put(MinimizedArrangement.class, new MinimizedArrangement());
        arrangements.put(MaximizedArrangement.class, new MaximizedArrangement());
        arrangements.put(CircularArrangement.class, new CircularArrangement());
        setupOverlay(context);
        post(new Runnable() {
            @Override
            public void run() {
                setArrangement(MinimizedArrangement.class, null);
            }
        });

    }

    private void setupOverlay(Context context) {
        overlayView = new View(context);
        overlayView.setBackgroundResource(R.drawable.overlay_transition);

        addView(overlayView, 0);
    }

    double getDistanceCloseButtonFromHead(float touchX, float touchY) {
        int left = closeButton.getLeft();
        int top = closeButton.getTop();
        double xDiff = touchX - left - closeButton.getTranslationX() - closeButton.getMeasuredWidth() / 2;
        double yDiff = touchY - top - closeButton.getTranslationY() - closeButton.getMeasuredHeight() / 2;
        double distance = Math.hypot(xDiff, yDiff);
        return distance;
    }

    void captureChatHeads(ChatHead causingChatHead) {
        activeArrangement.onCapture(this, causingChatHead);
    }

    public void removeAllChatHeads() {
        Set<Map.Entry<T, ChatHead>> entries = chatHeads.entrySet();
        Iterator<Map.Entry<T, ChatHead>> iterator = entries.iterator();
        List<ChatHead<T>> temp = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<T, ChatHead> next = iterator.next();
            if (!next.getValue().isSticky()) {
                temp.add(next.getValue());
                iterator.remove();
            }
        }
        for (int i = 0; i < temp.size(); i++) {
            removeChatHead(temp.get(i).getKey());
        }
    }

    public void setArrangement(final Class<? extends ChatHeadArrangement> arrangement, Bundle extras) {
        ChatHeadArrangement chatHeadArrangement = arrangements.get(arrangement);
        if (activeArrangement != null && chatHeadArrangement != activeArrangement) {
            activeArrangement.onDeactivate(maxWidth, maxHeight, springsHolder.getActiveHorizontalSpring(), springsHolder.getActiveVerticalSpring());
        }
        activeArrangement = chatHeadArrangement;
        chatHeadArrangement.onActivate(ChatHeadContainer.this, extras, springsHolder, maxWidth, maxHeight);

    }

    public void hideOverlayView() {
        if (overlayVisible) {
            TransitionDrawable drawable = (TransitionDrawable) overlayView.getBackground();
            drawable.reverseTransition(200);
            overlayView.setClickable(false);
            overlayVisible = false;
        }
    }

    public void showOverlayView() {
        if (!overlayVisible) {
            TransitionDrawable drawable = (TransitionDrawable) overlayView.getBackground();
            drawable.startTransition(200);
            overlayView.setClickable(true);
            overlayVisible = true;
        }

    }

    public int[] getChatHeadCoordsForCloseButton(ChatHead chatHead) {
        int[] coords = new int[2];
        int x = (int) (closeButton.getLeft() + closeButton.getTranslationX() + closeButton.getMeasuredWidth() / 2 - chatHead.getMeasuredWidth() / 2);
        int y = (int) (closeButton.getTop() + closeButton.getTranslationY() + closeButton.getMeasuredHeight() / 2 - chatHead.getMeasuredHeight() / 2);
        coords[0] = x;
        coords[1] = y;
        return coords;
    }

    public void setOnItemSelectedListener(ChatHeadContainer.OnItemSelectedListener<T> onItemSelectedListener) {
        this.itemSelectedListener = onItemSelectedListener;
    }

    public boolean onItemSelected(ChatHead<T> chatHead) {
        if (itemSelectedListener != null) {
            return itemSelectedListener.onChatHeadSelected(chatHead.getKey(), chatHead);
        }
        return false;
    }


    public static interface OnItemSelectedListener<T> {
        /**
         * Will be called whenever a chat head is clicked.
         * If you return false from here, the arrangement will continue whatever its supposed to do.
         * If you return true from here, the arrangement will stop the action it normally does after click.
         *
         * @param key
         * @param chatHead
         * @return true if you want to take control. false if you dont care.
         */
        public boolean onChatHeadSelected(T key, ChatHead chatHead);
    }
}
