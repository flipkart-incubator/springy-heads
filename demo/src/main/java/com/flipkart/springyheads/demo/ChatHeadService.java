package com.flipkart.springyheads.demo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeadContainer;
import com.flipkart.chatheads.ChatHeadListener;
import com.flipkart.chatheads.ChatHeadManager;
import com.flipkart.chatheads.ChatHeadViewAdapter;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;
import com.flipkart.chatheads.arrangement.MaximizedArrangement;
import com.flipkart.chatheads.arrangement.MinimizedArrangement;
import com.flipkart.chatheads.container.DefaultChatHeadManager;
import com.flipkart.chatheads.container.WindowManagerContainer;
import com.flipkart.circularImageView.CircularDrawable;
import com.flipkart.circularImageView.TextDrawer;
import com.flipkart.circularImageView.notification.CircularNotificationDrawer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatHeadService extends Service {

    private static final String TAG = "ChatHeadService";
    private final IBinder mBinder = new LocalBinder();
    private DefaultChatHeadManager<String> chatHeadManager;
    private int chatHeadIdentifier = 0;
    private WindowManagerContainer windowManagerContainer;
    private Map<String, View> viewCache = new HashMap<>();


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManagerContainer = new WindowManagerContainer(this);
        chatHeadManager = new DefaultChatHeadManager<String>(this, windowManagerContainer);

        // The view adapter is invoked when someone clicks a chat head.
        chatHeadManager.setViewAdapter(new ChatHeadViewAdapter<String>() {

            @Override
            public View attachView(String key, ChatHead chatHead, ViewGroup parent) {
                // You can return the view which is shown when the arrangement changes to maximized.
                // The passed "key" param is the same key which was used when adding the chat head.

                View cachedView = viewCache.get(key);
                if (cachedView == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.fragment_test, parent, false);
                    TextView identifier = (TextView) view.findViewById(R.id.identifier);
                    identifier.setText(key);
                    cachedView = view;
                    viewCache.put(key, view);
                }
                parent.addView(cachedView);
                return cachedView;
            }

            @Override
            public void detachView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    parent.removeView(cachedView);
                }
            }

            @Override
            public void removeView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    viewCache.remove(key);
                    parent.removeView(cachedView);
                }
            }

            @Override
            public Drawable getChatHeadDrawable(String key) {
                // this is where you return a drawable for the chat head itself based on the key. Typically you return a circular shape
                // you may want to checkout circular image library https://github.com/flipkart-incubator/circular-image
                return ChatHeadService.this.getChatHeadDrawable(key);
            }
        });

        chatHeadManager.setListener(new ChatHeadListener() {
            @Override
            public void onChatHeadAdded(Object key) {
                //called whenever a new chat head with the specified 'key' has been added
                Log.d(TAG, "onChatHeadAdded() called with: key = [" + key + "]");
            }

            @Override
            public void onChatHeadRemoved(Object key, boolean userTriggered) {
                //called whenever a new chat head with the specified 'key' has been removed.
                // userTriggered: 'true' says whether the user removed the chat head, 'false' says that the code triggered it
                Log.d(TAG, "onChatHeadRemoved() called with: key = [" + key + "], userTriggered = [" + userTriggered + "]");
            }

            @Override
            public void onChatHeadArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement) {
                //called whenever the chat head arrangement changed. For e.g minimized to maximized or vice versa.
                Log.d(TAG, "onChatHeadArrangementChanged() called with: oldArrangement = [" + oldArrangement + "], newArrangement = [" + newArrangement + "]");
            }

            @Override
            public void onChatHeadAnimateStart(ChatHead chatHead) {
                //called when the chat head has started moving (
                Log.d(TAG, "onChatHeadAnimateStart() called with: chatHead = [" + chatHead + "]");
            }

            @Override
            public void onChatHeadAnimateEnd(ChatHead chatHead) {
                //called when the chat head has settled after moving
                Log.d(TAG, "onChatHeadAnimateEnd() called with: chatHead = [" + chatHead + "]");
            }
        });

        chatHeadManager.setOnItemSelectedListener(new ChatHeadManager.OnItemSelectedListener<String>() {
            @Override
            public boolean onChatHeadSelected(String key, ChatHead chatHead) {
                if (chatHeadManager.getArrangementType() == MaximizedArrangement.class) {
                    Log.d(TAG, "chat head got selected in maximized arrangement");
                }
                return false; //returning true will mean that you have handled the behaviour and the default behaviour will be skipped
            }

            @Override
            public void onChatHeadRollOver(String key, ChatHead chatHead) {
                Log.d(TAG, "onChatHeadRollOver() called with: key = [" + key + "], chatHead = [" + chatHead + "]");
            }

            @Override
            public void onChatHeadRollOut(String key, ChatHead chatHead) {
                Log.d(TAG, "onChatHeadRollOut() called with: key = [" + key + "], chatHead = [" + chatHead + "]");
            }
        });
        addChatHead();
        addChatHead();
        addChatHead();
        addChatHead();
        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        moveToForeground();

    }

    private Drawable getChatHeadDrawable(String key) {
        Random rnd = new Random();
        int randomColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        CircularDrawable circularDrawable = new CircularDrawable();
        circularDrawable.setBitmapOrTextOrIcon(new TextDrawer().setText("C" + key).setBackgroundColor(randomColor));
        int badgeCount = (int) (Math.random() * 10f);
        circularDrawable.setNotificationDrawer(new CircularNotificationDrawer().setNotificationText(String.valueOf(badgeCount)).setNotificationAngle(135).setNotificationColor(Color.WHITE, Color.RED));
        circularDrawable.setBorder(Color.WHITE, 3);
        return circularDrawable;

    }

    private void moveToForeground() {
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentTitle("Springy heads")
                .setContentText("Click to configure.")
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, FloatingActivity.class), 0))
                .build();

        startForeground(1, notification);
    }

    public void addChatHead() {
        chatHeadIdentifier++;
        // you can even pass a custom object instead of "head0"
        // a sticky chat head (passed as 'true') cannot be closed and will remain when all other chat heads are closed.
        /**
         * In this example a String object (identified by chatHeadIdentifier) is attached to each chat head.
         * You can instead attach any custom object, for e.g a Conversation object to denote each chat head.
         * This object will represent a chat head uniquely and will be passed back in all callbacks.
         */
        chatHeadManager.addChatHead(String.valueOf(chatHeadIdentifier), false, true);
        chatHeadManager.bringToFront(chatHeadManager.findChatHeadByKey(String.valueOf(chatHeadIdentifier)));
    }

    public void removeChatHead() {
        chatHeadManager.removeChatHead(String.valueOf(chatHeadIdentifier), true);
        chatHeadIdentifier--;
    }

    public void removeAllChatHeads() {
        chatHeadIdentifier = 0;
        chatHeadManager.removeAllChatHeads(true);
    }

    public void toggleArrangement() {
        if (chatHeadManager.getActiveArrangement() instanceof MinimizedArrangement) {
            chatHeadManager.setArrangement(MaximizedArrangement.class, null);
        } else {
            chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        }
    }

    public void updateBadgeCount() {
        chatHeadManager.reloadDrawable(String.valueOf(chatHeadIdentifier));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManagerContainer.destroy();
    }

    public void minimize() {
        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ChatHeadService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ChatHeadService.this;
        }
    }
}