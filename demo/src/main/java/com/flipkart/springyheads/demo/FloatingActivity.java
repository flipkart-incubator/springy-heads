package com.flipkart.springyheads.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by kiran.kumar on 06/02/16.
 */
public class FloatingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ChatHeadService.class));
        finish();
    }
}
