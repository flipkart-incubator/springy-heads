package com.flipkart.chatheads.demo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadArrangement;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.ChatHeadListener;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.CircularArrangement;
import com.flipkart.chatheads.ui.MaximizedArrangement;
import com.flipkart.chatheads.ui.MinimizedArrangement;

import java.util.List;
import java.util.Random;


public class MainActivity extends ActionBarActivity {


    private View circularClickArea;
    private SharedPreferences chatHeadPreferences;
    private ChatHeadContainer chatContainer;
    private TextView chatHeadLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circularClickArea = findViewById(R.id.circular_click_area);
        chatHeadPreferences = getSharedPreferences("chat", MODE_PRIVATE);
        chatHeadLabel = (TextView) findViewById(R.id.chat_head_label);
        chatContainer = (ChatHeadContainer) findViewById(R.id.chat_container);
        chatContainer.setViewAdapter(new ChatHeadViewAdapter() {
            @Override
            public FragmentManager getFragmentManager() {
                return getSupportFragmentManager();
            }

            @Override
            public Fragment instantiateFragment(Object key, ChatHead chatHead) {
                return TestFragment.newInstance(key);
            }

            @Override
            public Drawable getChatHeadDrawable(Object key) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                return new ColorDrawable(color);
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
        chatContainer.setOnItemSelectedListener(new ChatHeadContainer.OnItemSelectedListener() {
            @Override
            public boolean onChatHeadSelected(Object key, ChatHead chatHead) {
                if (chatContainer.getArrangementType() == CircularArrangement.class) {
                    System.out.println("Clicked on " + key + " " +
                            "when arrangement was circular");
                }
//                chatContainer.setArrangement(MaximizedArrangement.class, null);
//                Fragment fragment = chatContainer.instantiateFragment(key,true);
//                //fragment.setArguments(new Bundle());
//                System.out.println("fragment = " + fragment);
                return false;
            }

            @Override
            public void onChatHeadRollOver(Object key, final ChatHead chatHead) {
                System.out.println("MainActivity.onChatHeadRollOver " + key + " : " + chatHead);
                chatHeadLabel.setTranslationX(chatHead.getTranslationX() + chatHead.getMeasuredWidth() / 2 - chatHeadLabel.getMeasuredWidth() / 2);
                float yStart = chatHead.getTranslationY() + chatHead.getMeasuredHeight() / 2 - chatHeadLabel.getMeasuredHeight();
                float yEnd = chatHead.getTranslationY() - chatHeadLabel.getMeasuredHeight();
                ObjectAnimator objectAnimatorTranslationY = ObjectAnimator.ofFloat(chatHeadLabel, "translationY", yStart, yEnd);
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Float animatedValue = (Float) animation.getAnimatedValue();
                        chatHeadLabel.setScaleX(animatedValue);
                        chatHeadLabel.setScaleY(animatedValue);
                    }
                });
                valueAnimator.setDuration(500);
                valueAnimator.setInterpolator(new OvershootInterpolator());
                valueAnimator.start();
                objectAnimatorTranslationY.setDuration(500);
                objectAnimatorTranslationY.setInterpolator(new OvershootInterpolator());
                objectAnimatorTranslationY.start();
                chatHeadLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChatHeadRollOut(Object key, ChatHead chatHead) {
                System.out.println("MainActivity.onChatHeadRollOut " + key + " : " + chatHead);
                chatHeadLabel.setVisibility(View.INVISIBLE);
            }
        });
        chatContainer.setListener(new ChatHeadListener() {
            @Override
            public void onChatHeadAdded(Object key) {
                System.out.println("MainActivity.onChatHeadAdded " + key);
            }

            @Override
            public void onChatHeadRemoved(Object key, boolean userTriggered) {
                System.out.println("MainActivity.onChatHeadRemoved " + key);
            }

            @Override
            public void onChatHeadArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement) {
                System.out.println("MainActivity.onChatHeadArrangementChanged from " + oldArrangement + " to " + newArrangement);
            }

            @Override
            public void onChatHeadAnimateStart(ChatHead chatHead) {

            }

            @Override
            public void onChatHeadAnimateEnd(ChatHead chatHead) {

            }
        });
        chatContainer.setConfig(new CustomChatHeadConfig(this, getInitialX(), getInitialY()));
        if (savedInstanceState == null) {
            //chatContainer.addChatHead("head0", false, true);
            //ChatHead chatHead = chatContainer.addChatHead("main", true, true);
            //chatContainer.bringToFront(chatHead);
            chatContainer.setArrangement(MinimizedArrangement.class, null);

        }
        Button addButton = (Button) findViewById(R.id.add);
        Button bringFrontButton = (Button) findViewById(R.id.bring_front);
        Button toggleButton = (Button) findViewById(R.id.arrangement_toggle);
        Button reloadFragmentButton = (Button) findViewById(R.id.reload_fragment);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatHead chatHead = chatContainer.addChatHead("head" + Math.random(), false, true);
            }
        });
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chatContainer.getArrangementType() == MinimizedArrangement.class) {
                    chatContainer.setArrangement(MaximizedArrangement.class, new Bundle());
                } else {
                    chatContainer.setArrangement(MinimizedArrangement.class, new Bundle());
                }
            }
        });
        reloadFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Class arrangementType = chatContainer.getArrangementType();
                if(arrangementType == MaximizedArrangement.class) {
                    ChatHeadArrangement activeArrangement = chatContainer.getActiveArrangement();
                    Integer heroIndex = activeArrangement.getHeroIndex();
                    ChatHead hero = (ChatHead) chatContainer.getChatHeads().get(heroIndex);
                    chatContainer.reloadFragment(hero.getKey());
                }
            }
        });

        bringFrontButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List chatHeads = chatContainer.getChatHeads();
                if (chatHeads.size() > 0) {
                    ChatHead chatHead;
                    double rand = Math.random() * (float) chatHeads.size();
                    chatHead = (ChatHead) chatHeads.get((int) rand);
                    chatContainer.bringToFront(chatHead);
                }
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
        //SpringConfiguratorView configuratorView = new SpringConfiguratorView(this);
        //chatContainer.addView(configuratorView, 0);

    }

    private int getInitialX() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        float defaultChatHeadXPosition = width;
        return chatHeadPreferences.getInt("initialX", (int) defaultChatHeadXPosition);
    }

    private int getInitialY() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        float defaultChatHeadYPosition = height * 0.70f;
        return chatHeadPreferences.getInt("initialY", (int) defaultChatHeadYPosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MinimizedArrangement arrangement = (MinimizedArrangement) chatContainer.getArrangement(MinimizedArrangement.class);
        Point idleStatePosition = arrangement.getIdleStatePosition();
        Log.v("idle_position", idleStatePosition.toString());
        chatHeadPreferences.edit().putInt("initialX", idleStatePosition.x).putInt("initialY", idleStatePosition.y).apply();
    }
}
