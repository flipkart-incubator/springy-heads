package com.flipkart.springyheads.demo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.flipkart.chatheads.ui.ChatHead;
import com.flipkart.chatheads.ui.ChatHeadArrangement;
import com.flipkart.chatheads.ui.ChatHeadContainer;
import com.flipkart.chatheads.ui.ChatHeadListener;
import com.flipkart.chatheads.ui.ChatHeadViewAdapter;
import com.flipkart.chatheads.ui.CircularArrangement;
import com.flipkart.chatheads.ui.MaximizedArrangement;
import com.flipkart.chatheads.ui.MinimizedArrangement;
import com.flipkart.springyheads.demo.R;

import java.util.List;


public class MainActivity extends ActionBarActivity {


    private View circularClickArea;
    private SharedPreferences chatHeadPreferences;
    private ChatHeadContainer chatContainer;
    private TextView chatHeadLabel;
    private int id = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_head:
                addChatHead();
                break;
            case R.id.toggle:
                if (chatContainer.getArrangementType() == MinimizedArrangement.class) {
                    chatContainer.setArrangement(MaximizedArrangement.class, new Bundle());
                } else {
                    chatContainer.setArrangement(MinimizedArrangement.class, new Bundle());
                }
                break;
            case R.id.remove_head:
                List chatHeads = chatContainer.getChatHeads();
                if (chatHeads.size() > 0) {
                    double rand = Math.random() * (float) chatHeads.size();
                    ChatHead chatHead = (ChatHead) chatHeads.get((int) rand);
                    chatContainer.removeChatHead(chatHead.getKey(),false);
                }
                break;
            case R.id.remove_all_heads:
                chatContainer.removeAllChatHeads(false);
                break;
            case R.id.select_random:
                chatHeads = chatContainer.getChatHeads();
                if (chatHeads.size() > 0) {
                    double rand = Math.random() * (float) chatHeads.size();
                    ChatHead chatHead = (ChatHead) chatHeads.get((int) rand);
                    chatContainer.bringToFront(chatHead);
                }
                break;
        }
        return true;
    }

    private void addChatHead() {
        chatContainer.addChatHead("head" + Math.random(), false, true);
    }

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
                id++;
                return TestFragment.newInstance(id);
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
        chatContainer.setOnItemSelectedListener(new ChatHeadContainer.OnItemSelectedListener() {
            @Override
            public boolean onChatHeadSelected(Object key, ChatHead chatHead) {
                return false;
            }

            @Override
            public void onChatHeadRollOver(Object key, final ChatHead chatHead) {
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
                //chatHeadLabel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChatHeadRollOut(Object key, ChatHead chatHead) {
                chatHeadLabel.setVisibility(View.INVISIBLE);
            }
        });
        chatContainer.setListener(new ChatHeadListener() {
            @Override
            public void onChatHeadAdded(Object key) {
            }

            @Override
            public void onChatHeadRemoved(Object key, boolean userTriggered) {
            }

            @Override
            public void onChatHeadArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement) {
                setTitle(newArrangement.getClass().getSimpleName());
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
            chatContainer.setArrangement(MinimizedArrangement.class, null);

        }
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
        chatContainer.setArrangement(MinimizedArrangement.class, new Bundle());
        addChatHead();
        addChatHead();
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
        float defaultChatHeadYPosition = height * 0.50f;
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
