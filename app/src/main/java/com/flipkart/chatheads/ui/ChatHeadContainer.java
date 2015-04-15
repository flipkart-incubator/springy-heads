package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringsHolder;
import com.flipkart.chatheads.reboundextensions.ChatHeadSpringChain;
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
public class ChatHeadContainer<T> extends FrameLayout implements ChatHeadCloseButton.CloseButtonListener {

    private static final int MAX_CHAT_HEADS = 5;
    private final Map<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangements = new HashMap<>(3);
    private final List<ChatHead<T>> chatHeads = new ArrayList<>(MAX_CHAT_HEADS);
    private int maxWidth;
    private int maxHeight;
    private ChatHeadCloseButton closeButton;
    private ChatHeadArrangement activeArrangement;
    private ChatHeadViewAdapter<T> viewAdapter;
    private ChatHeadOverlayView overlayView;
    private OnItemSelectedListener<T> itemSelectedListener;
    private boolean overlayVisible;
    private ImageView closeButtonShadow;
    private SpringSystem springSystem;

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

    public List<ChatHead<T>> getChatHeads() {
        return chatHeads;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * Selects the chat head. Very similar to performing touch up on it.
     *
     * @param chatHead
     */
    public void selectChatHead(ChatHead chatHead) {
        if (activeArrangement != null)
            activeArrangement.selectChatHead(chatHead);
    }

    public void selectChatHead(T key) {
        ChatHead chatHead = findChatHeadByKey(key);
        selectChatHead(chatHead);
    }

    /**
     * Returns the fragment for the key if its already present. If createIfRequired is set to true, it will create and return it.
     *
     * @param key
     * @param createIfRequired
     * @return
     */
    public Fragment getFragment(T key, boolean createIfRequired) {
        MaximizedArrangement<T> chatHeadArrangement = (MaximizedArrangement) arrangements.get(MaximizedArrangement.class);
        return chatHeadArrangement.getFragment(findChatHeadByKey(key), createIfRequired);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        maxWidth = getMeasuredWidth();
        maxHeight = getMeasuredHeight();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        activeArrangement.handleRawTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Adds and returns the created chat head
     *
     * @param isSticky If sticky is true, then this chat head will never be auto removed when size exceeds.
     *                 Sticky chat heads can never be removed
     * @return
     */
    public ChatHead<T> addChatHead(T key, boolean isSticky) {
        final ChatHead<T> chatHead = new ChatHead<T>(this, springSystem, getContext(), isSticky);
        chatHead.setKey(key);
        chatHeads.add(chatHead);
        addView(chatHead);
        if (chatHeads.size() > MAX_CHAT_HEADS) {
            removeOldestChatHead();
        }
        reloadDrawable(key);

        if (activeArrangement != null)
            activeArrangement.onChatHeadAdded(chatHead);
        else {
            chatHead.getHorizontalSpring().setCurrentValue(-100);
            chatHead.getVerticalSpring().setCurrentValue(-100);
        }

        closeButtonShadow.bringToFront();
        return chatHead;
    }

    private void removeOldestChatHead() {
        for (ChatHead<T> chatHead : chatHeads) {
            if (!chatHead.isSticky()) {
                removeChatHead(chatHead.getKey());
                break;
            }
        }

    }

    public ChatHead<T> findChatHeadByKey(T key) {
        for (ChatHead<T> chatHead : chatHeads) {
            if (chatHead.getKey().equals(key))
                return chatHead;
        }

        return null;
    }

    public void reloadDrawable(T key) {
        Drawable chatHeadDrawable = viewAdapter.getChatHeadDrawable(key);
        if (chatHeadDrawable != null) {
            findChatHeadByKey(key).setImageDrawable(viewAdapter.getChatHeadDrawable(key));
        }
    }

    public boolean removeChatHead(T key) {
        ChatHead chatHead = findChatHeadByKey(key);
        if (chatHead != null && chatHead.getParent() != null && !chatHead.isSticky()) {
            chatHead.onRemove();
            ChatHead<T> chatHeadByKey = findChatHeadByKey(key);
            chatHeads.remove(chatHeadByKey);
            removeView(chatHead);
            if (activeArrangement != null)
                activeArrangement.onChatHeadRemoved(chatHead);
            return true;
        }
        return false;
    }

    public ChatHeadOverlayView getOverlayView() {
        return overlayView;
    }

    private void init(Context context) {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        LayoutInflater.from(context).inflate(R.layout.arrow_layout, this, true);
        UpArrowLayout arrowLayout = (UpArrowLayout) findViewById(R.id.arrow_layout);
        arrowLayout.setVisibility(View.GONE);
        springSystem = SpringSystem.create();
        closeButton = new ChatHeadCloseButton(getContext());
        closeButton.setListener(this);
        addView(closeButton);
        closeButtonShadow = new ImageView(getContext());
        LayoutParams shadowLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        shadowLayoutParams.gravity = Gravity.BOTTOM;
        closeButtonShadow.setLayoutParams(shadowLayoutParams);
        closeButtonShadow.setImageResource(R.drawable.dismiss_shadow);
        closeButtonShadow.setVisibility(View.GONE);
        addView(closeButtonShadow);
        arrangements.put(MinimizedArrangement.class, new MinimizedArrangement(this));
        arrangements.put(MaximizedArrangement.class, new MaximizedArrangement<T>(this));
        arrangements.put(CircularArrangement.class, new CircularArrangement(this));
        setupOverlay(context);
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.DRAGGING, "dragging mode");
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.NOT_DRAGGING, "not dragging mode");
    }

    private void setupOverlay(Context context) {
        overlayView = new ChatHeadOverlayView(context);
        overlayView.setBackgroundResource(R.drawable.overlay_transition);
        addView(overlayView, 0);
    }

    double getDistanceCloseButtonFromHead(float touchX, float touchY) {
        int left = closeButton.getLeft();
        int top = closeButton.getTop();
        double xDiff = touchX - left - closeButton.getMeasuredWidth() / 2;
        double yDiff = touchY - top - closeButton.getTranslationY() - closeButton.getMeasuredHeight() / 2;
        double distance = Math.hypot(xDiff, yDiff);
        return distance;
    }

    void captureChatHeads(ChatHead causingChatHead) {
        activeArrangement.onCapture(this, causingChatHead);
    }

    public void removeAllChatHeads() {
//        Set<Map.Entry<T, ChatHead<T>>> entries = chatHeads.entrySet();
//        Iterator<Map.Entry<T, ChatHead<T>>> iterator = entries.iterator();
//        List<ChatHead<T>> temp = new ArrayList<>();
//        while (iterator.hasNext()) {
//            Map.Entry<T, ChatHead<T>> next = iterator.next();
//            if (!next.getValue().isSticky()) {
//                temp.add(next.getValue());
//                iterator.remove();
//            }
//        }
//        for (int i = 0; i < temp.size(); i++) {
//            removeChatHead(temp.get(i).getKey());
//        }
    }

    public void setArrangement(final Class<? extends ChatHeadArrangement> arrangement, Bundle extras) {
        ChatHeadArrangement chatHeadArrangement = arrangements.get(arrangement);
        if (activeArrangement != null && chatHeadArrangement != activeArrangement) {
            activeArrangement.onDeactivate(maxWidth, maxHeight);
        }
        activeArrangement = chatHeadArrangement;
        if (extras == null) extras = new Bundle();
        chatHeadArrangement.onActivate(this, extras, maxWidth, maxHeight);

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
        int x = (int) (closeButton.getLeft() + closeButton.getEndValueX() + closeButton.getMeasuredWidth() / 2 - chatHead.getMeasuredWidth() / 2);
        int y = (int) (closeButton.getTop() + closeButton.getEndValueY() + closeButton.getMeasuredHeight() / 2 - chatHead.getMeasuredHeight() / 2);
        coords[0] = x;
        coords[1] = y;
        return coords;
    }

    public void setOnItemSelectedListener(ChatHeadContainer.OnItemSelectedListener<T> onItemSelectedListener) {
        this.itemSelectedListener = onItemSelectedListener;
    }

    public boolean onItemSelected(ChatHead<T> chatHead) {
        return itemSelectedListener != null && itemSelectedListener.onChatHeadSelected(chatHead.getKey(), chatHead);
    }

    public void bringToFront(ChatHead chatHead) {
        if (activeArrangement != null) {
            activeArrangement.bringToFront(chatHead);
        }
    }

    @Override
    public void onCloseButtonAppear() {
        closeButtonShadow.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCloseButtonDisappear() {
        closeButtonShadow.setVisibility(View.GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        activeArrangement.onDraw(canvas);
    }

    public SpringSystem getSpringSystem() {
        return springSystem;
    }

    public interface OnItemSelectedListener<T> {
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
    }
}
