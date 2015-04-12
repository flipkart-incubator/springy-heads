package com.flipkart.chatheads.ui;

import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringConfigRegistry;

/**
 * Created by kirankumar on 13/02/15.
 */
public class SpringConfigsHolder {
    public static SpringConfig NOT_DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(150, 25);
    public static SpringConfig DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(0, 1.5);
}
