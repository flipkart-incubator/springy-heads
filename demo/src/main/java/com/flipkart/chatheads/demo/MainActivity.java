package com.flipkart.chatheads.demo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.facebook.rebound.ui.SpringConfiguratorView;
import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.arrangements.CircularArrangement;
import com.flipkart.chatheads.ui.arrangements.MaximizedArrangement;
import com.flipkart.chatheads.ui.arrangements.MinimizedArrangement;

import java.util.Set;


public class MainActivity extends ActionBarActivity {


    private View circularClickArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circularClickArea = findViewById(R.id.circular_click_area);
        final ChatHeadContainer chatContainer = (ChatHeadContainer) findViewById(R.id.chat_container);
        chatContainer.setViewAdapter(new ChatHeadViewAdapter() {
            @Override
            public FragmentManager getFragmentManager() {
                return getSupportFragmentManager();
            }

            @Override
            public Fragment getFragment(Object key, ChatHead chatHead) {
                return TestFragment.newInstance();
            }

            @Override
            public Drawable getChatHeadDrawable(Object key) {
                if (key.equals("main")) {
                    return getResources().getDrawable(R.drawable.circular_view_main);
                } else
                    return getResources().getDrawable(R.drawable.circular_view);
            }

            @Override
            public Drawable getPointerDrawable() {
                return getResources().getDrawable(R.drawable.circular_ring);
            }
        });
        chatContainer.setOnItemSelectedListener(new ChatHeadContainer.OnItemSelectedListener() {
            @Override
            public boolean onChatHeadSelected(Object key, ChatHead chatHead) {
                if (chatContainer.getArrangementType() == CircularArrangement.class) {
                    System.out.println("Clicked on " + key + " " +
                            "when arrangement was circular");
                }
//                chatContainer.setArrangement(MaximizedArrangement.class, null);
//                Fragment fragment = chatContainer.getFragment(key,true);
//                //fragment.setArguments(new Bundle());
//                System.out.println("fragment = " + fragment);
                return false;
            }
        });
        chatContainer.addChatHead("head0", false);
        chatContainer.addChatHead("main", true);
        chatContainer.post(new Runnable() {
            @Override
            public void run() {
                chatContainer.setArrangement(MinimizedArrangement.class, null);
            }
        });
        Button addButton = (Button) findViewById(R.id.add);
        Button removeButton = (Button) findViewById(R.id.remove);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatHead chatHead = chatContainer.addChatHead("head" + Math.random(), false);
                chatContainer.bringToFront(chatHead);
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Set<Object> keys = chatContainer.getChatHeads().keySet();
//                Object[] objects = keys.toArray();
//                if (objects.length > 0) {
//                    Object object = objects[objects.length - 1];
//                    chatContainer.removeChatHead(object);
//                }
            }
        });


        circularClickArea.setOnTouchListener(new View.OnTouchListener() {

            Bundle bundle = new Bundle();
            Runnable longPressCallback = new Runnable() {
                @Override
                public void run() {
                    chatContainer.setArrangement(CircularArrangement.class, bundle);
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                chatContainer.dispatchTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    circularClickArea.removeCallbacks(longPressCallback);
                    bundle.putInt(CircularArrangement.BUNDLE_KEY_X, (int) event.getX());
                    bundle.putInt(CircularArrangement.BUNDLE_KEY_Y, (int) event.getY());
                    circularClickArea.postDelayed(longPressCallback, 1000);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    circularClickArea.removeCallbacks(longPressCallback);
                }
                return true;
            }
        });
        SpringConfiguratorView configuratorView = new SpringConfiguratorView(this);
        chatContainer.addView(configuratorView, 0);

    }


}
