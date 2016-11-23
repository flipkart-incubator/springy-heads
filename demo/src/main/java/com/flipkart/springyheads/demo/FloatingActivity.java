package com.flipkart.springyheads.demo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by kiran.kumar on 06/02/16.
 */
public class FloatingActivity extends Activity implements View.OnClickListener {

    private Button addButton;
    private Button removeButton;
    private Button removeAllButtons;
    private Button toggleButton;
    private Button updateBadgeCount;

    private ChatHeadService chatHeadService;
    private boolean bound;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ChatHeadService.LocalBinder binder = (ChatHeadService.LocalBinder) service;
            chatHeadService = binder.getService();
            bound = true;
            chatHeadService.minimize();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, ChatHeadService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        setupButtons();
    }

    private void setupButtons() {
        setContentView(R.layout.activity_main);

        addButton = (Button) findViewById(R.id.add_head);
        removeButton = (Button) findViewById(R.id.remove_head);
        removeAllButtons = (Button) findViewById(R.id.remove_all_heads);
        toggleButton = (Button) findViewById(R.id.toggle_arrangement);
        updateBadgeCount = (Button) findViewById(R.id.update_badge_count);

        addButton.setOnClickListener(this);
        removeButton.setOnClickListener(this);
        removeAllButtons.setOnClickListener(this);
        toggleButton.setOnClickListener(this);
        updateBadgeCount.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (bound) {
            if (v == addButton) {
                chatHeadService.addChatHead();
            } else if (v == removeButton) {
                chatHeadService.removeChatHead();
            } else if (v == removeAllButtons) {
                chatHeadService.removeAllChatHeads();
            } else if (v == toggleButton) {
                chatHeadService.toggleArrangement();
            } else if (v == updateBadgeCount) {
                chatHeadService.updateBadgeCount();
            }
        } else {
            Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show();
        }
    }

}
