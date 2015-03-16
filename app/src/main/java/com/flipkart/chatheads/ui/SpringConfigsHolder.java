package com.flipkart.chatheads.ui;

import com.facebook.rebound.SpringConfig;

/**
 * Created by kirankumar on 13/02/15.
 */
public class SpringConfigsHolder {
    public static SpringConfig CONVERGING = SpringConfig.fromOrigamiTensionAndFriction(100, 8);
    public static SpringConfig COASTING = SpringConfig.fromOrigamiTensionAndFriction(0, 2);
}
