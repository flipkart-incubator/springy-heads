package com.flipkart.springyheads.demo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.FrameChatHeadContainer;
import com.flipkart.chatheads.ui.MinimizedArrangement;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.container.DefaultChatHeadManager;
import com.flipkart.chatheads.ui.container.WindowManagerContainer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

public class ChatHeadService extends Service {

    private DefaultChatHeadManager<String> chatContainer;


    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        ChatHeadContainer chatHeadContainer = new WindowManagerContainer(this);

        chatContainer = new DefaultChatHeadManager<String>(this, chatHeadContainer);

        chatContainer.setViewAdapter(new ChatHeadViewAdapter() {
            @Override
            public FragmentManager getFragmentManager() {
                return null;
            }

            @Override
            public Fragment instantiateFragment(Object key, ChatHead chatHead) {
                return TestFragment.newInstance(0);
            }

            @Override
            public Drawable getChatHeadDrawable(Object key) {
                return getResources().getDrawable(R.drawable.head);
            }

            @Override
            public Drawable getPointerDrawable() {
                return getResources().getDrawable(R.drawable.circular_ring);
            }

            @Override
            public View getTitleView(Object key, ChatHead chatHead) {
                return null;
            }
        });


        addChatHead();
        addChatHead();
        addChatHead();
        addChatHead();
        chatContainer.setArrangement(MinimizedArrangement.class, null);
        chatContainer.onMeasure();

        moveToForeground();

    }

    private void moveToForeground() {
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentText("Chat heads is active")
                .build();

        startForeground(1, notification);
    }

    private void addChatHead() {
        chatContainer.addChatHead("head" + Math.random(), false, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}