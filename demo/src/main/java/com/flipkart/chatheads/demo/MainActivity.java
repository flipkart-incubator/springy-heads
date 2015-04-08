package com.flipkart.chatheads.demo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.arrangements.CircularArrangement;
import com.flipkart.chatheads.ui.arrangements.MaximizedArrangement;

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
                return getResources().getDrawable(R.drawable.circular_view);
            }
        });
        chatContainer.setOnItemSelectedListener(new ChatHeadContainer.OnItemSelectedListener() {
            @Override
            public boolean onChatHeadSelected(Object key, ChatHead chatHead) {
                if (chatContainer.getArrangementType() == CircularArrangement.class) {
                    System.out.println("Clicked on " + key + " " +
                            "when arrangement was circular");
                }
                chatContainer.setArrangement(MaximizedArrangement.class, null);
                Fragment fragment = chatContainer.getFragment(key,true);
                fragment.setArguments(new Bundle());
                System.out.println("fragment = " + fragment);
                return true;
            }
        });
        chatContainer.addChatHead("head0", false);
        chatContainer.addChatHead("head1", false);
        Button addButton = (Button) findViewById(R.id.add);
        Button removeButton = (Button) findViewById(R.id.remove);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatContainer.addChatHead("head" + Math.random(), false);
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<Object> keys = chatContainer.getChatHeads().keySet();
                Object[] objects = keys.toArray();
                if (objects.length > 0) {
                    Object object = objects[objects.length - 1];
                    chatContainer.removeChatHead(object);
                }
            }
        });


        circularClickArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(CircularArrangement.BUNDLE_KEY_X, (int) event.getX());
                    bundle.putInt(CircularArrangement.BUNDLE_KEY_Y, (int) event.getY());
                    chatContainer.setArrangement(CircularArrangement.class, bundle);
                }
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();


    }


}
