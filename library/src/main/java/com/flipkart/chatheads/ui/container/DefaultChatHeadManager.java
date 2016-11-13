package com.flipkart.chatheads.ui.container;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringSystem;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadArrangement;
import com.flipkart.chatheads.ui.ChatHeadCloseButton;
import com.flipkart.chatheads.ui.ChatHeadConfig;
import com.flipkart.chatheads.ui.ChatHeadDefaultConfig;
import com.flipkart.chatheads.ui.ChatHeadListener;
import com.flipkart.chatheads.ui.ChatHeadManager;
import com.flipkart.chatheads.ui.ChatHeadOverlayView;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.CircularArrangement;
import com.flipkart.chatheads.ui.MaximizedArrangement;
import com.flipkart.chatheads.ui.MinimizedArrangement;
import com.flipkart.chatheads.ui.SpringConfigsHolder;
import com.flipkart.chatheads.ui.UpArrowLayout;
import com.flipkart.chatheads.ui.ChatHeadContainer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DefaultChatHeadManager<T extends Serializable> implements ChatHeadCloseButton.CloseButtonListener, ChatHeadManager<T> {

    private static final int OVERLAY_TRANSITION_DURATION = 200;
    private final Map<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangements = new HashMap<>(3);
    private final Context context;
    private final ChatHeadContainer chatHeadContainer;
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
    private UpArrowLayout arrowLayout;

    public DefaultChatHeadManager(Context context, ChatHeadContainer chatHeadContainer) {
        this.context = context;
        this.chatHeadContainer = chatHeadContainer;
        this.displayMetrics = chatHeadContainer.getDisplayMetrics();
        init(context, new ChatHeadDefaultConfig(context));
    }

    public ChatHeadContainer getChatHeadContainer() {
        return chatHeadContainer;
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }


    @Override
    public ChatHeadListener getListener() {
        return listener;
    }

    @Override
    public void setListener(ChatHeadListener listener) {
        this.listener = listener;
    }

    @Override
    public List<ChatHead<T>> getChatHeads() {
        return chatHeads;
    }

    @Override
    public ChatHeadViewAdapter getViewAdapter() {
        return viewAdapter;
    }

    @Override
    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {
        this.viewAdapter = chatHeadViewAdapter;
    }

    @Override
    public ChatHeadCloseButton getCloseButton() {
        return closeButton;
    }

    @Override
    public int getMaxWidth() {
        return maxWidth;
    }

    @Override
    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Class<? extends ChatHeadArrangement> getArrangementType() {
        if (activeArrangement != null) {
            return activeArrangement.getClass();
        } else if (requestedArrangement != null) {
            return requestedArrangement.getArrangement();
        }
        return null;
    }

    @Override
    public ChatHeadArrangement getActiveArrangement() {
        if (activeArrangement != null) {
            return activeArrangement;
        }
        return null;
    }

    @Override
    public void selectChatHead(ChatHead chatHead) {
        if (activeArrangement != null)
            activeArrangement.selectChatHead(chatHead);
    }

    @Override
    public void selectChatHead(T key) {
        ChatHead chatHead = findChatHeadByKey(key);
        if (chatHead != null) {
            selectChatHead(chatHead);
        }
    }


    @Override
    public void onMeasure(int height, int width) {
        boolean needsLayout = false;
        if (height != maxHeight && width != maxWidth) {
            needsLayout = true; // both changed, must be screen rotation.
        }
        maxHeight = height;
        maxWidth = width;

        int closeButtonCenterX = (int) ((float) width * 0.5f);
        int closeButtonCenterY = (int) ((float) height * 0.9f);

        closeButton.onParentHeightRefreshed();
        closeButton.setCenter(closeButtonCenterX, closeButtonCenterY);

        if (maxHeight > 0 && maxWidth > 0) {
            if (requestedArrangement != null) {
                setArrangementImpl(requestedArrangement);
                requestedArrangement = null;
            } else {
                if (needsLayout) {
                    // this means height changed and we need to redraw.
                    setArrangementImpl(new ArrangementChangeRequest(activeArrangement.getClass(), null, false));

                }
            }
        }

    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (activeArrangement != null) {
//            activeArrangement.handleRawTouchEvent(ev);
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    @Override
    public ChatHead<T> addChatHead(T key, boolean isSticky, boolean animated) {
        ChatHead<T> chatHead = findChatHeadByKey(key);
        if (chatHead == null) {
            chatHead = new ChatHead<T>(this, springSystem, getContext(), isSticky);
            chatHead.setKey(key);
            chatHeads.add(chatHead);
            ViewGroup.LayoutParams layoutParams = chatHeadContainer.createLayoutParams(getConfig().getHeadWidth(), getConfig().getHeadHeight(), Gravity.START | Gravity.TOP, 0);

            chatHeadContainer.addView(chatHead, layoutParams);
            if (chatHeads.size() > config.getMaxChatHeads(maxWidth, maxHeight) && activeArrangement != null) {
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

    @Override
    public ChatHead<T> findChatHeadByKey(T key) {
        for (ChatHead<T> chatHead : chatHeads) {
            if (chatHead.getKey().equals(key))
                return chatHead;
        }

        return null;
    }

    @Override
    public void reloadDrawable(T key) {
        Drawable chatHeadDrawable = viewAdapter.getChatHeadDrawable(key);
        if (chatHeadDrawable != null) {
            findChatHeadByKey(key).setImageDrawable(viewAdapter.getChatHeadDrawable(key));
        }
    }

    @Override
    public void removeAllChatHeads(boolean userTriggered) {
        for (Iterator<ChatHead<T>> iterator = chatHeads.iterator(); iterator.hasNext(); ) {
            ChatHead<T> chatHead = iterator.next();
            iterator.remove();
            onChatHeadRemoved(chatHead, userTriggered);
        }
    }

    @Override
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
            chatHeadContainer.removeView(chatHead);
            if (activeArrangement != null)
                activeArrangement.onChatHeadRemoved(chatHead);
            if (listener != null) {
                listener.onChatHeadRemoved(chatHead.getKey(), userTriggered);
            }
        }
    }

    @Override
    public ChatHeadOverlayView getOverlayView() {
        return overlayView;
    }

    private void init(Context context, ChatHeadConfig chatHeadDefaultConfig) {
        chatHeadContainer.onInitialized(this);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        this.displayMetrics = metrics;
        this.config = chatHeadDefaultConfig; //TODO : needs cleanup
        chatHeads = new ArrayList<>(5);
        arrowLayout = new UpArrowLayout(context);
        arrowLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        chatHeadContainer.addView(arrowLayout, arrowLayout.getLayoutParams());
        arrowLayout.setVisibility(View.GONE);
        springSystem = SpringSystem.create();
        closeButton = new ChatHeadCloseButton(context, this, maxHeight, maxWidth);
        ViewGroup.LayoutParams layoutParams = chatHeadContainer.createLayoutParams(chatHeadDefaultConfig.getCloseButtonHeight(), chatHeadDefaultConfig.getCloseButtonWidth(), Gravity.TOP | Gravity.START, 0);
        closeButton.setListener(this);
        chatHeadContainer.addView(closeButton, layoutParams);
        closeButtonShadow = new ImageView(getContext());
        ViewGroup.LayoutParams shadowLayoutParams = chatHeadContainer.createLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM, 0);
        closeButtonShadow.setImageResource(R.drawable.dismiss_shadow);
        closeButtonShadow.setVisibility(View.GONE);
        chatHeadContainer.addView(closeButtonShadow, shadowLayoutParams);
        arrangements.put(MinimizedArrangement.class, new MinimizedArrangement(this));
        arrangements.put(MaximizedArrangement.class, new MaximizedArrangement<T>(this));
        arrangements.put(CircularArrangement.class, new CircularArrangement(this));
        setupOverlay(context);
        setConfig(chatHeadDefaultConfig);
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.DRAGGING, "dragging mode");
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.NOT_DRAGGING, "not dragging mode");
    }

    private void setupOverlay(Context context) {
        overlayView = new ChatHeadOverlayView(context);
        overlayView.setBackgroundResource(R.drawable.overlay_transition);
        ViewGroup.LayoutParams layoutParams = getChatHeadContainer().createLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY, 0);
        getChatHeadContainer().addView(overlayView, layoutParams);
    }

    public double getDistanceCloseButtonFromHead(float touchX, float touchY) {
        if (closeButton.isDisappeared()) {
            return Double.MAX_VALUE;
        } else {
            int left = closeButton.getLeft();
            int top = closeButton.getTop();
            double xDiff = touchX - left -getChatHeadContainer().getViewX(closeButton) - closeButton.getMeasuredWidth() / 2;
            double yDiff = touchY - top - getChatHeadContainer().getViewY(closeButton) - closeButton.getMeasuredHeight() / 2;
            double distance = Math.hypot(xDiff, yDiff);
            return distance;
        }
    }

    @Override
    public UpArrowLayout getArrowLayout() {
        return arrowLayout;
    }

    @Override
    public void captureChatHeads(ChatHead causingChatHead) {
        activeArrangement.onCapture(this, causingChatHead);
    }


    @Override
    public ChatHeadArrangement getArrangement(Class<? extends ChatHeadArrangement> arrangementType) {
        return arrangements.get(arrangementType);
    }

    @Override
    public void setArrangement(final Class<? extends ChatHeadArrangement> arrangement, Bundle extras) {
        setArrangement(arrangement, extras, true);
    }

    @Override
    public void setArrangement(final Class<? extends ChatHeadArrangement> arrangement, Bundle extras, boolean animated) {
        this.requestedArrangement = new ArrangementChangeRequest(arrangement, extras, animated);
        chatHeadContainer.requestLayout();
    }

    /**
     * Should only be called after onMeasure
     *
     * @param requestedArrangementParam
     */
    private void setArrangementImpl(ArrangementChangeRequest requestedArrangementParam) {
        boolean hasChanged = false;
        ChatHeadArrangement requestedArrangement = arrangements.get(requestedArrangementParam.getArrangement());
        ChatHeadArrangement oldArrangement = null;
        ChatHeadArrangement newArrangement = requestedArrangement;
        Bundle extras = requestedArrangementParam.getExtras();
        if(activeArrangement!=requestedArrangement) hasChanged = true;
        if (extras == null) extras = new Bundle();

        if (activeArrangement != null) {
            extras.putAll(activeArrangement.getRetainBundle());
            activeArrangement.onDeactivate(maxWidth, maxHeight);
            oldArrangement = activeArrangement;
        }
        activeArrangement = requestedArrangement;
        activeArrangementBundle = extras;
        requestedArrangement.onActivate(this, extras, maxWidth, maxHeight, requestedArrangementParam.isAnimated());
        if(hasChanged) {
            chatHeadContainer.onArrangementChanged(oldArrangement, newArrangement);
            if (listener != null)
                listener.onChatHeadArrangementChanged(oldArrangement, newArrangement);
        }

    }

    @Override
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

    @Override
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

    @Override
    public int[] getChatHeadCoordsForCloseButton(ChatHead chatHead) {
        int[] coords = new int[2];
        int x = (int) (closeButton.getLeft() + closeButton.getEndValueX() + closeButton.getMeasuredWidth() / 2 - chatHead.getMeasuredWidth() / 2);
        int y = (int) (closeButton.getTop() + closeButton.getEndValueY() + closeButton.getMeasuredHeight() / 2 - chatHead.getMeasuredHeight() / 2);
        coords[0] = x;
        coords[1] = y;
        return coords;
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener<T> onItemSelectedListener) {
        this.itemSelectedListener = onItemSelectedListener;
    }

    @Override
    public boolean onItemSelected(ChatHead<T> chatHead) {
        return itemSelectedListener != null && itemSelectedListener.onChatHeadSelected(chatHead.getKey(), chatHead);
    }

    @Override
    public void onItemRollOver(ChatHead<T> chatHead) {
        if (itemSelectedListener != null)
            itemSelectedListener.onChatHeadRollOver(chatHead.getKey(), chatHead);
    }

    @Override
    public void onItemRollOut(ChatHead<T> chatHead) {
        if (itemSelectedListener != null)
            itemSelectedListener.onChatHeadRollOut(chatHead.getKey(), chatHead);
    }

    @Override
    public void bringToFront(ChatHead chatHead) {
        if (activeArrangement != null) {
            activeArrangement.bringToFront(chatHead);
        }
    }

    @Override
    public void onCloseButtonAppear() {
        if (!getConfig().isCloseButtonHidden()) {
            closeButtonShadow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCloseButtonDisappear() {
        closeButtonShadow.setVisibility(View.GONE);
    }


    @Override
    public void recreateView(T key) {
        removeView(findChatHeadByKey(key));
        if (activeArrangement != null) {
            activeArrangement.onReloadFragment(findChatHeadByKey(key));
        }
    }

    @Override
    public SpringSystem getSpringSystem() {
        return springSystem;
    }

    @Override
    public View addView(ChatHead<T> activeChatHead, ViewGroup parent) {
        View view = viewAdapter.createView(activeChatHead.getKey(), activeChatHead, parent);
        parent.addView(view);
        return view;
    }

    @Override
    public View removeView(ChatHead chatHead) {
        return null;
    }

    @Override
    public View detachView(ChatHead chatHead) {
        return null;
    }


    @Override
    public ChatHeadConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(ChatHeadConfig config) {
        this.config = config;
        if (closeButton != null) {
//            LayoutParams params = (LayoutParams) closeButton.getLayoutParams();
//            params.width = config.getCloseButtonWidth();
//            params.height = config.getCloseButtonHeight();
//            params.bottomMargin = config.getCloseButtonBottomMargin();
//            closeButton.setLayoutParams(params);
            if (config.isCloseButtonHidden()) {
                closeButton.setVisibility(View.GONE);
                closeButtonShadow.setVisibility(View.GONE);
            } else {
                closeButton.setVisibility(View.VISIBLE);
                closeButtonShadow.setVisibility(View.VISIBLE);
            }
        }
        for (Map.Entry<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangementEntry : arrangements.entrySet()) {
            arrangementEntry.getValue().onConfigChanged(config);
        }

    }

    @Override
    public Parcelable onSaveInstanceState(Parcelable superState) {
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
    public void onRestoreInstanceState(Parcelable state) {
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
            //view.onRestoreInstanceState(savedState.getSuperState());
        } else {
            //view.onRestoreInstanceState(state);
        }

    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (closeButton != null) {
            closeButton.onParentHeightRefreshed();
        }
    }


    static class SavedState extends View.BaseSavedState {
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
