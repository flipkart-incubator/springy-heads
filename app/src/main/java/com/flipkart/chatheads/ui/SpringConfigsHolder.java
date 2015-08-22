package com.flipkart.chatheads.ui;

import com.facebook.rebound.SpringConfig;

/**
 * Created by kirankumar on 13/02/15.
 */
public class SpringConfigsHolder {
    public static SpringConfig NOT_DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(190, 20);
    public static SpringConfig CAPTURING = SpringConfig.fromOrigamiTensionAndFriction(100, 10);
    public static SpringConfig DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(0, 1.5);
}
