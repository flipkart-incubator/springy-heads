package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ChatHeadContainer<T extends Serializable> extends FrameLayout implements ChatHeadCloseButton.CloseButtonListener {

    private static final int OVERLAY_TRANSITION_DURATION = 200;
    private final Map<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangements = new HashMap<>(3);
    private List<ChatHead<T>> chatHeads;
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
    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private ChatHeadConfig config;
    private ChatHeadListener listener;
    private Bundle activeArrangementBundle;
    private ArrangementChangeRequest requestedArrangement;
    private DisplayMetrics displayMetrics;

    public ChatHeadContainer(Context context) {
        super(context);
        init(context, new ChatHeadDefaultConfig(context));
    }

    public ChatHeadContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, new ChatHeadDefaultConfig(context));
    }

    public ChatHeadContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, new ChatHeadDefaultConfig(context));
    }

    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    public ChatHeadListener getListener() {
        return listener;
    }

    public void setListener(ChatHeadListener listener) {
        this.listener = listener;
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
        if (activeArrangement != null) {
            return activeArrangement.getClass();
        } else if (requestedArrangement != null) {
            return requestedArrangement.getArrangement();
        }
        return null;
    }

    public ChatHeadArrangement getActiveArrangement() {
        if (activeArrangement != null) {
            return activeArrangement;
        }
        return null;
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
        if (chatHead != null) {
            selectChatHead(chatHead);
        }
    }

    /**
     * Returns the fragment for the key if its already present. If createIfRequired is set to true, it will create and return it.
     *
     * @param key
     * @param createIfRequired
     * @return
     */
    public Fragment getFragment(T key, boolean createIfRequired) {
        return getFragment(findChatHeadByKey(key), createIfRequired);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        maxWidth = getMeasuredWidth();
        maxHeight = getMeasuredHeight();
        if (requestedArrangement != null) setArrangementImpl(requestedArrangement);
        requestedArrangement = null;

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (activeArrangement != null) {
            activeArrangement.handleRawTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Adds and returns the created chat head
     *
     * @param isSticky If sticky is true, then this chat head will never be auto removed when size exceeds.
     *                 Sticky chat heads can never be removed
     * @return
     */
    public ChatHead<T> addChatHead(T key, boolean isSticky, boolean animated) {
        ChatHead<T> chatHead = findChatHeadByKey(key);
        if (chatHead == null) {
            chatHead = new ChatHead<T>(this, springSystem, getContext(), isSticky);
            chatHead.setKey(key);
            chatHeads.add(chatHead);
            addView(chatHead);
            if (chatHeads.size() > config.getMaxChatHeads(maxWidth, maxHeight) && activeArrangement!=null) {
                activeArrangement.removeOldestChatHead();
            }
            reloadDrawable(key);
            if (activeArrangement != null)
                activeArrangement.onChatHeadAdded(chatHead, animated);
            else {
                chatHead.getHorizontalSpring().setCurrentValue(-100);
                chatHead.getVerticalSpring().setCurrentValue(-100);
            }
            if (listener != null) {
                listener.onChatHeadAdded(key);
            }
            closeButtonShadow.bringToFront();
        }
        return chatHead;
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

    /**
     * @param userTriggered if true this means that the chat head was removed by user action (drag to bottom)
     */
    public void removeAllChatHeads(boolean userTriggered) {
        for (Iterator<ChatHead<T>> iterator = chatHeads.iterator(); iterator.hasNext(); ) {
            ChatHead<T> chatHead = iterator.next();
            iterator.remove();
            onChatHeadRemoved(chatHead, userTriggered);
        }
    }

    /**
     * Removed the chat head and calls the onChatHeadRemoved listener
     *
     * @param key
     * @param userTriggered if true this means that the chat head was removed by user action (drag to bottom)
     * @return
     */
    public boolean removeChatHead(T key, boolean userTriggered) {
        ChatHead chatHead = findChatHeadByKey(key);
        if (chatHead != null) {
            chatHeads.remove(chatHead);
            onChatHeadRemoved(chatHead, userTriggered);
            return true;
        }
        return false;
    }

    private void onChatHeadRemoved(ChatHead chatHead, boolean userTriggered) {
        if (chatHead != null && chatHead.getParent() != null) {
            chatHead.onRemove();
            removeView(chatHead);
            if (activeArrangement != null)
                activeArrangement.onChatHeadRemoved(chatHead);
            if (listener != null) {
                listener.onChatHeadRemoved(chatHead.getKey(), userTriggered);
            }
        }
    }

    public ChatHeadOverlayView getOverlayView() {
        return overlayView;
    }

    private void init(Context context, ChatHeadConfig chatHeadDefaultConfig) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        this.displayMetrics = metrics;
        setConfig(chatHeadDefaultConfig);
        chatHeads = new ArrayList<>(5);
        LayoutInflater.from(context).inflate(R.layout.arrow_layout, this, true);
        UpArrowLayout arrowLayout = (UpArrowLayout) findViewById(R.id.arrow_layout);
        arrowLayout.setVisibility(View.GONE);
        springSystem = SpringSystem.create();
        closeButton = new ChatHeadCloseButton(getContext(), this);
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
        if (closeButton.isDisappeared()) {
            return Double.MAX_VALUE;
        } else {
            int left = closeButton.getLeft();
            int top = closeButton.getTop();
            double xDiff = touchX - left - closeButton.getMeasuredWidth() / 2;
            double yDiff = touchY - top - closeButton.getTranslationY() - closeButton.getMeasuredHeight() / 2;
            double distance = Math.hypot(xDiff, yDiff);
            return distance;
        }
    }

    void captureChatHeads(ChatHead causingChatHead) {
        activeArrangement.onCapture(this, causingChatHead);
    }


    public ChatHeadArrangement getArrangement(Class<? extends ChatHeadArrangement> arrangementType) {
        return arrangements.get(arrangementType);
    }

    public void setArrangement(final Class<? extends ChatHeadArrangement> arrangement, Bundle extras) {
        setArrangement(arrangement, extras, true);
    }

    public void setArrangement(final Class<? extends ChatHeadArrangement> arrangement, Bundle extras, boolean animated) {
        this.requestedArrangement = new ArrangementChangeRequest(arrangement, extras, animated);
        requestLayout();
    }

    /**
     * Should only be called after onMeasure
     *
     * @param requestedArrangement
     */
    private void setArrangementImpl(ArrangementChangeRequest requestedArrangement) {
        ChatHeadArrangement chatHeadArrangement = arrangements.get(requestedArrangement.getArrangement());
        ChatHeadArrangement oldArrangement = null;
        ChatHeadArrangement newArrangement = chatHeadArrangement;
        Bundle extras = requestedArrangement.getExtras();
        if (extras == null) extras = new Bundle();

        if (activeArrangement != null && chatHeadArrangement != activeArrangement) {
            extras.putAll(activeArrangement.getRetainBundle());
            activeArrangement.onDeactivate(maxWidth, maxHeight);
            oldArrangement = activeArrangement;
        }
        activeArrangement = chatHeadArrangement;
        activeArrangementBundle = extras;
        chatHeadArrangement.onActivate(this, extras, maxWidth, maxHeight, requestedArrangement.isAnimated());
        if (listener != null) listener.onChatHeadArrangementChanged(oldArrangement, newArrangement);
    }

    public void hideOverlayView(boolean animated) {
        if (overlayVisible) {
            TransitionDrawable drawable = (TransitionDrawable) overlayView.getBackground();
            int duration = OVERLAY_TRANSITION_DURATION;
            if (!animated) duration = 0;
            drawable.reverseTransition(duration);
            overlayView.setClickable(false);
            overlayVisible = false;
        }
    }

    public void showOverlayView(boolean animated) {
        if (!overlayVisible) {
            TransitionDrawable drawable = (TransitionDrawable) overlayView.getBackground();
            int duration = OVERLAY_TRANSITION_DURATION;
            if (!animated) duration = 0;
            drawable.startTransition(duration);
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

    public void onItemRollOver(ChatHead<T> chatHead) {
        if (itemSelectedListener != null)
            itemSelectedListener.onChatHeadRollOver(chatHead.getKey(), chatHead);
    }

    public void onItemRollOut(ChatHead<T> chatHead) {
        if (itemSelectedListener != null)
            itemSelectedListener.onChatHeadRollOut(chatHead.getKey(), chatHead);
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

    public void reloadFragment(T key) {
        removeFragment(findChatHeadByKey(key));
        if (activeArrangement != null) {
            activeArrangement.onReloadFragment(findChatHeadByKey(key));
        }
    }

    public SpringSystem getSpringSystem() {
        return springSystem;
    }

    Fragment addFragment(ChatHead<T> activeChatHead, ViewGroup parent) {
        try {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            Fragment fragment = getFragmentManager().findFragmentByTag(activeChatHead.getKey().toString());

            if (fragment == null) {
                fragment = getViewAdapter().instantiateFragment(activeChatHead.getKey(), activeChatHead);
                transaction.add(parent.getId(), fragment, activeChatHead.getKey().toString());
            } else {
                if (fragment.isDetached()) {
                    transaction.attach(fragment);
                }
            }
            if (fragment != currentFragment && currentFragment != null) {
                transaction.detach(currentFragment);
            }
            currentFragment = fragment;
            transaction.commitAllowingStateLoss();
            manager.executePendingTransactions();
            return fragment;
        } catch (IllegalStateException ex) {
            //raised when activity has been destroyed
            ex.printStackTrace();
        }
        return null;
    }

    Fragment removeFragment(ChatHead chatHead) {
        try {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            Fragment fragment = getFragment(chatHead, false);
            if (fragment == null) {
                //we dont have it in our cache. So we create it and add it
            } else {
                //we have added it already sometime earlier. So re-attach it.
                transaction.remove(fragment);

            }
            if (fragment == currentFragment) {
                currentFragment = null;
            }
            transaction.commitAllowingStateLoss();
            manager.executePendingTransactions();
            return fragment;
        } catch (IllegalStateException ex) {
            //raised when activity has been destroyed
            ex.printStackTrace();
        }
        return null;
    }

    Fragment detachFragment(ChatHead chatHead) {
        try {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = getFragment(chatHead, false);
            if (fragment != null) {

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (!fragment.isDetached()) {
                    fragmentTransaction.detach(fragment);
                }
                fragmentTransaction.commitAllowingStateLoss();
            }
            return fragment;
        } catch (IllegalStateException ex) {
            //raised when activity has been destroyed
            ex.printStackTrace();
        }
        return null;
    }


    Fragment getFragment(ChatHead<T> activeChatHead, boolean createIfRequired) {
        String tag = "";
        if (activeChatHead != null) {
            tag = activeChatHead.getKey().toString();
        }
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        if (fragment == null && createIfRequired) {
            fragment = getViewAdapter().instantiateFragment(activeChatHead.getKey(), activeChatHead);
        }
        return fragment;
    }


    public FragmentManager getFragmentManager() {
        if (fragmentManager == null) {
            if (getViewAdapter() == null)
                throw new IllegalStateException(ChatHeadViewAdapter.class.getSimpleName() + " should not be null");
            fragmentManager = getViewAdapter().getFragmentManager();
            if (fragmentManager == null)
                throw new IllegalStateException(FragmentManager.class.getSimpleName() + " returned from " + ChatHeadViewAdapter.class.getSimpleName() + " should not be null");
        }
        return fragmentManager;
    }

    public ChatHeadConfig getConfig() {
        return config;
    }

    public void setConfig(ChatHeadConfig config) {
        this.config = config;
        if (closeButton != null) {
            FrameLayout.LayoutParams params = (LayoutParams) closeButton.getLayoutParams();
            params.width = config.getCloseButtonWidth();
            params.height = config.getCloseButtonHeight();
            params.bottomMargin = config.getCloseButtonBottomMargin();
            closeButton.setLayoutParams(params);
        }
        for (Map.Entry<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangementEntry : arrangements.entrySet()) {
            arrangementEntry.getValue().onConfigChanged(config);
        }

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        if (activeArrangement != null) {
            savedState.setActiveArrangement(activeArrangement.getClass());
            savedState.setActiveArrangementBundle(activeArrangement.getRetainBundle());
        }
        LinkedHashMap<T, Boolean> chatHeadState = new LinkedHashMap<>();
        for (ChatHead<T> chatHead : chatHeads) {
            T key = chatHead.getKey();
            boolean sticky = chatHead.isSticky();
            chatHeadState.put(key, sticky);
        }
        savedState.setChatHeads(chatHeadState);
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            final Class activeArrangementClass = savedState.getActiveArrangement();
            final Bundle activeArrangementBundle = savedState.getActiveArrangementBundle();
            final Map<? extends Serializable, Boolean> chatHeads = savedState.getChatHeads();
            for (Map.Entry<? extends Serializable, Boolean> entry : chatHeads.entrySet()) {
                T key = (T) entry.getKey();
                Boolean sticky = entry.getValue();
                addChatHead(key, sticky, false);
            }
            if (activeArrangementClass != null) {
                setArrangement(activeArrangementClass, activeArrangementBundle, false);
            }
            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (closeButton != null) {
            closeButton.onParentHeightRefreshed();
        }
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

        void onChatHeadRollOver(T key, ChatHead chatHead);

        void onChatHeadRollOut(T key, ChatHead chatHead);
    }

    static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private Class<? extends ChatHeadArrangement> activeArrangement;
        private Bundle activeArrangementBundle;
        private LinkedHashMap<? extends Serializable, Boolean> chatHeads;

        public SavedState(Parcel source) {
            super(source);
            activeArrangement = (Class<? extends ChatHeadArrangement>) source.readSerializable();
            activeArrangementBundle = source.readBundle();
            chatHeads = (LinkedHashMap<? extends Serializable, Boolean>) source.readSerializable();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public Class<? extends ChatHeadArrangement> getActiveArrangement() {
            return activeArrangement;
        }

        public void setActiveArrangement(Class<? extends ChatHeadArrangement> activeArrangement) {
            this.activeArrangement = activeArrangement;
        }

        public Bundle getActiveArrangementBundle() {
            return activeArrangementBundle;
        }

        public void setActiveArrangementBundle(Bundle activeArrangementBundle) {
            this.activeArrangementBundle = activeArrangementBundle;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeSerializable(activeArrangement);
            dest.writeBundle(activeArrangementBundle);
            dest.writeSerializable(chatHeads);
        }

        public Map<? extends Serializable, Boolean> getChatHeads() {
            return chatHeads;
        }

        public void setChatHeads(LinkedHashMap<? extends Serializable, Boolean> chatHeads) {
            this.chatHeads = chatHeads;
        }
    }

    private class ArrangementChangeRequest {
        private final Bundle extras;
        private final Class<? extends ChatHeadArrangement> arrangement;
        private final boolean animated;

        public ArrangementChangeRequest(Class<? extends ChatHeadArrangement> arrangement, Bundle extras, boolean animated) {
            this.arrangement = arrangement;
            this.extras = extras;
            this.animated = animated;
        }

        public Bundle getExtras() {
            return extras;
        }

        public Class<? extends ChatHeadArrangement> getArrangement() {
            return arrangement;
        }

        public boolean isAnimated() {
            return animated;
        }
    }
}
